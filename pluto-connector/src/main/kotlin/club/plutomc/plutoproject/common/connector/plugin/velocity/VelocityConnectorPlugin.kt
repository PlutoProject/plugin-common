package club.plutomc.plutoproject.common.connector.plugin.velocity

import club.plutomc.plutoproject.common.connector.api.Connector
import club.plutomc.plutoproject.common.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.common.connector.impl.bukkit.BukkitConnector
import club.plutomc.plutoproject.common.connector.plugin.DatabaseUtils
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path
import java.util.logging.Logger
import javax.inject.Inject

@Suppress("UNUSED_PARAMETER")
@Plugin(
    id = "pluto-connector",
    name = "pluto-connector",
    version = "3.0.0-SNAPSHOT",
    authors = ["nostalfinals", "Members of PlutoProject"]
)
class VelocityConnectorPlugin @Inject constructor(server: ProxyServer, logger: Logger, @DataDirectory dataDir: Path) {

    companion object {
        private lateinit var server: ProxyServer
        private lateinit var logger: Logger
        private lateinit var dataDir: Path
        private lateinit var connector: Connector
        private lateinit var processor: RequestProcessor
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

        connector = BukkitConnector(DatabaseUtils.createJedisPool())
        ConnectorApiProvider.connector = connector
        processor = RequestProcessor()

        logger.info("Connector Bukkit - Enabled")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        connector.close()
        processor.stop()

        logger.info("Connector Bukkit - Disabled")
    }

}