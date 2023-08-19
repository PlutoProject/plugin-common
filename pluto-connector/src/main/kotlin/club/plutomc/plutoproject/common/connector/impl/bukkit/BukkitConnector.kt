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

class BukkitConnector(jedis: JedisPool): Connector {

    companion object {
        private fun generateRequest(): String {
            val jsonObject = JsonObject()
            jsonObject.add("request_type", JsonParser.parseString("mongo"))

            return jsonObject.toString()
        }
    }

    private lateinit var _mongo: MongoClient
    private var _jedis: JedisPool

    override val jedis: Jedis
        get() = _jedis.resource

    override val mongo: MongoClient
        get() = _mongo

    init {
        this._jedis = jedis
        this.jedis.publish("connector", generateRequest())

        this.jedis.subscribe(object : JedisPubSub() {
            override fun onMessage(channel: String?, message: String?) {
                val nonNullMessage = checkNotNull(message)
                val jsonObject = JsonParser.parseString(nonNullMessage).asJsonObject

                val connectionString = ConnectionString(jsonObject.get("connection_string").asString)
                val username = jsonObject.get("username").asString
                val database = jsonObject.get("database").asString
                val password = jsonObject.get("password").asString

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
            }
        }, "connector")
    }

}