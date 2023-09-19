package club.plutomc.plutoproject.messaging.api

import java.util.*

interface Member {

    /**
     * 获取成员名称。
     * @return 成员名称。
     */
    fun getName(): String

    /**
     * 获取成员 UUID。
     * @return 成员 UUID。
     */
    fun getUniqueId(): UUID

    /**
     * 通过内部通信通道向该成员发送消息。
     * @param contents 消息内容。
     */
    fun send(contents: Contents)

}