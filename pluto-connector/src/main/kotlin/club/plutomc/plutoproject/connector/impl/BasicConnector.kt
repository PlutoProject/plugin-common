package club.plutomc.plutoproject.connector.impl

import club.plutomc.plutoproject.connector.api.Connector
import com.mongodb.client.MongoClient
import redis.clients.jedis.JedisPool

class BasicConnector(jedis: JedisPool, mongo: MongoClient) : Connector {

    private var _jedis: JedisPool
    private var _mongo: MongoClient

    override val jedis: JedisPool
        get() = _jedis
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