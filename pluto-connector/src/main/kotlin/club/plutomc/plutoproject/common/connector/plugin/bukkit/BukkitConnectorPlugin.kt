package club.plutomc.plutoproject.common.connector.plugin.bukkit

import club.plutomc.plutoproject.common.connector.api.Connector
import club.plutomc.plutoproject.common.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.common.connector.impl.bukkit.BukkitConnector
import club.plutomc.plutoproject.common.connector.plugin.DatabaseUtils
import org.bukkit.plugin.java.JavaPlugin

class BukkitConnectorPlugin : JavaPlugin() {

    companion object {
        private lateinit var connector: Connector
    }

    override fun onEnable() {
        connector = BukkitConnector(DatabaseUtils.createJedisPool())
        ConnectorApiProvider.connector = connector

        logger.info("Connector Bukkit - Enabled")
    }

    override fun onDisable() {
        connector.close()
        logger.info("Connector Bukkit - Disabled")
    }

}