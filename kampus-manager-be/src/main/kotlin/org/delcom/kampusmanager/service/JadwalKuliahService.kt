package org.delcom.kampusmanager.service

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.kampusmanager.data.repository.IJadwalKuliahRepository
import org.delcom.kampusmanager.data.repository.IUserRepository
import org.delcom.kampusmanager.domain.dto.*
import org.delcom.kampusmanager.domain.model.JadwalKuliah
import org.delcom.kampusmanager.util.*
import java.io.File
import kotlin.math.ceil

class JadwalKuliahService(
    private val userRepo  : IUserRepository,
    private val jadwalRepo: IJadwalKuliahRepository,
) {
    private fun userId(call: ApplicationCall) =
        call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()

    suspend fun getAll(call: ApplicationCall) {
        val uid      = userId(call)
        val search   = call.request.queryParameters["search"]   ?: ""
        val hari     = call.request.queryParameters["hari"]     ?: ""
        val semester = call.request.queryParameters["semester"] ?: ""
        val page     = (call.request.queryParameters["page"]    ?: "1").toIntOrNull()?.coerceAtLeast(1) ?: 1
        val perPage  = (call.request.queryParameters["perPage"] ?: "20").toIntOrNull()?.coerceIn(1, 100) ?: 20

        val all      = jadwalRepo.findAll(uid, search, hari, semester)
        val total    = all.size
        val lastPage = if (total == 0) 1 else ceil(total.toDouble() / perPage).toInt()
        val items    = all.drop((page - 1) * perPage).take(perPage)

        call.respond(ApiResponse("success", "Berhasil mengambil daftar jadwal",
            JadwalListResponse(items, MetaDto(total, perPage, page, lastPage))))
    }

    suspend fun getById(call: ApplicationCall) {
        val uid    = userId(call)
        val id     = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val jadwal = jadwalRepo.findById(id)
        if (jadwal == null || jadwal.userId != uid) throw AppException(404, "Jadwal tidak ditemukan")
        call.respond(ApiResponse("success", "Berhasil mengambil data jadwal", mapOf("jadwalKuliah" to jadwal)))
    }

    suspend fun create(call: ApplicationCall) {
        val uid = userId(call)
        val req = call.receive<JadwalRequest>()
        val v   = Validator(mapOf(
            "mataKuliah" to req.mataKuliah, "dosen" to req.dosen, "hari" to req.hari,
            "jamMulai"   to req.jamMulai,   "jamSelesai" to req.jamSelesai, "ruangan" to req.ruangan,
        ))
        v.required("mataKuliah", "Mata kuliah"); v.required("dosen", "Dosen")
        v.required("hari", "Hari");              v.required("jamMulai", "Jam mulai")
        v.required("jamSelesai", "Jam selesai"); v.required("ruangan", "Ruangan")
        v.validate()

        val newId = jadwalRepo.create(JadwalKuliah(
            userId = uid, mataKuliah = req.mataKuliah, dosen = req.dosen, hari = req.hari,
            jamMulai = req.jamMulai, jamSelesai = req.jamSelesai, ruangan = req.ruangan,
            semester = req.semester, keterangan = req.keterangan,
        ))
        call.respond(ApiResponse("success", "Jadwal berhasil ditambahkan", mapOf("jadwalKuliahId" to newId)))
    }

    suspend fun update(call: ApplicationCall) {
        val uid = userId(call)
        val id  = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val req = call.receive<JadwalRequest>()
        val v   = Validator(mapOf(
            "mataKuliah" to req.mataKuliah, "dosen" to req.dosen, "hari" to req.hari,
            "jamMulai"   to req.jamMulai,   "jamSelesai" to req.jamSelesai, "ruangan" to req.ruangan,
        ))
        v.required("mataKuliah", "Mata kuliah"); v.required("dosen", "Dosen")
        v.required("hari", "Hari");              v.required("jamMulai", "Jam mulai")
        v.required("jamSelesai", "Jam selesai"); v.required("ruangan", "Ruangan")
        v.validate()

        val old = jadwalRepo.findById(id)
        if (old == null || old.userId != uid) throw AppException(404, "Jadwal tidak ditemukan")

        jadwalRepo.update(uid, id, JadwalKuliah(
            id = id, userId = uid, mataKuliah = req.mataKuliah, dosen = req.dosen, hari = req.hari,
            jamMulai = req.jamMulai, jamSelesai = req.jamSelesai, ruangan = req.ruangan,
            semester = req.semester, keterangan = req.keterangan, cover = old.cover,
        ))
        call.respond(ApiResponse<Unit>("success", "Jadwal berhasil diperbarui"))
    }

    suspend fun updateCover(call: ApplicationCall) {
        val uid  = userId(call)
        val id   = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val old  = jadwalRepo.findById(id)
        if (old == null || old.userId != uid) throw AppException(404, "Jadwal tidak ditemukan")

        var newPath: String? = null
        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10).forEachPart { part ->
            if (part is PartData.FileItem) newPath = FileUtil.saveUpload(part, "jadwal")
            part.dispose()
        }
        if (newPath == null || !File(newPath!!).exists()) throw AppException(400, "Cover gagal diunggah")

        jadwalRepo.update(uid, id, old.copy(cover = newPath))
        FileUtil.deleteFile(old.cover)
        call.respond(ApiResponse<Unit>("success", "Cover jadwal berhasil diperbarui"))
    }

    suspend fun delete(call: ApplicationCall) {
        val uid = userId(call)
        val id  = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val old = jadwalRepo.findById(id)
        if (old == null || old.userId != uid) throw AppException(404, "Jadwal tidak ditemukan")

        jadwalRepo.delete(uid, id)
        FileUtil.deleteFile(old.cover)
        call.respond(ApiResponse<Unit>("success", "Jadwal berhasil dihapus"))
    }

    suspend fun getCover(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val jadwal = jadwalRepo.findById(id) ?: throw AppException(404, "Jadwal tidak ditemukan")
        val file = jadwal.cover?.let { File(it) } ?: throw AppException(404, "Cover belum tersedia")
        if (!file.exists()) throw AppException(404, "File cover tidak ditemukan")
        call.respondFile(file)
    }
}
