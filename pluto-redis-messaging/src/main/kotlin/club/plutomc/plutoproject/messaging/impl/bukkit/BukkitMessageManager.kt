package club.plutomc.plutoproject.messaging.impl.bukkit

import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.MessageManager
import redis.clients.jedis.JedisPool

class BukkitMessageManager(jedis: JedisPool): MessageManager {
    override fun register(channel: String) {
        TODO("Not yet implemented")
    }

    override fun get(channel: String): Channel {
        TODO("Not yet implemented")
    }

    override fun exist(channel: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun unregister(channel: String) {
        TODO("Not yet implemented")
    }

    override fun unregister(channel: Channel) {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}