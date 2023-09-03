package club.plutomc.plutoproject.connector.api

import com.mongodb.client.MongoClient
import redis.clients.jedis.JedisPool

interface Connector {

    val jedis: JedisPool
    val mongo: MongoClient

    fun close()

}