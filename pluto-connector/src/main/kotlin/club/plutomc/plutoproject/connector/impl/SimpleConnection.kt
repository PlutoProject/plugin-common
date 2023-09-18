package club.plutomc.plutoproject.connector.impl

import club.plutomc.plutoproject.connector.api.Connection
import java.io.Closeable

class SimpleConnection<T : Closeable>(private val obj: T) : Connection<T> {

    override fun get(): T = obj

    override fun close() {
        obj.close()
    }

}