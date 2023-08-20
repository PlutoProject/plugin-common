package club.plutomc.plutoproject.common.connector.impl.bukkit

import club.plutomc.plutoproject.common.connector.api.Connector
import club.plutomc.plutoproject.common.connector.plugin.DatabaseUtils
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.client.MongoClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class BukkitConnector(jedis: JedisPool) : Connector {

    companion object {
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
        val publishJob = GlobalScope.launch {
            while (true) {
                _jedis.resource.publish("connector_bukkit", generateRequest())
                delay(5000)
            }
        }

        _jedis.resource.subscribe(object : JedisPubSub() {
            override fun onMessage(channel: String?, message: String?) {
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

                _mongo = DatabaseUtils.createMongoClient(connectionString, username, database, password)
                requestIds.clear()
            }
        }, "connector_proxy")
    }

}