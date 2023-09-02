package club.plutomc.plutoproject.messaging.impl.velocity

import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.api.Subscription
import club.plutomc.plutoproject.messaging.plugin.event.velocity.MessageReceivedEvent
import club.plutomc.plutoproject.messaging.plugin.event.velocity.SelfMessageReceivedEvent
import club.plutomc.plutoproject.messaging.impl.ImplUtils
import club.plutomc.plutoproject.messaging.plugin.VelocityMessagingPlugin
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
    private var subs: ConcurrentHashMap<String, Subscription>
    private lateinit var clientChannelPublish: Job

    init {
        this._name = name
        this.thisChannel = this
        this.messageManager = messageManager
        this.jedis = jedis
        this.subs = ConcurrentHashMap()

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

        subs.forEach {
            it.value.onMessageWithSelf(thisChannel, content)
        }

        VelocityMessagingPlugin.server.eventManager.fire(SelfMessageReceivedEvent(name, content))
        publishMessageToBukkit(content)
    }

    override fun subscribe(name: String, subscription: Subscription) {
        if (!isValid()) {
            return
        }

        if (subs.contains(name)) {
            return
        }

        subs[name] = subscription
    }

    override fun unsubscribe(name: String) {
        subs.remove(name)
    }

    override fun close() {
        clientChannelPublish.cancel()
    }

    private fun publishMessageToBukkit(jsonObject: JsonObject) {
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

                    subs.forEach {
                        ImplUtils.debugLogInfo("Running action: ${it.key}")
                        it.value.onMessage(thisChannel, messageContent)
                    }

                    VelocityMessagingPlugin.server.eventManager.fire(MessageReceivedEvent(name, messageContent))
                    jedis.resource.publish("message_internal_proxy", responseContent.toString())
                }
            }, "message_internal_bukkit")
        }
    }

}