package club.plutomc.plutoproject.messaging.impl.simple.message

import club.plutomc.plutoproject.messaging.api.Contents
import club.plutomc.plutoproject.messaging.api.Member
import club.plutomc.plutoproject.messaging.api.message.Message
import club.plutomc.plutoproject.messaging.impl.simple.SimpleContents
import club.plutomc.plutoproject.messaging.impl.simple.SimpleMember
import java.util.*

open class SimpleMessage(
    private val uuid: UUID,
    sender: Member,
    private val time: Long,
    contents: Contents,
    targets: Collection<Member>,
    private val disableMemberCheck: Boolean = false
) : Message {

    internal open var type = "message"
    private val sender = sender as SimpleMember
    private val contents = contents as SimpleContents

    @Suppress("UNCHECKED_CAST")
    private val targets = targets as Collection<SimpleMember>

    override fun getUniqueId(): UUID {
        return uuid
    }

    override fun getContents(): Contents {
        return contents
    }

    override fun getTime(): Date {
        return Date(time * 1000)
    }

    override fun getSender(): Member {
        return sender
    }

    override fun getTargets(): Collection<Member> {
        return targets
    }

    override fun isMemberCheckDisabled(): Boolean {
        return disableMemberCheck
    }

}