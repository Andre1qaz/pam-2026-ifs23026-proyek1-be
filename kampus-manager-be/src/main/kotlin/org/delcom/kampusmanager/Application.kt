package org.delcom.kampusmanager

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.delcom.kampusmanager.config.JwtConfig
import org.delcom.kampusmanager.di.appModule
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.plugin.Koin
import java.io.File

// ── Config holder (dibaca dari .env) ──────────────────────────────────────
data class AppConfig(
    val host      : String,
    val port      : Int,
    val dbHost    : String,
    val dbPort    : String,
    val dbName    : String,
    val dbUser    : String,
    val dbPassword: String,
    val jwtSecret : String,
)

fun main() {
    val cfg = loadConfig()

    embeddedServer(Netty, host = cfg.host, port = cfg.port) {
        configureApp(cfg)
    }.start(wait = true)
}

// ── Baca .env dari beberapa lokasi yang mungkin ───────────────────────────
private fun loadConfig(): AppConfig {
    val searchDirs = listOf(
        System.getProperty("user.dir") ?: ".",
        ".",
        File(System.getProperty("user.dir") ?: ".").parent ?: ".",
    )

    var env: Map<String, String> = System.getenv()  // fallback ke OS env vars

    for (dir in searchDirs) {
        val file = File(dir, ".env")
        if (file.exists()) {
            try {
                val dotEnv = dotenv {
                    directory         = dir
                    ignoreIfMissing   = false
                    ignoreIfMalformed = true
                }
                // Gabungkan: OS env vars + .env (OS diutamakan)
                val merged = mutableMapOf<String, String>()
                dotEnv.entries().forEach { merged[it.key] = it.value }
                System.getenv().forEach { (k, v) -> merged[k] = v }   // OS override .env
                env = merged
                println("[KampusManager] .env loaded from: ${file.canonicalPath}")
                break
            } catch (e: Exception) {
                println("[KampusManager] Could not load .env from $dir: ${e.message}")
            }
        }
    }

    fun get(key: String, default: String = ""): String =
        env[key]?.takeIf { it.isNotBlank() } ?: default.also {
            if (default.isBlank()) println("[KampusManager] WARNING: $key is not set!")
        }

    return AppConfig(
        host       = get("APP_HOST",      "0.0.0.0"),
        port       = get("APP_PORT",      "8080").toIntOrNull() ?: 8080,
        dbHost     = get("DB_HOST",       "localhost"),
        dbPort     = get("DB_PORT",       "5432"),
        dbName     = get("DB_NAME",       "kampus_manager"),
        dbUser     = get("DB_USER",       "postgres"),
        dbPassword = get("DB_PASSWORD",   ""),
        jwtSecret  = get("JWT_SECRET",    "kampus-default-secret-please-change"),
    )
}

// ── Konfigurasi Ktor (dipanggil oleh embeddedServer) ─────────────────────
fun Application.configureApp(cfg: AppConfig) {
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
                JWT.require(Algorithm.HMAC256(cfg.jwtSecret))
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
        modules(appModule(cfg.jwtSecret))
    }

    // Koneksi database langsung dari config (tidak lewat application.yaml)
    Database.connect(
        url      = "jdbc:postgresql://${cfg.dbHost}:${cfg.dbPort}/${cfg.dbName}",
        driver   = "org.postgresql.Driver",
        user     = cfg.dbUser,
        password = cfg.dbPassword,
    )

    configureRouting()
}
