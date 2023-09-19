package club.plutomc.plutoproject.messaging.api.message

import club.plutomc.plutoproject.messaging.api.Contents

interface ReceivedMessage : Message {

    /**
     * 发送一条消息回复。
     * @param contents 回复内容。
     */
    fun reply(contents: Contents)

}