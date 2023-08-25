package club.plutomc.plutoproject.connector.plugin

import club.plutomc.plutoproject.connector.api.Connector
import club.plutomc.plutoproject.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.connector.impl.bukkit.BukkitConnector
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.client.MongoClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.plugin.java.JavaPlugin
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.*

class BukkitConnectorPlugin : JavaPlugin() {

    companion object {
        private lateinit var connector: Connector
        private val requestIds: MutableList<String> = mutableListOf()

        private fun generateRequest(): String {
            val requestObject = JsonObject()
            val id = UUID.randomUUID().toString()

            requestObject.addProperty("type", "mongo")
            requestObject.addProperty("id", id)
            requestIds.add(id)

            return requestObject.toString()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEnable() {
        val redis: JedisPool = DatabaseUtils.createJedisPool()
        var mongo: MongoClient? = null

        val publishJob = GlobalScope.launch {
            while (true) {
                redis.resource.publish("connector_bukkit", generateRequest())
                delay(5000)
            }
        }

        var requestProcessed = false

        redis.resource.subscribe(object : JedisPubSub() {
            override fun onMessage(channel: String?, message: String?) {
                if (requestProcessed) {
                    return
                }

                val nonNullMessage = checkNotNull(message)
                val resultObject = JsonParser.parseString(nonNullMessage).asJsonObject

                val id = resultObject.get("id").asString

                if (!requestIds.contains(id)) {
                    return
                }

                publishJob.cancel()

                val connectionString = resultObject.get("connection_string").asString
                val username = resultObject.get("username").asString
                val database = resultObject.get("database").asString
                val password = resultObject.get("password").asString

                mongo = DatabaseUtils.createMongoClient(connectionString, username, database, password)
                requestIds.clear()
                unsubscribe()
                requestProcessed = true
            }
        }, "connector_proxy")

        connector = BukkitConnector(DatabaseUtils.createJedisPool(), checkNotNull(mongo))
        ConnectorApiProvider.connector = connector

        logger.info("Connector Bukkit - Enabled")
    }

    override fun onDisable() {
        connector.close()
        logger.info("Connector Bukkit - Disabled")
    }

}