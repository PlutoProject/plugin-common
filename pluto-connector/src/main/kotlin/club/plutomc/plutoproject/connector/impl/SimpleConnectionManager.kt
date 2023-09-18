package club.plutomc.plutoproject.connector.impl

import club.plutomc.plutoproject.apiutils.data.concurrentHashMapOf
import club.plutomc.plutoproject.connector.api.Connection
import club.plutomc.plutoproject.connector.api.ConnectionManager
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.Closeable
import java.io.File

class SimpleConnectionManager(configFile: File) : ConnectionManager {

    private val connectionMap: MutableMap<String, Connection<*>> = concurrentHashMapOf()
    private val config = ConfigFactory.parseFile(configFile).resolve()

    override fun <T : Closeable> get(name: String): Connection<T> {
        if (!exist(name)) {
            throw RuntimeException("Connection doesn't exist!")
        }

        @Suppress("UNCHECKED_CAST")
        return checkNotNull(connectionMap[name]) as Connection<T>
    }

    override fun <T : Closeable> connect(
        name: String,
        action: (settingsSection: Map<String, Any>) -> T
    ): Connection<T> {
        if (!enabledInConfig(name)) {
            throw RuntimeException("Not enabled in config!")
        }

        return connect(name, action.invoke(config.getConfig("settings.$name").entrySet().associate { entry ->
            entry.key to entry.value.unwrapped()
        }))
    }

    override fun <T : Closeable> connect(name: String, obj: T): Connection<T> {
        val connection = SimpleConnection(obj)
        connectionMap[name] = connection
        return connection
    }

    override fun <T : Closeable> registerConnection(name: String, connection: Connection<T>) {
        connectionMap[name] = connection
    }

    override fun getSettingsSection(name: String): Config {
        return config.getConfig("settings.$name").resolve()
    }

    override fun exist(name: String): Boolean {
        return connectionMap.contains(name)
    }

    override fun disconnect(name: String) {
        if (!exist(name)) {
            return
        }

        connectionMap[name]!!.close()
    }

    override fun enabledInConfig(name: String): Boolean {
        return config.hasPath("settings.${name}.enabled") && config.getBoolean("settings.${name}.enabled")
    }

    override fun close() {
        connectionMap.forEach {
            it.value.close()
        }
    }

}