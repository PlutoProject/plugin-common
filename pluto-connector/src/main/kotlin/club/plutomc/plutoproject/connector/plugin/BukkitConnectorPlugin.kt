package club.plutomc.plutoproject.connector.plugin

import club.plutomc.plutoproject.connector.api.Connector
import club.plutomc.plutoproject.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.connector.impl.BasicConnector
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import kotlinx.coroutines.*
import org.bson.UuidRepresentation
import org.bukkit.plugin.java.JavaPlugin
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class BukkitConnectorPlugin : JavaPlugin() {

    companion object {
        private lateinit var connector: Connector
    }

    override fun onEnable() {
        val jedis = DatabaseUtils.createJedisPool()
        val mongo = request(jedis)

        connector = BasicConnector(jedis, mongo)
        ConnectorApiProvider.connector = connector

        logger.info("Connector Bukkit - Enabled")
    }

    override fun onDisable() {
        connector.close()
        logger.info("Connector Bukkit - Disabled")
    }

    private fun request(jedis: JedisPool): MongoClient {
        lateinit var mongo: MongoClient
        var received = false
        val id = UUID.randomUUID()

        val requestContent = JsonObject()

        requestContent.addProperty("id", id.toString())
        requestContent.addProperty("type", "request")

        val receiveJob = GlobalScope.launch {
            jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val nonNullMessage = checkNotNull(message)
                    val resultContent = JsonParser.parseString(nonNullMessage).asJsonObject

                    if (resultContent.get("id").asString != id.toString()) {
                        return
                    }

                    if (resultContent.get("type").asString != "response") {
                        return
                    }

                    val host = resultContent.get("mongo_host").asString
                    val port = resultContent.get("mongo_port").asString
                    val username = resultContent.get("mongo_username").asString
                    val database = resultContent.get("mongo_database").asString
                    val password = resultContent.get("mongo_password").asString

                    val connectionString = ConnectionString("mongodb://$host:$port")

                    val credentials = MongoCredential.createCredential(
                        username,
                        database,
                        password.toCharArray()
                    )

                    val settings = MongoClientSettings.builder()
                        .uuidRepresentation(UuidRepresentation.STANDARD)
                        .applyConnectionString(connectionString)
                        .credential(credentials)
                        .build()

                    mongo = MongoClients.create(settings)
                    unsubscribe()
                }
            }, "connector_proxy")
        }

        runBlocking {
            delay(3000L)
        }

        val requestJob = GlobalScope.launch {
            while (!received) {
                jedis.resource.publish("connector_bukkit", requestContent.toString())
                delay(5000L)
            }
        }

        runBlocking {
            receiveJob.join()
        }

        received = true
        requestJob.cancel()

        return mongo
    }

}