package club.plutomc.plutoproject.messaging.api

import club.plutomc.plutoproject.messaging.api.message.Message
import club.plutomc.plutoproject.messaging.api.message.ReceivedMessage
import club.plutomc.plutoproject.messaging.api.message.SendResult

interface Channel {

    /**
     * 发送一条消息。
     * @param contents 消息内容
     */
    fun send(contents: Contents): SendResult

    fun sendTo(contents: Contents, vararg targets: Member, disableMemberCheck: Boolean = false): SendResult

    fun sendToExcept(contents: Contents, vararg targets: Member): SendResult

    fun sendToProxy(contents: Contents): SendResult

    /**
     * 订阅频道并做出处理。
     * @param name 订阅名称。
     * @param action 当接收到消息时的行动
     */
    fun subscribe(name: String, action: (Channel, ReceivedMessage) -> Unit)

    fun unsubscribe(name: String)

    fun reply(target: Message, contents: Contents)

    /**
     * 获取频道的名称。
     * @return 频道名称
     */
    fun getName(): String

    /**
     * 关闭频道监听。
     * 该方法会从本地删除该频道，届时所有监听将关闭，但是不会影响到整个网络中的频道列表。
     */
    fun close()

    fun getMessenger(): Messenger

}