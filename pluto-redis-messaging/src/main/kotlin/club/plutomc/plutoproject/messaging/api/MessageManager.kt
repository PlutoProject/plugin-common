package club.plutomc.plutoproject.messaging.api

import java.io.Closeable

interface MessageManager : Closeable {

    fun register(channel: String)

    fun get(channel: String): Channel

    fun exist(channel: String): Boolean

    fun unregister(channel: String)

    fun unregister(channel: Channel)

}