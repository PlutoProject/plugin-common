package club.plutomc.plutoproject.common.connector.plugin.velocity

import club.plutomc.plutoproject.common.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.common.connector.plugin.DatabaseUtils
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import redis.clients.jedis.JedisPubSub

@OptIn(DelicateCoroutinesApi::class)
internal class RequestProcessor {

    companion object {
        fun process() {
            ConnectorApiProvider.connector.jedis.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val nonNullMessage = checkNotNull(message)
                    val requestObject = JsonParser.parseString(nonNullMessage).asJsonObject

                    val requestType = requestObject.get("type").asString
                    val id = requestObject.get("id").asString

                    if (requestType == "mongo") {
                        val resultObject = JsonObject()

                        val mongoHost = checkNotNull(DatabaseUtils.getMongoHost())
                        val mongoPort = checkNotNull(DatabaseUtils.getMongoPort())
                        val mongoUsername = checkNotNull(DatabaseUtils.getMongoUsername())
                        val mongoDatabase = checkNotNull(DatabaseUtils.getMongoDatabase())
                        val mongoPassword = checkNotNull(DatabaseUtils.getMongoPassword())

                        resultObject.add("id", JsonParser.parseString(id))
                        resultObject.addProperty("connection_string", "mongodb://$mongoHost:$mongoPort")
                        resultObject.addProperty("username", mongoUsername)
                        resultObject.addProperty("database", mongoDatabase)
                        resultObject.addProperty("password", mongoPassword)

                        ConnectorApiProvider.connector.jedis.publish("connector_proxy", resultObject.toString())
                        VelocityConnectorPlugin.logger.info("A connection request was processed. $nonNullMessage")
                    }
                }
            }, "connector_bukkit")
        }
    }

    private var job: Job = GlobalScope.launch {
        while (true) {
            process()
        }
    }

    fun stop() {
        job.cancel()
    }

}