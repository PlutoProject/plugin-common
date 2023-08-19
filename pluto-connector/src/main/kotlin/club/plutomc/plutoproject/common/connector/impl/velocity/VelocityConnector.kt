package club.plutomc.plutoproject.common.connector.impl.velocity

import club.plutomc.plutoproject.common.connector.api.Connector
import com.mongodb.client.MongoClient
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

class VelocityConnector(jedis: JedisPool, mongo: MongoClient) : Connector {

    private var _jedis: JedisPool
    private var _mongo: MongoClient

    override val jedis: Jedis
        get() = _jedis.resource
    override val mongo: MongoClient
        get() = _mongo

    override fun close() {
        _jedis.close()
        _mongo.close()
    }

    init {
        this._jedis = jedis
        this._mongo = mongo
    }

}