package club.plutomc.plutoproject.messaging.api.message

import club.plutomc.plutoproject.messaging.api.Member
import kotlinx.coroutines.Deferred

interface SendResult {

    /**
     * 获取回复。
     * @param from 需要的回复者列表。
     * @return 回复的消息列表。
     */
    fun getReplies(from: Collection<Member>): Deferred<Collection<RepliedMessage>>

    /**
     * 获取消息发送的目标。
     * @return 消息发送目标。
     */
    fun getTarget(): Collection<Member>

}