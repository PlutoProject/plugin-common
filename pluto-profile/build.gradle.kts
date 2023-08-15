group = "club.plutomc.plutoproject.common.profile"
version = "3.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    dependencies {
        // MongoDB
        api("dev.morphia.morphia:morphia-core:2.3.0")
        api("org.mongodb:mongodb-driver-sync:4.10.2")

        // Redis
        api("redis.clients:jedis:4.4.3")

        // Caffeine cache
        api("com.github.ben-manes.caffeine:caffeine:3.1.8")

        // Paper
        compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    }
}