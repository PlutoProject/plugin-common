package club.plutomc.plutoproject.common.connector.plugin.bukkit

import club.plutomc.plutoproject.common.connector.api.Connector
import club.plutomc.plutoproject.common.connector.api.ConnectorApiProvider
import club.plutomc.plutoproject.common.connector.api.bukkit.BukkitConnector
import club.plutomc.plutoproject.common.connector.plugin.RedisUtils
import org.bukkit.plugin.java.JavaPlugin
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class BukkitConnectorPlugin: JavaPlugin() {

    companion object {
        private lateinit var instance: JavaPlugin
        private lateinit var connector: Connector
    }

    override fun onEnable() {
        instance = this
        logger.info("Connector Bukkit - Enabled")

        val jedisHost = checkNotNull(RedisUtils.getRedisHost())
        val jedisPort = checkNotNull(RedisUtils.getRedisPort()?.toInt())
        val jedisPassword = checkNotNull(RedisUtils.getRedisPassword())

        val jedisPoolConfig = JedisPoolConfig()
        jedisPoolConfig.testOnCreate = true
        jedisPoolConfig.testOnBorrow = true
        jedisPoolConfig.testOnReturn = true

        val jedisPool = JedisPool(jedisPoolConfig, jedisHost, jedisPort, 2000, jedisPassword)
        connector = BukkitConnector(jedisPool)

        ConnectorApiProvider.connector = connector
    }

    override fun onDisable() {
        connector.mongo.close()
        connector.jedis.close()

        logger.info("Connector Bukkit - Disabled")
    }

}