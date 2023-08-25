package club.plutomc.plutoproject.messaging.impl.velocity

import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.impl.bukkit.BukkitChannel
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.concurrent.ConcurrentHashMap

@OptIn(DelicateCoroutinesApi::class)
class VelocityMessageManager(jedis: JedisPool) : MessageManager {

    private val channelMap: MutableMap<String, Channel>
    private val jedis: JedisPool
    private var isClosed = false
    private lateinit var clientInit: Job
    private lateinit var heartbeat: Job
    private lateinit var clientChannelRegister: Job
    private lateinit var clientChannelExist: Job
    private lateinit var clientChannelUnregister: Job

    init {
        channelMap = ConcurrentHashMap()
        this.jedis = jedis

        init()
        heartbeat()
        clientChannelRegister()
        clientChannelExist()
        clientChannelRegister()
    }

    override fun register(channel: String) {
        if (exist(channel)) {
            return
        }

        channelMap[channel] = BukkitChannel(channel, this, jedis)
        broadcastRegisterChannel(channel)
    }

    override fun get(channel: String): Channel {
        if (!exist(channel)) {
            register(channel)
        }

        return channelMap[channel]!!
    }

    override fun exist(channel: String): Boolean {
        return channelMap.contains(channel)
    }

    override fun unregister(channel: String) {
        if (!exist(channel)) {
            return
        }

        channelMap[channel]!!.close()
        channelMap.remove(channel)
        broadcastUnregisterChannel(channel)
    }

    override fun unregister(channel: Channel) {
        unregister(channel.name)
    }

    override fun close() {
        if (isClosed) {
            return
        }

        channelMap.entries.forEach { it.value.close() }
        jedis.close()
        heartbeat.cancel()
        clientInit.cancel()
        isClosed = true
    }

    private fun init() {
        clientInit = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val jsonObject = JsonParser.parseString(message).asJsonObject

                    if (jsonObject.get("type").asString != "message_client_request") {
                        return
                    }

                    jsonObject.remove("type")
                    jsonObject.addProperty("type", "message_server_response")

                    jedis.resource.publish("message_internal_proxy", jsonObject.toString())
                }
            }, "message_internal_bukkit")
        }
    }

    private fun heartbeat() {
        heartbeat = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val jsonObject = JsonParser.parseString(message).asJsonObject

                    if (jsonObject.get("type").asString != "message_client_heartbeat") {
                        return
                    }

                    jsonObject.remove("type")
                    jsonObject.addProperty("type", "message_server_heartbeat")

                    jedis.resource.publish("message_internal_proxy", jsonObject.toString())
                }
            }, "message_internal_bukkit")
        }
    }

    private fun broadcastRegisterChannel(channel: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", "message_server_broadcast_channel_register")
        jsonObject.addProperty("channel_name", channel)

        jedis.resource.publish("message_internal_proxy", jsonObject.toString())
    }

    private fun broadcastUnregisterChannel(channel: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", "message_server_broadcast_channel_unregister")
        jsonObject.addProperty("channel_name", channel)

        jedis.resource.publish("message_internal_proxy", jsonObject.toString())
    }

    private fun clientChannelRegister() {
        clientChannelRegister = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val jsonObject = JsonParser.parseString(message).asJsonObject

                    if (jsonObject.get("type").asString != "message_client_register_channel") {
                        return
                    }

                    val channelName = jsonObject.get("channel_name").asString

                    jsonObject.remove("type")
                    jsonObject.addProperty("type", "message_server_register_channel")

                    if (exist(channelName)) {
                        jsonObject.addProperty("result", "false")
                        jsonObject.addProperty("cause", "Already existed")
                    }
                    jsonObject.addProperty("result", "true")

                    register(channelName)

                    jedis.resource.publish("message_internal_proxy", jsonObject.toString())
                }
            }, "message_internal_bukkit")
        }
    }

    private fun clientChannelExist() {
        clientChannelExist = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val jsonObject = JsonParser.parseString(message).asJsonObject

                    if (jsonObject.get("type").asString != "message_client_channel_exist") {
                        return
                    }

                    val channelName = jsonObject.get("channel_name").asString

                    jsonObject.remove("type")
                    jsonObject.addProperty("type", "message_server_channel_exist")

                    if (!exist(channelName)) {
                        jsonObject.addProperty("result", "false")
                    }
                    jsonObject.addProperty("result", "true")

                    jedis.resource.publish("message_internal_proxy", jsonObject.toString())
                }
            }, "message_internal_bukkit")
        }
    }

    private fun channelUnregister() {
        clientChannelUnregister = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val jsonObject = JsonParser.parseString(message).asJsonObject

                    if (jsonObject.get("type").asString != "message_client_unregister_channel") {
                        return
                    }

                    val channelName = jsonObject.get("channel_name").asString

                    jsonObject.remove("type")
                    jsonObject.addProperty("type", "message_server_unregister_channel")

                    if (!exist(channelName)) {
                        jsonObject.addProperty("result", "false")
                        jsonObject.addProperty("cause", "Not exist")
                    }
                    jsonObject.addProperty("result", "true")

                    unregister(channelName)

                    jedis.resource.publish("message_internal_proxy", jsonObject.toString())
                }
            }, "message_internal_bukkit")
        }
    }

}