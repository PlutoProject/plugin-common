group = "club.plutomc.plutoproject.common.connector"
version = "3.0.0-SNAPSHOT"

plugins {
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    // Velocity
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    kapt("com.velocitypowered:velocity-api:3.1.1")

    // Java libraries
    compileOnly("commons-io:commons-io:2.13.0")
    compileOnly("com.google.guava:guava:32.1.2-jre")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("redis.clients:jedis:4.4.3")
    compileOnly("com.google.code.gson:gson:2.10.1")

    // Database drivers
    compileOnly("redis.clients:jedis:4.4.3")
    compileOnly("org.mongodb:mongodb-driver-sync:4.10.2")
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