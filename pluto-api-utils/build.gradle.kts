group = "club.plutomc.plutoproject.apiutils"
version = "3.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    // Velocity
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    kapt("com.velocitypowered:velocity-api:3.1.1")
}