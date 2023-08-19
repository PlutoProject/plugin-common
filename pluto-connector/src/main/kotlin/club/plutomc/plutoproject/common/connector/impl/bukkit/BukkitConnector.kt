package club.plutomc.plutoproject.common.connector.impl.bukkit

import club.plutomc.plutoproject.common.connector.api.Connector
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.UuidRepresentation
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.*

class BukkitConnector(jedis: JedisPool) : Connector {

    companion object {
        private val requestIds: MutableList<String> = mutableListOf()

        private fun generateRequest(): String {
            val requestObject = JsonObject()
            val id = UUID.randomUUID().toString()

            requestObject.add("type", JsonParser.parseString("mongo"))
            requestObject.add("id", JsonParser.parseString(id))
            requestIds.add(id)

            return requestObject.toString()
        }
    }

    private lateinit var _mongo: MongoClient
    private var _jedis: JedisPool

    override val jedis: Jedis
        get() = _jedis.resource

    override val mongo: MongoClient
        get() = _mongo

    override fun close() {
        _jedis.close()
        _mongo.close()
    }

    init {
        this._jedis = jedis
        this.jedis.publish("connector", generateRequest())

        this.jedis.subscribe(object : JedisPubSub() {
            override fun onMessage(channel: String?, message: String?) {
                val nonNullMessage = checkNotNull(message)
                val resultObject = JsonParser.parseString(nonNullMessage).asJsonObject

                val id = resultObject.get("id").asString

                if (!requestIds.contains(id)) {
                    return
                }

                val connectionString = ConnectionString(resultObject.get("connection_string").asString)
                val username = resultObject.get("username").asString
                val database = resultObject.get("database").asString
                val password = resultObject.get("password").asString

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

                _mongo = MongoClients.create(settings)
                requestIds.remove(id)
            }
        }, "connector")
    }

}