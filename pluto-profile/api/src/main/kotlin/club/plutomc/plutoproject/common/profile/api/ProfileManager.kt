package club.plutomc.plutoproject.common.profile.api

import java.util.*

interface ProfileManager {

    fun get(uuid: UUID): Profile

    fun contains(uuid: UUID): Boolean

    fun save(profile: Profile)

    fun destroy(uuid: UUID)

    fun addSubProfile(root: Profile, sub: Profile)

    fun addSubProfile(root: UUID, sub: Profile)

    fun removeSubProfile(root: Profile, sub: UUID)

    fun removeSubProfile(root: UUID, sub: UUID)

    fun isSubProfile(profile: Profile): Boolean

    fun removeCache(profile: Profile)

    fun removeCache(uuid: UUID)

    fun close()

}