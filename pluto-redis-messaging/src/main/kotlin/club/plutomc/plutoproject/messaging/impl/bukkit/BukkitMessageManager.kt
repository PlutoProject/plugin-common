package club.plutomc.plutoproject.messaging.impl.bukkit

import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.impl.ImplUtils
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(DelicateCoroutinesApi::class)
class BukkitMessageManager(jedis: JedisPool) : MessageManager {

    private val channelMap: MutableMap<String, Channel>
    private val thisMessageManager: MessageManager
    private val jedis: JedisPool
    private var isClosed = false
    private lateinit var heartbeat: Job
    private lateinit var heartbeatWait: Job
    private lateinit var serverChannelRegister: Job
    private lateinit var serverChannelUnregister: Job

    init {
        this.channelMap = ConcurrentHashMap()
        this.thisMessageManager = this
        this.jedis = jedis

        init()
        heartbeat()
        serverChannelRegister()
        serverChannelUnregister()
    }

    override fun register(channel: String) {
        TODO("Not yet implemented")
    }

    override fun get(channel: String): Channel {
        TODO("Not yet implemented")
    }

    override fun exist(channel: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun unregister(channel: String) {
        TODO("Not yet implemented")
    }

    override fun unregister(channel: Channel) {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    private fun init() {
        var received = false

        val requestContent = JsonObject()
        val id = UUID.randomUUID()

        requestContent.addProperty("id", id.toString())
        requestContent.addProperty("type", "message_client_request")

        val requestJob = GlobalScope.launch {
            while (!received) {
                ImplUtils.debugLogInfo("Send client init request: $requestContent")
                jedis.resource.publish("message_internal_bukkit", requestContent.toString())
                delay(5000L)
            }
        }

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
            }
        }, "message_internal_proxy")

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

    private fun serverChannelRegister() {
        serverChannelRegister = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
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
                    channelMap[channelName] = BukkitChannel(channelName, thisMessageManager, jedis)
                }
            }, "message_internal_proxy")
        }
    }

    private fun serverChannelUnregister() {
        serverChannelUnregister = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
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
                    channelMap.remove(channelName)
                }
            }, "message_internal_proxy")
        }
    }
}