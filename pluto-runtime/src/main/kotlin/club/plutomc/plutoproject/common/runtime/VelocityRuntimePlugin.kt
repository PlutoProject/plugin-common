package club.plutomc.plutoproject.common.connector.plugin.velocity

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
    id = "pluto-runtime",
    name = "pluto-runtime",
    version = "3.0.0-SNAPSHOT",
    authors = ["nostalfinals", "Members of PlutoProject"]
)
class VelocityRuntimePlugin @Inject constructor(server: ProxyServer, logger: Logger, @DataDirectory dataDir: Path) {

    companion object {
        lateinit var logger: Logger
        private lateinit var server: ProxyServer
        private lateinit var dataDir: Path
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
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {

    }

}