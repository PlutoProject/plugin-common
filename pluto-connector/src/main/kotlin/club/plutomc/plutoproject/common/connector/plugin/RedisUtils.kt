package club.plutomc.plutoproject.common.connector.plugin

object RedisUtils {

    private fun getInformation(property: String, env: String): String? {
        var information = System.getProperty(property)

        if (information != null) {
            return information
        }

        information = System.getenv(env)

        if (information != null) {
            return information
        }

        return null
    }

    internal fun getRedisHost(): String? {
        return getInformation("redisHost", "REDIS_HOST")
    }

    internal fun getRedisPort(): String? {
        return getInformation("redisPort", "REDIS_PORT")
    }

    internal fun getRedisUsername(): String? {
        return getInformation("redisUsername", "REDIS_USERNAME")
    }

    internal fun getRedisPassword(): String? {
        return getInformation("redisPassword", "REDIS_PASSWORD")
    }

}