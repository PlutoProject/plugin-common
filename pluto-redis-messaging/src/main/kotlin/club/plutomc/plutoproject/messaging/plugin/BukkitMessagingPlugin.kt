package club.plutomc.plutoproject.messaging.plugin

import club.plutomc.plutoproject.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.api.MessagingApiProvider
import club.plutomc.plutoproject.messaging.impl.bukkit.BukkitMessageManager
import org.bukkit.plugin.java.JavaPlugin

class BukkitMessagingPlugin: JavaPlugin() {

    companion object {
        private lateinit var manager: MessageManager
    }

    override fun onEnable() {
        manager = BukkitMessageManager(ConnectorApiProvider.connector.jedis)
        MessagingApiProvider.manager = manager
        logger.info("Redis Messaging Bukkit - Enabled")
    }

    override fun onDisable() {
        manager.close()
        logger.info("Redis Messaging Bukkit - Disabled")
    }

}