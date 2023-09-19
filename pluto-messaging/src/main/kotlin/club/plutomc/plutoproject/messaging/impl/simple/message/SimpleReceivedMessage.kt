package club.plutomc.plutoproject.messaging.impl.simple.message

import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.Contents
import club.plutomc.plutoproject.messaging.api.message.Message
import club.plutomc.plutoproject.messaging.api.message.ReceivedMessage

class SimpleReceivedMessage(private val channel: Channel, message: Message) : ReceivedMessage,
    SimpleMessage(
        message.getUniqueId(),
        message.getSender(),
        message.getTime().time / 1000,
        message.getContents(),
        message.getTargets()
    ) {

    override fun reply(contents: Contents) {
        channel.reply(this, contents)
    }

}