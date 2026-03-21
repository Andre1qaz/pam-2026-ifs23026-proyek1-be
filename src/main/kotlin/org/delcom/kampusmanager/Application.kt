package org.delcom.kampusmanager

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.delcom.kampusmanager.config.JwtConfig
import org.delcom.kampusmanager.config.configureDatabase
import org.delcom.kampusmanager.di.appModule
import org.koin.ktor.plugin.Koin
import java.io.File

fun main(args: Array<String>) {
    loadEnv()
    EngineMain.main(args)
}

/**
 * Cari .env di beberapa lokasi yang mungkin, supaya tidak
 * bergantung pada working directory IntelliJ / terminal.
 */
private fun loadEnv() {
    val searchDirs = listOf(
        ".",                                        // working dir saat ini
        System.getProperty("user.dir") ?: ".",      // user.dir JVM
        File(System.getProperty("user.dir") ?: ".").parent ?: ".",  // parent folder
    )

    var loaded = false
    for (dir in searchDirs) {
        val envFile = File(dir, ".env")
        if (envFile.exists()) {
            try {
                val env: Dotenv = dotenv {
                    directory      = dir
                    ignoreIfMissing = false
                    ignoreIfMalformed = true
                }
                env.entries().forEach { entry ->
                    // Hanya set jika belum ada di system properties
                    if (System.getProperty(entry.key) == null) {
                        System.setProperty(entry.key, entry.value)
                    }
                }
                println("[KampusManager] .env loaded from: ${envFile.absolutePath}")
                loaded = true
                break
            } catch (e: Exception) {
                println("[KampusManager] Failed to load .env from $dir: ${e.message}")
            }
        }
    }

    if (!loaded) {
        println("[KampusManager] WARNING: .env not found in any search path. Using system environment variables only.")
        // Set nilai default jika env var belum ada, agar aplikasi bisa start
        setDefaultIfMissing("APP_HOST",      "0.0.0.0")
        setDefaultIfMissing("APP_PORT",      "8080")
        setDefaultIfMissing("DB_HOST",       "localhost")
        setDefaultIfMissing("DB_PORT",       "5432")
        setDefaultIfMissing("DB_NAME",       "kampus_manager")
        setDefaultIfMissing("DB_USER",       "postgres")
        setDefaultIfMissing("DB_PASSWORD",   "")
        setDefaultIfMissing("JWT_SECRET",    "kampus-manager-default-secret-change-me")
    }
}

private fun setDefaultIfMissing(key: String, default: String) {
    if (System.getProperty(key).isNullOrBlank() && System.getenv(key).isNullOrBlank()) {
        System.setProperty(key, default)
    }
}

fun Application.module() {
    val jwtSecret = environment.config.property("ktor.jwt.secret").getString()

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint       = true
            ignoreUnknownKeys = true
            explicitNulls     = false
        })
    }

    install(Authentication) {
        jwt(JwtConfig.AUTH_NAME) {
            realm = JwtConfig.REALM
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(JwtConfig.ISSUER)
                    .withAudience(JwtConfig.AUDIENCE)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("status" to "error", "message" to "Token tidak valid atau sudah kedaluwarsa")
                )
            }
        }
    }

    install(Koin) {
        modules(appModule(jwtSecret))
    }

    configureDatabase()
    configureRouting()
}
