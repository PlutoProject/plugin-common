package club.plutomc.plutoproject.common.connector.api

import com.mongodb.client.MongoClient
import redis.clients.jedis.Jedis

interface Connector {

    val jedis: Jedis
    val mongo: MongoClient

    fun close()

}