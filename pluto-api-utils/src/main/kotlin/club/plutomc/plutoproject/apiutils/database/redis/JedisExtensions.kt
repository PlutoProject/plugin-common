package club.plutomc.plutoproject.apiutils.database.redis

import club.plutomc.plutoproject.apiutils.json.toObject
import club.plutomc.plutoproject.apiutils.json.toJsonString
import redis.clients.jedis.Jedis

fun Jedis.setObject(key: String, value: Any) {
    this.set(key, value.toJsonString())
}

inline fun <reified T> Jedis.getObject(key: String): T {
    return key.toObject()
}