package club.plutomc.plutoproject.common.connector.plugin

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.UuidRepresentation
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

    internal fun createJedisPool(
        host: String = checkNotNull(getRedisHost()),
        port: String = checkNotNull(getRedisPort())
    ): JedisPool {
        val jedisPoolConfig = JedisPoolConfig()
        jedisPoolConfig.testOnCreate = true
        jedisPoolConfig.testOnBorrow = true
        jedisPoolConfig.testOnReturn = true

        return JedisPool(jedisPoolConfig, host, port.toInt())
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

    internal fun createMongoClient(
        connectionStr: String = "mongodb://${checkNotNull(getMongoHost())}:${checkNotNull(getMongoPort())}",
        username: String = checkNotNull(getMongoUsername()),
        database: String = checkNotNull(getMongoDatabase()),
        password: String = checkNotNull(getMongoPassword())
    ): MongoClient {
        val connectionString = ConnectionString(connectionStr)
        val credentials = MongoCredential.createCredential(
            username,
            database,
            password.toCharArray()
        )

        val settings = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(connectionString)
            .credential(credentials)
            .build()

        return MongoClients.create(settings)
    }

}