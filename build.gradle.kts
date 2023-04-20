plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("org.jetbrains.compose") version "1.3.1"
}

group = "com.cramsan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

val ktor_version: String by project

dependencies {
    implementation("com.github.twitch4j:twitch4j:1.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.0-Beta")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-java:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-core:1.4.6")
    implementation("ch.qos.logback:logback-classic:1.4.6")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    implementation(compose.desktop.currentOs)

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

/*
application {
    mainClass.set("MainKt")
}
 */

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}