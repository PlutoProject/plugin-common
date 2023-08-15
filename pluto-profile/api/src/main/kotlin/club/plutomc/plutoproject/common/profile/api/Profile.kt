package club.plutomc.plutoproject.common.profile.api

import java.util.*

interface Profile {

    val uuid: UUID

    var name: String

    val createdTime: Long

    val properties: MutableMap<String, Any>

    fun isInitialized(): Boolean

}