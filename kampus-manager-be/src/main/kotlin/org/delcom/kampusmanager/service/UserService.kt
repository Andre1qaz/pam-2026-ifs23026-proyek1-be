package org.delcom.kampusmanager.service

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.kampusmanager.data.repository.IUserRepository
import org.delcom.kampusmanager.domain.dto.*
import org.delcom.kampusmanager.util.*
import java.io.File

class UserService(private val userRepo: IUserRepository) {

    private suspend fun getCallerUser(call: ApplicationCall) =
        userRepo.findById(
            call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
        ) ?: throw AppException(401, "Pengguna tidak ditemukan")

    suspend fun getMe(call: ApplicationCall) {
        val user = getCallerUser(call)
        call.respond(ApiResponse("success", "Berhasil mengambil profil",
            mapOf("user" to UserResponse(user.id, user.name, user.username, user.createdAt, user.updatedAt))))
    }

    suspend fun updateMe(call: ApplicationCall) {
        val user = getCallerUser(call)
        val req  = call.receive<UpdateProfileRequest>()
        val v    = Validator(mapOf("name" to req.name, "username" to req.username))
        v.required("name",     "Nama")
        v.required("username", "Username")
        v.validate()

        val existing = userRepo.findByUsername(req.username)
        if (existing != null && existing.id != user.id)
            throw AppException(409, "Username sudah digunakan")

        user.name     = req.name
        user.username = req.username
        userRepo.update(user.id, user)
        call.respond(ApiResponse<Unit>("success", "Profil berhasil diperbarui"))
    }

    suspend fun updatePassword(call: ApplicationCall) {
        val user = getCallerUser(call)
        val req  = call.receive<UpdatePasswordRequest>()
        val v    = Validator(mapOf("password" to req.password, "newPassword" to req.newPassword))
        v.required("password",    "Kata sandi lama")
        v.required("newPassword", "Kata sandi baru")
        v.minLength("newPassword", 6, "Kata sandi baru")
        v.validate()

        if (!Security.verifyPassword(req.password, user.password))
            throw AppException(400, "Kata sandi lama tidak sesuai")

        user.password = Security.hashPassword(req.newPassword)
        userRepo.update(user.id, user)
        call.respond(ApiResponse<Unit>("success", "Kata sandi berhasil diubah"))
    }

    suspend fun updatePhoto(call: ApplicationCall) {
        val user     = getCallerUser(call)
        var newPath: String? = null

        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10).forEachPart { part ->
            if (part is PartData.FileItem) {
                newPath = FileUtil.saveUpload(part, "users")
            }
            part.dispose()
        }

        if (newPath == null || !File(newPath!!).exists())
            throw AppException(400, "Foto gagal diunggah")

        val oldPhoto = user.photo
        user.photo   = newPath
        userRepo.update(user.id, user)
        FileUtil.deleteFile(oldPhoto)

        call.respond(ApiResponse<Unit>("success", "Foto profil berhasil diperbarui"))
    }

    suspend fun getPhoto(call: ApplicationCall) {
        val userId = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val user   = userRepo.findById(userId) ?: throw AppException(404, "Pengguna tidak ditemukan")
        val file   = user.photo?.let { File(it) } ?: throw AppException(404, "Foto belum tersedia")
        if (!file.exists()) throw AppException(404, "File foto tidak ditemukan")
        call.respondFile(file)
    }
}
