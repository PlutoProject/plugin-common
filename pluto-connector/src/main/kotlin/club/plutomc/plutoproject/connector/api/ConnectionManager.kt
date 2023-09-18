package club.plutomc.plutoproject.connector.api

import com.typesafe.config.Config
import java.io.Closeable

interface ConnectionManager {

    fun <T : Closeable> get(name: String): Connection<T>

    fun <T : Closeable> connect(name: String, action: (settingsSection: Map<String, Any>) -> T): Connection<T>

    fun <T : Closeable> connect(name: String, obj: T): Connection<T>

    fun <T : Closeable> registerConnection(name: String, connection: Connection<T>)

    fun getSettingsSection(name: String): Config

    fun exist(name: String): Boolean

    fun disconnect(name: String)

    fun enabledInConfig(name: String): Boolean

    fun close()

}