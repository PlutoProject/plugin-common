package club.plutomc.plutoproject.connector.plugin

import club.plutomc.plutoproject.connector.api.ConnectionManager
import club.plutomc.plutoproject.connector.api.DefaultConnections
import club.plutomc.plutoproject.connector.impl.SimpleConnectionManager
import club.plutomc.plutoproject.connector.impl.SimpleMongoConnection
import club.plutomc.plutoproject.connector.impl.SimpleRedisConnection
import java.io.File

object SharedMethods {

    fun platformConnectionManager(configFile: File): ConnectionManager {
        val connectionManager = SimpleConnectionManager(configFile)

        if (connectionManager.enabledInConfig("defaults.mongo")) {
            val config = connectionManager.getSettingsSection("defaults.mongo")

            val host = config.getString("host")
            val port = config.getInt("port")
            val database = config.getString("database")
            val username = config.getString("username")
            val password = config.getString("password")

            val mongo = SimpleMongoConnection(host, port, database, username, password)
            connectionManager.registerConnection("defaultMongo", mongo)
            DefaultConnections.internalMongo = mongo
        }

        if (connectionManager.enabledInConfig("defaults.redis")) {
            val config = connectionManager.getSettingsSection("defaults.redis").resolve()

            val host = config.getString("host")
            val port = config.getInt("port")

            val redis = SimpleRedisConnection(host, port)
            connectionManager.registerConnection("defaultRedis", redis)
            DefaultConnections.internalRedis = redis
        }

        return connectionManager
    }

}