package club.plutomc.plutoproject.messaging.impl.velocity

import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.impl.ImplUtils
import club.plutomc.plutoproject.messaging.impl.bukkit.BukkitChannel
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(DelicateCoroutinesApi::class)
class VelocityMessageManager(jedis: JedisPool) : MessageManager {

    private val channelMap: MutableMap<String, Channel>
    private val jedis: JedisPool
    private var isClosed = false
    private lateinit var clientInit: Job
    private lateinit var heartbeat: Job
    private lateinit var heartbeatWait: Job
    private lateinit var clientChannelRegister: Job
    private lateinit var clientChannelExist: Job
    private lateinit var clientChannelUnregister: Job

    init {
        this.channelMap = ConcurrentHashMap()
        this.jedis = jedis

        init()
        // heartbeat()
        clientChannelRegister()
        clientChannelExist()
        clientChannelUnregister()
    }

    override fun register(channel: String) {
        if (exist(channel)) {
            return
        }

        channelMap[channel] = BukkitChannel(channel, this, jedis)
        broadcastRegisterChannel(channel)
    }

    override fun get(channel: String): Channel {
        register(channel)
        return channelMap[channel]!!
    }

    override fun exist(channel: String): Boolean = if (!isClosed) channelMap.contains(channel) else false

    override fun unregister(channel: String) {
        if (!exist(channel)) {
            return
        }

        channelMap[channel]!!.close()
        channelMap.remove(channel)
        broadcastUnregisterChannel(channel)
    }

    override fun unregister(channel: Channel) = unregister(channel.name)

    override fun close() {
        if (isClosed) {
            return
        }

        channelMap.entries.forEach { it.value.close() }
        // heartbeat.cancel()
        // heartbeatWait.cancel()
        clientInit.cancel()
        clientChannelRegister.cancel()
        clientChannelExist.cancel()
        clientChannelUnregister.cancel()
        isClosed = true
    }

    private fun init() {
        clientInit = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val responseContent = JsonParser.parseString(message).asJsonObject

                    if (responseContent.get("type").asString != "message_client_request") {
                        return
                    }

                    responseContent.remove("type")
                    responseContent.addProperty("type", "message_server_response")

                    ImplUtils.debugLogInfo("Received client init request, sending response: $responseContent")
                    jedis.resource.publish("message_internal_proxy", responseContent.toString())
                }
            }, "message_internal_bukkit")
        }
    }

    private fun heartbeat() {
        val received = AtomicBoolean(false)

        heartbeat = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val responseContent = JsonParser.parseString(message).asJsonObject

                    if (responseContent.get("type").asString != "message_client_heartbeat") {
                        return
                    }

                    received.set(true)
                    ImplUtils.debugLogInfo("Heartbeat received: $responseContent")
                    responseContent.remove("type")
                    responseContent.addProperty("type", "message_server_heartbeat")

                    jedis.resource.publish("message_internal_proxy", responseContent.toString())
                    ImplUtils.debugLogInfo("Heartbeat sent: $responseContent")
                }
            }, "message_internal_bukkit")
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

    private fun broadcastRegisterChannel(channel: String) {
        val responseContent = JsonObject()
        responseContent.addProperty("type", "message_server_broadcast_channel_register")
        responseContent.addProperty("channel_name", channel)

        ImplUtils.debugLogInfo("Broadcasting a channel registration: $responseContent")
        jedis.resource.publish("message_internal_proxy", responseContent.toString())
    }

    private fun broadcastUnregisterChannel(channel: String) {
        val responseContent = JsonObject()
        responseContent.addProperty("type", "message_server_broadcast_channel_unregister")
        responseContent.addProperty("channel_name", channel)

        ImplUtils.debugLogInfo("Broadcasting a channel un-registration: $responseContent")
        jedis.resource.publish("message_internal_proxy", responseContent.toString())
    }

    private fun clientChannelRegister() {
        clientChannelRegister = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val requestContent = JsonParser.parseString(message).asJsonObject

                    if (requestContent.get("type").asString != "message_client_register_channel") {
                        return
                    }

                    val channelName = requestContent.get("channel_name").asString

                    if (exist(channelName)) {
                        ImplUtils.debugLogWarn("Received client channel registration, but already registered: $requestContent")
                        return
                    }

                    ImplUtils.debugLogInfo("Received client channel registration: $requestContent")
                    register(channelName)
                    broadcastRegisterChannel(channelName)
                }
            }, "message_internal_bukkit")
        }
    }

    private fun clientChannelExist() {
        clientChannelExist = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val requestContent = JsonParser.parseString(message).asJsonObject

                    if (requestContent.get("type").asString != "message_client_channel_exist") {
                        return
                    }

                    val channelName = requestContent.get("channel_name").asString

                    requestContent.remove("type")
                    requestContent.addProperty("type", "message_server_channel_exist")
                    requestContent.addProperty("result", exist(channelName).toString())

                    ImplUtils.debugLogInfo("Received a client channel check, sending response: $requestContent")
                    jedis.resource.publish("message_internal_proxy", requestContent.toString())
                }
            }, "message_internal_bukkit")
        }
    }

    private fun clientChannelUnregister() {
        clientChannelUnregister = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val requestContent = JsonParser.parseString(message).asJsonObject

                    if (requestContent.get("type").asString != "message_client_unregister_channel") {
                        return
                    }

                    val channelName = requestContent.get("channel_name").asString

                    if (!exist(channelName)) {
                        ImplUtils.debugLogWarn("Received client channel un-registration, but it not registered: $requestContent")
                    }

                    ImplUtils.debugLogInfo("Received client channel un-registration: $requestContent")
                    unregister(channelName)
                    broadcastUnregisterChannel(channelName)
                }
            }, "message_internal_bukkit")
        }
    }

}