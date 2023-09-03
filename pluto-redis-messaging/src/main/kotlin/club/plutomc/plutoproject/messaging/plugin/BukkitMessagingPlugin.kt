package club.plutomc.plutoproject.messaging.plugin

import club.plutomc.plutoproject.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.api.MessagingApiProvider
import club.plutomc.plutoproject.messaging.impl.bukkit.BukkitMessageManager
import club.plutomc.plutoproject.messaging.plugin.command.Command
import club.plutomc.plutoproject.messaging.plugin.command.BukkitCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

internal class BukkitMessagingPlugin : JavaPlugin() {

    companion object {
        private lateinit var _plugin: Plugin
        private lateinit var _manager: MessageManager
        private lateinit var _coroutineScope: CoroutineScope
        private lateinit var command: Command
        val plugin: Plugin
            get() = _plugin
        val coroutineScope: CoroutineScope
            get() = _coroutineScope
        val manager: MessageManager
            get() = _manager
    }

    override fun onEnable() {
        _plugin = this
        _coroutineScope = MainScope()
        _manager = BukkitMessageManager(ConnectorApiProvider.connector.jedis)
        command = BukkitCommand(this, _manager)
        command.registerCommandManager()
        command.register()
        MessagingApiProvider.manager = _manager
        logger.info("Redis Messaging Bukkit - Enabled")
    }

    override fun onDisable() {
        _manager.close()
        _coroutineScope.cancel()
        logger.info("Redis Messaging Bukkit - Disabled")
    }

}