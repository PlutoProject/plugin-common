package club.plutomc.plutoproject.connector.plugin

import club.plutomc.plutoproject.connector.api.Connector
import club.plutomc.plutoproject.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.connector.impl.BasicConnector
import com.google.gson.JsonParser
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import redis.clients.jedis.JedisPubSub
import java.nio.file.Path
import java.util.logging.Logger

@OptIn(DelicateCoroutinesApi::class)
@Suppress("UNUSED_PARAMETER")
@Plugin(
    id = "pluto-connector",
    name = "pluto-connector",
    version = "3.0.0-SNAPSHOT",
    authors = ["nostalfinals", "Members of PlutoProject"]
)
class VelocityConnectorPlugin @Inject constructor(server: ProxyServer, logger: Logger, @DataDirectory dataDir: Path) {

    companion object {
        lateinit var logger: Logger
        private lateinit var server: ProxyServer
        private lateinit var dataDir: Path
        private lateinit var connector: Connector
        private lateinit var receiveJob: Job
    }

    init {
        Companion.server = server
        Companion.logger = logger
        Companion.dataDir = dataDir
    }

    private fun receiveRequest() {
        receiveJob = GlobalScope.launch {
            connector.jedis.resource.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String?, message: String?) {
                    val nonNullMessage = checkNotNull(message)
                    val content = JsonParser.parseString(nonNullMessage).asJsonObject

                    if (content.get("type").asString != "request") {
                        return
                    }

                    content.remove("type")
                    content.addProperty("type", "response")
                    content.addProperty("mongo_host", checkNotNull(DatabaseUtils.getMongoHost()))
                    content.addProperty("mongo_port", checkNotNull(DatabaseUtils.getMongoPort()))
                    content.addProperty("mongo_username", checkNotNull(DatabaseUtils.getMongoUsername()))
                    content.addProperty("mongo_database", checkNotNull(DatabaseUtils.getMongoDatabase()))
                    content.addProperty("mongo_password", checkNotNull(DatabaseUtils.getMongoPassword()))

                    connector.jedis.resource.publish("connector_proxy", content.toString())

                    logger.info("A request was processed. $nonNullMessage")
                }
            }, "connector_bukkit")
        }
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        if (server.pluginManager.getPlugin("pluto-connector").isEmpty) {
            return
        }

        connector = BasicConnector(DatabaseUtils.createJedisPool(), DatabaseUtils.createMongoClient())
        ConnectorApiProvider.connector = connector
        receiveRequest()

        logger.info("Connector Velocity - Enabled")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        receiveJob.cancel()
        connector.close()
        logger.info("Connector Velocity - Disabled")
    }

}