pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "plugin-common"
include("pluto-connector")
include("pluto-runtime")
include("pluto-profile")
include("pluto-profile:api")
findProject(":pluto-profile:api")?.name = "api"
include("pluto-profile:impl")
findProject(":pluto-profile:impl")?.name = "impl"
include("pluto-profile:bukkit")
findProject(":pluto-profile:bukkit")?.name = "bukkit"
