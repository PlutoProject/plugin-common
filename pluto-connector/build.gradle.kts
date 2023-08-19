group = "club.plutomc.plutoproject.common.connector"
version = "3.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.code.gson:gson:2.10.1")

    // Database drivers
    api("redis.clients:jedis:4.4.3")
    api("org.mongodb:mongodb-driver-sync:4.10.2")

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    // Velocity
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    kapt("com.velocitypowered:velocity-api:3.1.1")
}