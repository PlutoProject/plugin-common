package club.plutomc.plutoproject.messaging.impl.velocity

import club.plutomc.plutoproject.messaging.api.Action
import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.impl.ImplUtils
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@OptIn(DelicateCoroutinesApi::class)
class VelocityChannel(name: String, messageManager: MessageManager, jedis: JedisPool) : Channel {

    private var _name: String
    private var thisChannel: Channel
    private var messageManager: MessageManager
    private var jedis: JedisPool
    private var actions: ConcurrentHashMap<String, Action>
    private lateinit var clientChannelPublish: Job

    init {
        this._name = name
        this.thisChannel = this
        this.messageManager = messageManager
        this.jedis = jedis
        this.actions = ConcurrentHashMap()

        clientChannelPublish()
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
        clientChannelPublish.cancel()
    }

    private fun publishMessage(jsonObject: JsonObject) {
        val requestContent = JsonObject()
        val id = UUID.randomUUID()
        requestContent.addProperty("id", id.toString())
        requestContent.addProperty("type", "message_server_publish")
        requestContent.addProperty("channel_name", name)
        requestContent.addProperty("content", jsonObject.toString())

        ImplUtils.debugLogInfo("Server published message: $jsonObject, sending broadcast to clients: $requestContent")
        jedis.resource.publish("message_internal_proxy", jsonObject.toString())
    }

    private fun clientChannelPublish() {
        clientChannelPublish = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    checkNotNull(message)
                    val responseContent = JsonParser.parseString(message).asJsonObject

                    if (responseContent.get("type").asString != "message_client_publish") {
                        return
                    }

                    if (responseContent.get("channel_name").asString != name) {
                        return
                    }

                    jedis.resource.publish("message_internal_proxy", responseContent.toString())

                    val messageContent = JsonParser.parseString(responseContent.get("content").asString).asJsonObject
                    ImplUtils.debugLogInfo("Received a message published by client: $messageContent, original message: $responseContent")
                    actions.forEach {
                        ImplUtils.debugLogInfo("Running action: ${it.key}")
                        it.value.onMessage(thisChannel, messageContent)
                    }
                }
            }, "message_internal_bukkit")
        }
    }

}