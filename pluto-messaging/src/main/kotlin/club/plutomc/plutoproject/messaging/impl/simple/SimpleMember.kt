package club.plutomc.plutoproject.messaging.impl.simple

import club.plutomc.plutoproject.messaging.api.Contents
import club.plutomc.plutoproject.messaging.api.Member
import club.plutomc.plutoproject.messaging.api.Messenger
import java.util.*

class SimpleMember(@Transient private val messenger: Messenger, private val name: String, private val uuid: UUID) :
    Member {

    override fun getName(): String {
        return name
    }

    override fun getUniqueId(): UUID {
        return uuid
    }

    override fun send(contents: Contents) {
        messenger.getInternalChannel().sendTo(contents, this)
    }

}