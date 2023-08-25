package club.plutomc.plutoproject.messaging.impl.velocity

import club.plutomc.plutoproject.messaging.api.Action
import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.MessageManager
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
class VelocityChannel(name: String, messageManager: MessageManager, jedis: JedisPool) : Channel {

    private var _name: String
    private var messageManager: MessageManager
    private var jedis: JedisPool
    private var actions: ConcurrentHashMap<String, Action>
    private lateinit var receiveJob: Job

    init {
        this._name = name
        this.messageManager = messageManager
        this.jedis = jedis
        this.actions = ConcurrentHashMap()

        receiveProcessor()
    }

    override val name: String
        get() = _name

    override fun isValid(): Boolean {
        return messageManager.exist(name)
    }

    override fun publish(content: JsonObject) {
        if (!isValid()) {
            return
        }

        publishMessage(content)
    }

    override fun subscribe(name: String, action: Action) {
        if (!isValid()) {
            return
        }

        if (actions.contains(name)) {
            return
        }

        actions[name] = action
    }

    override fun unsubscribe(name: String) {
        actions.remove(name)
    }

    override fun close() {
        receiveJob.cancel()
    }

    private fun publishMessage(jsonObject: JsonObject) {
        val contentObject = JsonObject()
        contentObject.addProperty("type", "message_server_publish")
        contentObject.addProperty("channel_name", name)
        contentObject.addProperty("content", jsonObject.toString())

        jedis.resource.publish("message_internal_proxy", jsonObject.toString())
    }

    private fun receiveProcessor() {
        val channelObject = this

        receiveJob = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val jsonObject = JsonParser.parseString(message).asJsonObject

                    if (jsonObject.get("type").asString != "message_client_publish") {
                        return
                    }

                    if (jsonObject.get("channel_name").asString != name) {
                        return
                    }

                    val resultObject = JsonParser.parseString(jsonObject.get("content").asString).asJsonObject
                    actions.entries.forEach { it.value.onMessage(channelObject, resultObject) }
                }
            }, "message_internal_bukkit")
        }
    }

}