package club.plutomc.plutoproject.common.connector.plugin

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

internal object DatabaseUtils {

    private fun getInformation(property: String, env: String): String? {
        var information = System.getProperty(property)

        if (information != null) {
            return information
        }

        information = System.getenv(env)

        if (information != null) {
            return information
        }

        return null
    }

    private fun getRedisHost(): String? {
        return getInformation("redisHost", "REDIS_HOST")
    }

    private fun getRedisPort(): String? {
        return getInformation("redisPort", "REDIS_PORT")
    }

    internal fun createJedisPool(): JedisPool {
        val jedisHost = checkNotNull(DatabaseUtils.getRedisHost())
        val jedisPort = checkNotNull(DatabaseUtils.getRedisPort()?.toInt())

        val jedisPoolConfig = JedisPoolConfig()
        jedisPoolConfig.testOnCreate = true
        jedisPoolConfig.testOnBorrow = true
        jedisPoolConfig.testOnReturn = true

        return JedisPool(jedisPoolConfig, jedisHost, jedisPort)
    }

    internal fun getMongoHost(): String? {
        return getInformation("mongoHost", "MONGO_HOST")
    }

    internal fun getMongoPort(): String? {
        return getInformation("mongoPort", "MONGO_PORT")
    }

    internal fun getMongoUsername(): String? {
        return getInformation("mongoUsername", "MONGO_USERNAME")
    }

    internal fun getMongoDatabase(): String? {
        return getInformation("mongoDatabase", "MONGO_DATABASE")
    }

    internal fun getMongoPassword(): String? {
        return getInformation("mongoPassword", "MONGO_Password")
    }

}