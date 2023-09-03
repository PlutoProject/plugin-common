group = "club.plutomc.plutoproject.common.runtime"
version = "3.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin language dependencies
    api(kotlin("stdlib-jdk8", "1.9.0"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Java libraries
    api("commons-io:commons-io:2.13.0")
    api("com.google.guava:guava:32.1.2-jre")
    api("com.google.code.gson:gson:2.10.1")
    api("redis.clients:jedis:4.4.3")
    api("com.google.code.gson:gson:2.10.1")

    // Database drivers
    api("redis.clients:jedis:4.4.3")
    api("org.mongodb:mongodb-driver-sync:4.10.2")

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    api("cloud.commandframework:cloud-paper:1.8.3")

    // Velocity
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    kapt("com.velocitypowered:velocity-api:3.1.1")
    api("cloud.commandframework:cloud-velocity:1.8.3")

    // api-utils
    api(project(":pluto-api-utils"))
}