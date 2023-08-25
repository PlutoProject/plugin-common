package club.plutomc.plutoproject.messaging.plugin

import club.plutomc.plutoproject.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.api.MessagingApiProvider
import club.plutomc.plutoproject.messaging.impl.velocity.VelocityMessageManager
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
    id = "pluto-redis-messaging",
    name = "pluto-redis-messaging",
    version = "3.0.0-SNAPSHOT",
    authors = ["nostalfinals", "Members of PlutoProject"]
)
class VelocityMessagingPlugin @Inject constructor(server: ProxyServer, logger: Logger, @DataDirectory dataDir: Path) {

    companion object {
        private lateinit var logger: Logger
        private lateinit var server: ProxyServer
        private lateinit var dataDir: Path
        private lateinit var manager: MessageManager
    }

    init {
        Companion.server = server
        Companion.logger = logger
        Companion.dataDir = dataDir
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        if (server.pluginManager.getPlugin("pluto-redis-messaging").isEmpty) {
            return
        }

        manager = VelocityMessageManager(ConnectorApiProvider.connector.jedis)
        MessagingApiProvider.manager = manager

        logger.info("Redis Messaging Velocity - Enabled")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        manager.close()
        logger.info("Redis Messaging Velocity - Disabled")
    }

}