package club.plutomc.plutoproject.connector.plugin

import club.plutomc.plutoproject.connector.api.Connector
import club.plutomc.plutoproject.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.connector.impl.BasicConnector
import org.bukkit.plugin.java.JavaPlugin

class BukkitConnectorPlugin : JavaPlugin() {

    companion object {
        private lateinit var connector: Connector
    }

    override fun onEnable() {
        val jedis = DatabaseUtils.createJedisPool()
        val mongo = DatabaseUtils.createMongoClient()

        connector = BasicConnector(jedis, mongo)
        ConnectorApiProvider.connector = connector

        logger.info("Connector Bukkit - Enabled")
    }

    override fun onDisable() {
        connector.close()
        logger.info("Connector Bukkit - Disabled")
    }

}