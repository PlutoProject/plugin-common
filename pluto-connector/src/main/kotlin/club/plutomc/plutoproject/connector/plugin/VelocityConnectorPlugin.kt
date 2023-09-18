package club.plutomc.plutoproject.connector.plugin

import club.plutomc.plutoproject.connector.api.ConnectionManager
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.nio.file.Path
import java.util.logging.Logger

@Suppress("UNUSED_PARAMETER")
@Plugin(
    id = "pluto-connector",
    name = "pluto-connector",
    version = "3.0.0-SNAPSHOT",
    authors = ["nostalfinals", "Members of PlutoProject"]
)
class VelocityConnectorPlugin @Inject constructor(server: ProxyServer, logger: Logger, @DataDirectory dataFolder: Path) {

    companion object {
        lateinit var logger: Logger
        private lateinit var server: ProxyServer
        private lateinit var dataFolder: File
        private lateinit var connectionManager: ConnectionManager
    }

    init {
        Companion.server = server
        Companion.logger = logger
        Companion.dataFolder = dataFolder.toFile()
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        if (server.pluginManager.getPlugin("pluto-connector").isEmpty) {
            return
        }

        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        val configFile = File(dataFolder, "settings.conf")

        if (!configFile.exists()) {
            configFile.createNewFile()
        }

        connectionManager = SharedMethods.platformConnectionManager(configFile)

        logger.info("Connector Velocity - Enabled")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        connectionManager.close()
        logger.info("Connector Velocity - Disabled")
    }

}