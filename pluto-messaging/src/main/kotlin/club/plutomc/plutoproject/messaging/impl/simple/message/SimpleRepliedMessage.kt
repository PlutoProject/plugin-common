package club.plutomc.plutoproject.messaging.impl.simple.message

import club.plutomc.plutoproject.apiutils.time.unixTimestamp
import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.Contents
import club.plutomc.plutoproject.messaging.api.Member
import club.plutomc.plutoproject.messaging.api.message.Message
import club.plutomc.plutoproject.messaging.api.message.RepliedMessage
import java.util.*

class SimpleRepliedMessage(@Transient private val channel: Channel, targetMessage: Message, contents: Contents) :
    RepliedMessage, SimpleMessage(
    targetMessage.getUniqueId(),
    channel.getMessenger().getMember(),
    unixTimestamp(),
    contents,
    listOf(targetMessage.getSender())
) {

    private val targetMessage = targetMessage.getUniqueId()
    private val targetMember = targetMessage.getSender()
    private val replier = channel.getMessenger().getMember()

    override fun getTargetMessage(): UUID {
        return targetMessage
    }

    override fun getTargetMember(): Member {
        return targetMember
    }

    override fun getReplier(): Member {
        return replier
    }

}