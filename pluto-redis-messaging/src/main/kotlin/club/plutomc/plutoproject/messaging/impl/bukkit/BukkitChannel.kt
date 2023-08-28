package club.plutomc.plutoproject.messaging.impl.bukkit

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
import java.util.concurrent.CopyOnWriteArraySet

@OptIn(DelicateCoroutinesApi::class)
class BukkitChannel(name: String, messageManager: MessageManager, jedis: JedisPool) : Channel {

    private var _name: String
    private var thisChannel: Channel
    private var messageManager: MessageManager
    private var jedis: JedisPool
    private var ids: CopyOnWriteArraySet<String>
    private var actions: ConcurrentHashMap<String, Action>
    private lateinit var serverChannelPublish: Job

    init {
        this._name = name
        this.thisChannel = this
        this.messageManager = messageManager
        this.jedis = jedis
        ids = CopyOnWriteArraySet()
        this.actions = ConcurrentHashMap()

        serverChannelPublish()
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
        serverChannelPublish.cancel()
    }

    private fun publishMessage(jsonObject: JsonObject) {
        val requestContent = JsonObject()
        val id = UUID.randomUUID()
        ids.add(id.toString())
        requestContent.addProperty("id", id.toString())
        requestContent.addProperty("type", "message_client_publish")
        requestContent.addProperty("channel_name", name)
        requestContent.addProperty("content", jsonObject.toString())

        ImplUtils.debugLogInfo("Client published message: $jsonObject, sending request to server: $requestContent")
        jedis.resource.publish("message_internal_bukkit", jsonObject.toString())
    }

    private fun serverChannelPublish() {
        serverChannelPublish = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    checkNotNull(message)
                    val responseContent = JsonParser.parseString(message).asJsonObject
                    val id = responseContent.get("id").asString

                    if (responseContent.get("type").asString != "message_server_publish") {
                        return
                    }

                    if (responseContent.get("channel_name").asString != name) {
                        return
                    }

                    if (ids.contains(id)) {
                        ImplUtils.debugLogWarn("Received a message published by server, but it was published by this client: $responseContent")
                        ids.remove(id)
                        return
                    }

                    val messageContent = JsonParser.parseString(responseContent.get("content").asString).asJsonObject
                    ImplUtils.debugLogInfo("Received a message published by server: $messageContent, original message: $responseContent")
                    actions.forEach {
                        ImplUtils.debugLogInfo("Running action: ${it.key}")
                        it.value.onMessage(thisChannel, messageContent)
                    }
                }
            }, "message_internal_proxy")
        }
    }

}