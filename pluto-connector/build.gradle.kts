group = "club.plutomc.plutoproject.common.connector"
version = "3.0.0-SNAPSHOT"

plugins {
    id("maven-publish")
}

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

publishing {
    configure<PublishingExtension> {
        publications.create<MavenPublication>(name) {
            from(components["kotlin"])

            groupId = "club.plutomc.plutoproject.common"
            artifactId = "connector"
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            val releaseRepo = "https://nexus.nostaldaisuki.cc/repository/maven-releases/"
            val snapshotRepo = "https://nexus.nostaldaisuki.cc/repository/maven-snapshots/"

            url = uri(if (project.version.toString().endsWith("SNAPSHOT")) snapshotRepo else releaseRepo)

            credentials {
                username = rootProject.properties["nostalRepoUsername"].toString()
                password = rootProject.properties["nostalRepoPassword"].toString()
            }
        }
    }
}