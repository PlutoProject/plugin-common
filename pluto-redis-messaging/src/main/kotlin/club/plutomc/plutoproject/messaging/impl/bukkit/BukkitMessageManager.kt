package club.plutomc.plutoproject.messaging.impl.bukkit

import club.plutomc.plutoproject.apiutils.concurrent.launchWithPluto
import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.impl.ImplUtils
import club.plutomc.plutoproject.messaging.plugin.BukkitMessagingPlugin
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class BukkitMessageManager(jedis: JedisPool) : MessageManager {

    private val channelMap: MutableMap<String, Channel>
    private val thisMessageManager: MessageManager
    private val jedis: JedisPool
    private var isClosed = false
    private var scope: CoroutineScope
    private lateinit var heartbeat: Job
    private lateinit var heartbeatWait: Job
    private lateinit var serverChannelRegister: Job
    private lateinit var serverChannelExist: Job
    private lateinit var serverChannelUnregister: Job
    private var coroutineData: MutableSharedFlow<String>

    init {
        this.channelMap = ConcurrentHashMap()
        this.thisMessageManager = this
        this.jedis = jedis
        this.scope = BukkitMessagingPlugin.coroutineScope
        this.coroutineData = MutableSharedFlow(10)

        init()
        // heartbeat()
        // heartbeatWait()
        serverChannelRegister()
        serverChannelExist()
        serverChannelUnregister()
    }

    override fun register(channel: String) {
        if (exist(channel)) {
            ImplUtils.debugLogWarn("Channel register invoked, but this channel already existed!")
            return
        }

        requestChannelRegister(channel)
        localRegister(channel)
    }

    override fun get(channel: String): Channel {
        register(channel)
        return channelMap[channel]!!
    }

    override fun exist(channel: String): Boolean {
        if (isClosed) {
            ImplUtils.debugLogWarn("Channel exist invoked, but this manager already closed!")
            return false
        }

        if (localExist(channel)) {
            ImplUtils.debugLogInfo("Channel $channel exist in local, returning")
            return true
        }

        val id = UUID.randomUUID()
        val response = CompletableDeferred<String>()

        GlobalScope.launch {
            ImplUtils.debugLogInfo("Start to collect coroutine data (channelExist)")
            coroutineData.collect {
                if (!it.startsWith(id.toString())) {
                    ImplUtils.debugLogWarn("Collected a data which isn't we want (id we need: $id, this data: $it)")
                    return@collect
                }

                ImplUtils.debugLogInfo("Collected a data we want, returning ($it) ")
                response.complete(it.substring(it.indexOf(",") + 1, it.lastIndex))
                cancel()
            }
        }

        requestChannelExist(channel, id.toString())

        runBlocking {
            response.await()
            ImplUtils.debugLogInfo("Deferred waiting completed! (${response.getCompleted()}")
        }

        return response.getCompleted().toBoolean()
    }

    override fun unregister(channel: String) {
        if (!exist(channel)) {
            return
        }

        requestChannelUnregister(channel)
        localUnregister(channel)
    }

    override fun unregister(channel: Channel) = unregister(channel.name)

    override fun close() {
        if (isClosed) {
            return
        }

        channelMap.entries.forEach { it.value.close() }
        // heartbeat.cancel()'
        // heartbeatWait.cancel()
        serverChannelRegister.cancel()
        serverChannelExist.cancel()
        serverChannelUnregister.cancel()
    }

    private fun init() {
        var received = false

        val requestContent = JsonObject()
        val id = UUID.randomUUID()

        requestContent.addProperty("id", id.toString())
        requestContent.addProperty("type", "message_client_request")

        val receiveJob = scope.launchWithPluto {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    checkNotNull(message)
                    val responseContent = JsonParser.parseString(message).asJsonObject

                    if (responseContent.get("id").asString != id.toString()) {
                        return
                    }

                    if (responseContent.get("type").asString != "message_server_response") {
                        return
                    }

                    ImplUtils.debugLogInfo("Received server response, client initialized: $responseContent")
                    unsubscribe()
                }
            }, "message_internal_proxy")
        }

        runBlocking {
            delay(3000L)
        }

        val requestJob = GlobalScope.launch {
            while (!received) {
                ImplUtils.debugLogInfo("Send client init request: $requestContent")
                jedis.resource.publish("message_internal_bukkit", requestContent.toString())
                delay(5000L)
            }
        }

        runBlocking {
            receiveJob.join()
        }

        received = true
        requestJob.cancel()
    }

    private fun heartbeat() {
        val received = AtomicBoolean(false)

        heartbeat = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val responseContent = JsonParser.parseString(message).asJsonObject

                    if (responseContent.get("type").asString != "message_server_heartbeat") {
                        return
                    }

                    received.set(true)
                    ImplUtils.debugLogInfo("Heartbeat received: $responseContent")
                    responseContent.remove("type")
                    responseContent.addProperty("type", "message_client_heartbeat")

                    jedis.resource.publish("message_internal_bukkit", responseContent.toString())
                    ImplUtils.debugLogInfo("Heartbeat sent: $responseContent")
                }
            }, "message_internal_proxy")
        }

        heartbeatWait = GlobalScope.launch {
            while (true) {
                delay(5000L)

                if (!received.get()) {
                    close()
                    ImplUtils.debugLogError("Heartbeat timeout!")
                }

                received.set(false)
            }
        }
    }

    private fun requestChannelRegister(name: String) {
        val requestContent = JsonObject()
        requestContent.addProperty("type", "message_client_register_channel")
        requestContent.addProperty("channel_name", name)

        ImplUtils.debugLogInfo("Sending channel register request to server: $requestContent")
        jedis.resource.publish(requestContent.asString, "message_internal_bukkit")
    }

    private fun requestChannelExist(name: String, id: String) {
        val requestContent = JsonObject()
        requestContent.addProperty("id", id)
        requestContent.addProperty("type", "message_client_channel_exist")
        requestContent.addProperty("channel_name", name)

        ImplUtils.debugLogInfo("Sending channel check-exist request to server (id: $id): $requestContent")
        jedis.resource.publish("message_internal_bukkit", requestContent.toString())
    }

    private fun requestChannelUnregister(name: String) {
        val requestContent = JsonObject()
        requestContent.addProperty("type", "message_client_unregister_channel")
        requestContent.addProperty("channel_name", name)

        ImplUtils.debugLogInfo("Sending channel unregister request to server: $requestContent")
        jedis.resource.publish(requestContent.asString, "message_internal_bukkit")
    }

    private fun localRegister(channel: String) {
        if (localExist(channel)) {
            return
        }

        channelMap[channel] = BukkitChannel(channel, this, jedis)
    }

    private fun localExist(channel: String): Boolean = channelMap.contains(channel)

    private fun localUnregister(channel: String) {
        if (!localExist(channel)) {
            return
        }

        channelMap[channel]!!.close()
        channelMap.remove(channel)
    }

    private fun serverChannelRegister() {
        serverChannelRegister = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    checkNotNull(message)
                    val responseContent = JsonParser.parseString(message).asJsonObject

                    if (responseContent.get("type").asString != "message_server_broadcast_channel_register") {
                        return
                    }

                    val channelName = responseContent.get("channel_name").asString

                    if (channelMap.contains(channelName)) {
                        ImplUtils.debugLogWarn("Received server channel registration broadcast, but already contained in cache (maybe this client made the request?): $responseContent")
                        return
                    }

                    ImplUtils.debugLogInfo("Received server channel registration: $responseContent")
                    localRegister(channelName)
                }
            }, "message_internal_proxy")
        }
    }

    private fun serverChannelExist() {
        serverChannelExist = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    checkNotNull(message)
                    val responseContent = JsonParser.parseString(message).asJsonObject

                    if (responseContent.get("type").asString != "message_server_channel_exist") {
                        return
                    }


                    val result = responseContent.get("result").asString
                    val id = responseContent.get("id").asString

                    runBlocking {
                        coroutineData.emit("$id, $result")
                    }
                }
            }, "message_internal_proxy")
        }
    }

    private fun serverChannelUnregister() {
        serverChannelUnregister = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    checkNotNull(message)
                    val responseContent = JsonParser.parseString(message).asJsonObject

                    if (responseContent.get("type").asString != "message_server_broadcast_channel_unregister") {
                        return
                    }

                    val channelName = responseContent.get("channel_name").asString

                    if (!channelMap.contains(channelName)) {
                        ImplUtils.debugLogWarn("Received server channel un-registration broadcast, but didn't find in cache (maybe this client made the request?): $responseContent")
                        return
                    }

                    ImplUtils.debugLogInfo("Received server channel un-registration: $responseContent")
                    localUnregister(channelName)
                }
            }, "message_internal_proxy")
        }
    }
}