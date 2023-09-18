package club.plutomc.plutoproject.connector.impl

import club.plutomc.plutoproject.connector.api.Connection
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class SimpleRedisConnection(
    host: String,
    port: Int
) : Connection<Jedis> {

    private val pool: JedisPool

    init {
        val jedisPoolConfig = JedisPoolConfig()
        jedisPoolConfig.testOnCreate = true
        jedisPoolConfig.testOnBorrow = true
        jedisPoolConfig.testOnReturn = true

        pool = JedisPool(jedisPoolConfig, host, port)
    }

    override fun get(): Jedis = pool.resource

    override fun close() {
        pool.close()
    }

}