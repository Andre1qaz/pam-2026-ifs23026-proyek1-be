package org.delcom.kampusmanager.service

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.kampusmanager.data.repository.IRefreshTokenRepository
import org.delcom.kampusmanager.data.repository.IUserRepository
import org.delcom.kampusmanager.domain.dto.*
import org.delcom.kampusmanager.domain.model.RefreshToken
import org.delcom.kampusmanager.domain.model.User
import org.delcom.kampusmanager.util.*
import java.util.UUID

class AuthService(
    private val jwtSecret  : String,
    private val userRepo   : IUserRepository,
    private val tokenRepo  : IRefreshTokenRepository,
) {
    suspend fun register(call: ApplicationCall) {
        val req = call.receive<RegisterRequest>()
        val v   = Validator(mapOf("name" to req.name, "username" to req.username, "password" to req.password))
        v.required("name",     "Nama")
        v.required("username", "Username")
        v.required("password", "Kata sandi")
        v.minLength("password", 6, "Kata sandi")
        v.validate()

        if (userRepo.findByUsername(req.username) != null)
            throw AppException(409, "Username sudah terdaftar")

        val userId = userRepo.create(
            User(name = req.name, username = req.username, password = Security.hashPassword(req.password))
        )
        call.respond(ApiResponse("success", "Akun berhasil dibuat", mapOf("userId" to userId)))
    }

    suspend fun login(call: ApplicationCall) {
        val req  = call.receive<LoginRequest>()
        val v    = Validator(mapOf("username" to req.username, "password" to req.password))
        v.required("username", "Username")
        v.required("password", "Kata sandi")
        v.validate()

        val user = userRepo.findByUsername(req.username)
            ?: throw AppException(401, "Username atau kata sandi salah")

        if (!Security.verifyPassword(req.password, user.password))
            throw AppException(401, "Username atau kata sandi salah")

        tokenRepo.deleteByUserId(user.id)

        val authToken    = TokenUtil.generateAuthToken(jwtSecret, user.id)
        val refreshToken = UUID.randomUUID().toString()
        tokenRepo.create(RefreshToken(userId = user.id, authToken = authToken, refreshToken = refreshToken))

        call.respond(ApiResponse("success", "Login berhasil",
            AuthTokenResponse(authToken = authToken, refreshToken = refreshToken)))
    }

    suspend fun refreshToken(call: ApplicationCall) {
        val req = call.receive<RefreshTokenRequest>()
        val v   = Validator(mapOf("refreshToken" to req.refreshToken, "authToken" to req.authToken))
        v.required("refreshToken", "Refresh token")
        v.required("authToken",    "Auth token")
        v.validate()

        val existing = tokenRepo.findByTokens(req.refreshToken, req.authToken)
        tokenRepo.deleteByAuthToken(req.authToken)
        if (existing == null) throw AppException(401, "Token tidak valid")

        val user = userRepo.findById(existing.userId) ?: throw AppException(401, "Pengguna tidak ditemukan")

        val newAuth    = TokenUtil.generateAuthToken(jwtSecret, user.id)
        val newRefresh = UUID.randomUUID().toString()
        tokenRepo.create(RefreshToken(userId = user.id, authToken = newAuth, refreshToken = newRefresh))

        call.respond(ApiResponse("success", "Token berhasil diperbarui",
            AuthTokenResponse(authToken = newAuth, refreshToken = newRefresh)))
    }

    suspend fun logout(call: ApplicationCall) {
        val req = call.receive<RefreshTokenRequest>()
        val v   = Validator(mapOf("authToken" to req.authToken))
        v.required("authToken", "Auth token")
        v.validate()

        val userId = TokenUtil.extractUserId(jwtSecret, req.authToken)
        tokenRepo.deleteByAuthToken(req.authToken)
        if (userId != null) tokenRepo.deleteByUserId(userId)

        call.respond(ApiResponse<Unit>("success", "Logout berhasil"))
    }
}
