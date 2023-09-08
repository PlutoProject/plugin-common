package club.plutomc.plutoproject.connector.plugin

import club.plutomc.plutoproject.connector.api.Connector
import club.plutomc.plutoproject.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.connector.impl.BasicConnector
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path
import java.util.logging.Logger

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
    }

    init {
        Companion.server = server
        Companion.logger = logger
        Companion.dataDir = dataDir
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        if (server.pluginManager.getPlugin("pluto-connector").isEmpty) {
            return
        }

        connector = BasicConnector(DatabaseUtils.createJedisPool(), DatabaseUtils.createMongoClient())
        ConnectorApiProvider.connector = connector

        logger.info("Connector Velocity - Enabled")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        connector.close()
        logger.info("Connector Velocity - Disabled")
    }

}