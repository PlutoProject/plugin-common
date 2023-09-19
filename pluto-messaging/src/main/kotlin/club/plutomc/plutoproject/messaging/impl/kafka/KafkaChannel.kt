package club.plutomc.plutoproject.messaging.impl.kafka

import club.plutomc.plutoproject.apiutils.concurrent.launchWithPluto
import club.plutomc.plutoproject.apiutils.data.concurrentHashMapOf
import club.plutomc.plutoproject.apiutils.data.copyOnWriteArraySetOf
import club.plutomc.plutoproject.apiutils.data.nonnull
import club.plutomc.plutoproject.apiutils.json.toJsonString
import club.plutomc.plutoproject.apiutils.json.toObject
import club.plutomc.plutoproject.apiutils.time.unixTimestamp
import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.Contents
import club.plutomc.plutoproject.messaging.api.Member
import club.plutomc.plutoproject.messaging.api.Messenger
import club.plutomc.plutoproject.messaging.api.message.Message
import club.plutomc.plutoproject.messaging.api.message.ReceivedMessage
import club.plutomc.plutoproject.messaging.api.message.RepliedMessage
import club.plutomc.plutoproject.messaging.api.message.SendResult
import club.plutomc.plutoproject.messaging.impl.simple.ImplementedChannel
import club.plutomc.plutoproject.messaging.impl.simple.message.SimpleMessage
import club.plutomc.plutoproject.messaging.impl.simple.message.SimpleReceivedMessage
import club.plutomc.plutoproject.messaging.impl.simple.message.SimpleRepliedMessage
import club.plutomc.plutoproject.messaging.impl.simple.message.SimpleSendResult
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Duration
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

class KafkaChannel(messenger: Messenger, private val name: String) : Channel, ImplementedChannel() {

    private val instance = this
    private val topic = name
    private val messenger = messenger as KafkaMessenger
    private val cache = this.messenger.cache.apply {
        this.put("channel.$topic.consumedMessageIds", copyOnWriteArraySetOf<UUID>())
        this.put("channel.$topic.repliedMessages", copyOnWriteArraySetOf<RepliedMessage>())
        this.put("channel.$topic.repliedMessagesByMe", copyOnWriteArraySetOf<RepliedMessage>())
    }
    private val member = this.messenger.member
    private val producer = KafkaProducer<String, String>(this.messenger.producerProps)
    private val consumer = KafkaConsumer<String, String>(this.messenger.consumerProps.apply {
        this[ConsumerConfig.GROUP_ID_CONFIG] = "_${member.getName()}\$${member.getUniqueId()}\$$topic"
        this[ConsumerConfig.CLIENT_ID_CONFIG] = "_${member.getName()}\$${member.getUniqueId()}\$client\$$topic"
    }).apply {
        this.subscribe(listOf(topic))
    }

    @Suppress("UNCHECKED_CAST")
    private val consumedMessageIds
        get() = cache.get("channel.$topic.consumedMessageIds") { _ -> copyOnWriteArraySetOf<UUID>() } as CopyOnWriteArraySet<UUID>

    @Suppress("UNCHECKED_CAST")
    private val repliedMessages
        get() = cache.get("channel.$topic.repliedMessages") { _ -> copyOnWriteArraySetOf<UUID>() } as CopyOnWriteArraySet<RepliedMessage>

    @Suppress("UNCHECKED_CAST")
    private val repliedMessagesByMe
        get() = cache.get("channel.$topic.repliedMessagesByMe") { _ -> copyOnWriteArraySetOf<UUID>() } as CopyOnWriteArraySet<RepliedMessage>

    private val subscriptionMap = concurrentHashMapOf<String, (Channel, ReceivedMessage) -> Unit>()
    private val scope = MainScope().apply {
        this.launchWithPluto {
            while (true) {
                val records = consumer.poll(Duration.ofMillis(100))
                records.forEach { record ->

                    val value = record.value()

                    if (JsonParser.parseString(value).asJsonObject.keySet().contains("replier")) {
                        val message = value.toObject<SimpleRepliedMessage>()
                        if (doMessageCheck(message)) {
                            repliedMessages.add(message)
                        }
                    }

                    val message = value.toObject<SimpleMessage>()

                    if (doMessageCheck(message)) {
                        subscriptionMap.values.forEach {
                            it(instance, SimpleReceivedMessage(instance, message))
                        }
                    }

                    consumedMessageIds.add(message.getUniqueId())
                }
            }
        }
    }

    private fun doMessageCheck(message: Message): Boolean {
        return (message.getTargets()
            .contains(messenger.getMember()) || message.isMemberCheckDisabled()) && message.getSender()
            .getUniqueId() != messenger.getMember().getUniqueId()
    }

    override fun send(contents: Contents): SendResult {
        return sendTo(contents, *messenger.getMembers().toTypedArray(), disableMemberCheck = true)
    }

    override fun sendTo(contents: Contents, vararg targets: Member, disableMemberCheck: Boolean): SendResult {
        return sendProvidedMessage(
            SimpleMessage(
                UUID.randomUUID(), member, unixTimestamp(), contents, targets.toCollection(
                    mutableSetOf()
                ), disableMemberCheck
            )
        )
    }

    override fun sendToExcept(contents: Contents, vararg targets: Member): SendResult {
        return sendTo(contents, *messenger.getMembers().filter { !targets.contains(it) }.toTypedArray())
    }

    override fun sendToProxy(contents: Contents): SendResult {
        return sendTo(contents, messenger.getProxy().nonnull())
    }

    override fun subscribe(name: String, action: (Channel, ReceivedMessage) -> Unit) {
        if (subscriptionMap.keys.any { it == name }) {
            return
        }

        subscriptionMap[name] = action
    }

    override fun unsubscribe(name: String) {
        if (!subscriptionMap.keys.any { it == name }) {
            return
        }

        subscriptionMap.remove(name)
    }

    override fun reply(target: Message, contents: Contents) {
        sendProvidedMessage(SimpleRepliedMessage(this, target, contents))
    }

    override fun getName(): String {
        return name
    }

    override fun close() {
        scope.cancel()
    }

    override fun getMessenger(): Messenger {
        return messenger
    }

    override fun getCoroutineScope(): CoroutineScope {
        return scope
    }

    override fun getRepliedMessages(): Collection<RepliedMessage> {
        return repliedMessages
    }

    override fun getRepliedMessagesByMe(): Collection<RepliedMessage> {
        return repliedMessagesByMe
    }

    private fun sendProvidedMessage(message: Message): SendResult {
        producer.send(ProducerRecord(topic, message.toJsonString()))
        return SimpleSendResult(this, message)
    }

}