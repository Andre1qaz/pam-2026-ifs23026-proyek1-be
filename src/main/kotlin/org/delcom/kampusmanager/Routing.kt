package org.delcom.kampusmanager

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.kampusmanager.config.JwtConfig
import org.delcom.kampusmanager.domain.dto.ErrorResponse
import org.delcom.kampusmanager.service.*
import org.delcom.kampusmanager.util.AppException
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService   : AuthService          by inject()
    val userService   : UserService          by inject()
    val jadwalService : JadwalKuliahService  by inject()
    val kegiatanService : KegiatanService    by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            call.respond(
                HttpStatusCode.fromValue(cause.code),
                ErrorResponse(status = "fail", message = cause.message),
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(status = "error", message = cause.message ?: "Terjadi kesalahan"),
            )
        }
    }

    routing {
        get("/") {
            call.respondText("Kampus Manager API v1.0 – org.delcom.kampusmanager dibuat Andre Komting 11S23026")
        }

        // ── Auth ──────────────────────────────────────────────────────────
        route("/auth") {
            post("/register")      { authService.register(call) }
            post("/login")         { authService.login(call) }
            post("/refresh-token") { authService.refreshToken(call) }
            post("/logout")        { authService.logout(call) }
        }

        // ── Protected routes ──────────────────────────────────────────────
        authenticate(JwtConfig.AUTH_NAME) {

            // Users
            route("/users") {
                get("/me")          { userService.getMe(call) }
                put("/me")          { userService.updateMe(call) }
                put("/me/password") { userService.updatePassword(call) }
                put("/me/photo")    { userService.updatePhoto(call) }
            }

            // Jadwal Kuliah
            route("/jadwal-kuliahs") {
                get             { jadwalService.getAll(call) }
                post            { jadwalService.create(call) }
                get("/{id}")    { jadwalService.getById(call) }
                put("/{id}")    { jadwalService.update(call) }
                put("/{id}/cover") { jadwalService.updateCover(call) }
                delete("/{id}") { jadwalService.delete(call) }
            }

            // Kegiatan
            route("/kegiatans") {
                get             { kegiatanService.getAll(call) }
                post            { kegiatanService.create(call) }
                get("/{id}")    { kegiatanService.getById(call) }
                put("/{id}")    { kegiatanService.update(call) }
                put("/{id}/cover") { kegiatanService.updateCover(call) }
                delete("/{id}") { kegiatanService.delete(call) }
            }
        }

        // ── Public image endpoints ─────────────────────────────────────────
        route("/images") {
            get("/users/{id}")         { userService.getPhoto(call) }
            get("/jadwal-kuliahs/{id}") { jadwalService.getCover(call) }
            get("/kegiatans/{id}")      { kegiatanService.getCover(call) }
        }
    }
}
