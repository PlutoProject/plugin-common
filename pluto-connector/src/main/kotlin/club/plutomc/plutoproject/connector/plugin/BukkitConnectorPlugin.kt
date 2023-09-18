package club.plutomc.plutoproject.connector.plugin

import club.plutomc.plutoproject.connector.api.ConnectionManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class BukkitConnectorPlugin : JavaPlugin() {

    companion object {
        private lateinit var connectionManager: ConnectionManager
    }

    override fun onEnable() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        val configFile = File(dataFolder, "settings.conf")

        if (!configFile.exists()) {
            configFile.createNewFile()
        }

        connectionManager = SharedMethods.platformConnectionManager(configFile)

        logger.info("Connector Bukkit - Enabled")
    }

    override fun onDisable() {
        connectionManager.close()
        logger.info("Connector Bukkit - Disabled")
    }

}