group = "club.plutomc.plutoproject.framework.messaging"
version = "3.0.0-SNAPSHOT"

plugins {
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":pluto-connector"))
    compileOnly(project(":pluto-api-utils"))
}

publishing {
    configure<PublishingExtension> {
        publications.create<MavenPublication>(name) {
            from(components["kotlin"])

            groupId = "club.plutomc.plutoproject"
            artifactId = "messaging"
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