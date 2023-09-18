package club.plutomc.plutoproject.connector.api

import com.mongodb.client.MongoClient
import redis.clients.jedis.Jedis

object DefaultConnections {

    internal var internalMongo: Connection<MongoClient>? = null
    internal var internalRedis: Connection<Jedis>? = null
    val mongo: MongoClient
        get() = checkNotNull(internalMongo).get()
    val redis: Jedis
        get() = checkNotNull(internalRedis).get()

}