package club.plutomc.plutoproject.messaging.api.message

import club.plutomc.plutoproject.messaging.api.Contents
import club.plutomc.plutoproject.messaging.api.Member
import java.util.*

interface Message {

    /**
     * 获取消息的 UUID。
     * @return 消息 UUID。
     */
    fun getUniqueId(): UUID

    /**
     * 获取消息的内容。
     * @return 消息内容。
     */
    fun getContents(): Contents

    /**
     * 获取消息发送时间。
     * @return 消息发送时间。
     */
    fun getTime(): Date

    /**
     * 获取消息发送者。
     * @return 消息发送者。
     */
    fun getSender(): Member

    fun getTargets(): Collection<Member>

    fun isMemberCheckDisabled(): Boolean

}