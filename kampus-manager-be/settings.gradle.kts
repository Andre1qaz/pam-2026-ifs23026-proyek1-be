val ktor_version    = "3.1.3"
val kotlin_version  = "2.1.20"
val logback_version = "1.5.18"
val exposed_version = "0.61.0"
val koin_version    = "4.1.0-Beta5"

plugins {
    kotlin("jvm")                      version "2.1.20"
    id("io.ktor.plugin")               version "3.1.3"
    kotlin("plugin.serialization")     version "2.1.20"
}

group   = "org.delcom.kampusmanager"
version = "1.0.0"

// ✅ FIX: SAMAKAN JAVA KE 23
java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

// ✅ FIX: paksa Java compile ke 23
tasks.withType<JavaCompile>().configureEach {
    options.release.set(23)
}

// ✅ FIX: paksa Kotlin ke 23
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "23"
    }
}

application {
    mainClass = "org.delcom.kampusmanager.ApplicationKt"
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")
    implementation("org.postgresql:postgresql:42.7.5")

    // DI
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    // Auth
    implementation("com.auth0:java-jwt:4.5.0")
    implementation("org.mindrot:jbcrypt:0.4")

    // Config
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.2")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Test
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}