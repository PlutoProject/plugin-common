package club.plutomc.plutoproject.common.connector.api

import com.mongodb.client.MongoClient
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

interface Connector {

    val jedis: JedisPool
    val mongo: MongoClient

    fun close()

}