package club.plutomc.plutoproject.messaging.api.message

import club.plutomc.plutoproject.messaging.api.Member
import java.util.*

interface RepliedMessage : Message {

    /**
     * 获取回复的目标消息。
     * @return 目标消息的 UUID。
     */
    fun getTargetMessage(): UUID

    /**
     * 获取回复的目标成员。
     * @return 目标成员。
     */
    fun getTargetMember(): Member

    /**
     * 获取回复者。
     * @return 回复者。
     */
    fun getReplier(): Member

}