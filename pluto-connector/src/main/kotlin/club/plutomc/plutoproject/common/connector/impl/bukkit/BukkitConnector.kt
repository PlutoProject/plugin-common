package club.plutomc.plutoproject.common.connector.impl.bukkit

import club.plutomc.plutoproject.common.connector.api.Connector
import com.mongodb.client.MongoClient
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

class BukkitConnector(jedis: JedisPool, mongo: MongoClient) : Connector {

    private var _mongo: MongoClient
    private var _jedis: JedisPool

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