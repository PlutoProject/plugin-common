package club.plutomc.plutoproject.messaging.impl.kafka

import club.plutomc.plutoproject.apiutils.data.concurrentHashMapOf
import club.plutomc.plutoproject.apiutils.data.copyOnWriteArraySetOf
import club.plutomc.plutoproject.apiutils.data.nonnull
import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.Member
import club.plutomc.plutoproject.messaging.api.Messenger
import club.plutomc.plutoproject.messaging.impl.simple.ImplementedMessenger
import club.plutomc.plutoproject.messaging.impl.simple.SimpleMember
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import java.util.*

class KafkaMessenger(name: String, val producerProps: Properties, val consumerProps: Properties) : Messenger,
    ImplementedMessenger() {

    val member = SimpleMember(this, name, UUID.randomUUID())
    val cache: Cache<String, Any> = Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(Duration.ofHours(5))
        .build()
    private val channelMap = concurrentHashMapOf<String, Channel>()
    private val memberSet = copyOnWriteArraySetOf<Member>()
    private var proxy: Member? = null
    private val internalChannel = get("_message_internal")

    override fun get(name: String): Channel {
        if (!channelMap.containsKey(name)) {
            channelMap[name] = KafkaChannel(this, name)
        }

        return channelMap[name].nonnull()
    }

    override fun exist(name: String): Boolean {
        return channelMap.containsKey(name)
    }

    override fun remove(name: String) {
        if (!exist(name)) {
            return
        }

        channelMap[name].nonnull().close()
        channelMap.remove(name)
    }

    /*override fun getOperator(): Operator {
        return operator
    }*/

    override fun getMembers(): Collection<Member> {
        return memberSet
    }

    override fun getMember(): Member {
        return member
    }

    override fun getProxy(): Member? {
        return proxy
    }

    override fun getInternalChannel(): Channel {
        return internalChannel
    }

}