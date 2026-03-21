package org.delcom.kampusmanager.service

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.kampusmanager.data.repository.IKegiatanRepository
import org.delcom.kampusmanager.data.repository.IUserRepository
import org.delcom.kampusmanager.domain.dto.*
import org.delcom.kampusmanager.domain.model.Kegiatan
import org.delcom.kampusmanager.util.*
import java.io.File
import kotlin.math.ceil

class KegiatanService(
    private val userRepo    : IUserRepository,
    private val kegiatanRepo: IKegiatanRepository,
) {
    private fun userId(call: ApplicationCall) =
        call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()

    suspend fun getAll(call: ApplicationCall) {
        val uid      = userId(call)
        val search   = call.request.queryParameters["search"]   ?: ""
        val tanggal  = call.request.queryParameters["tanggal"]  ?: ""
        val kategori = call.request.queryParameters["kategori"] ?: ""
        val page     = (call.request.queryParameters["page"]    ?: "1").toIntOrNull()?.coerceAtLeast(1) ?: 1
        val perPage  = (call.request.queryParameters["perPage"] ?: "20").toIntOrNull()?.coerceIn(1, 100) ?: 20

        val all      = kegiatanRepo.findAll(uid, search, tanggal, kategori)
        val total    = all.size
        val lastPage = if (total == 0) 1 else ceil(total.toDouble() / perPage).toInt()
        val items    = all.drop((page - 1) * perPage).take(perPage)

        call.respond(ApiResponse("success", "Berhasil mengambil daftar kegiatan",
            KegiatanListResponse(items, MetaDto(total, perPage, page, lastPage))))
    }

    suspend fun getById(call: ApplicationCall) {
        val uid      = userId(call)
        val id       = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val kegiatan = kegiatanRepo.findById(id)
        if (kegiatan == null || kegiatan.userId != uid) throw AppException(404, "Kegiatan tidak ditemukan")
        call.respond(ApiResponse("success", "Berhasil mengambil data kegiatan", mapOf("kegiatan" to kegiatan)))
    }

    suspend fun create(call: ApplicationCall) {
        val uid = userId(call)
        val req = call.receive<KegiatanRequest>()
        val v   = Validator(mapOf("judul" to req.judul, "kategori" to req.kategori, "tanggal" to req.tanggal, "waktu" to req.waktu))
        v.required("judul",    "Judul"); v.required("kategori", "Kategori")
        v.required("tanggal",  "Tanggal"); v.required("waktu", "Waktu")
        v.validate()

        val newId = kegiatanRepo.create(Kegiatan(
            userId = uid, judul = req.judul, deskripsi = req.deskripsi,
            kategori = req.kategori, tanggal = req.tanggal, waktu = req.waktu, lokasi = req.lokasi,
        ))
        call.respond(ApiResponse("success", "Kegiatan berhasil ditambahkan", mapOf("kegiatanId" to newId)))
    }

    suspend fun update(call: ApplicationCall) {
        val uid = userId(call)
        val id  = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val req = call.receive<KegiatanRequest>()
        val v   = Validator(mapOf("judul" to req.judul, "kategori" to req.kategori, "tanggal" to req.tanggal, "waktu" to req.waktu))
        v.required("judul",    "Judul"); v.required("kategori", "Kategori")
        v.required("tanggal",  "Tanggal"); v.required("waktu", "Waktu")
        v.validate()

        val old = kegiatanRepo.findById(id)
        if (old == null || old.userId != uid) throw AppException(404, "Kegiatan tidak ditemukan")

        kegiatanRepo.update(uid, id, Kegiatan(
            id = id, userId = uid, judul = req.judul, deskripsi = req.deskripsi,
            kategori = req.kategori, tanggal = req.tanggal, waktu = req.waktu,
            lokasi = req.lokasi, cover = old.cover,
        ))
        call.respond(ApiResponse<Unit>("success", "Kegiatan berhasil diperbarui"))
    }

    suspend fun updateCover(call: ApplicationCall) {
        val uid = userId(call)
        val id  = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val old = kegiatanRepo.findById(id)
        if (old == null || old.userId != uid) throw AppException(404, "Kegiatan tidak ditemukan")

        var newPath: String? = null
        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10).forEachPart { part ->
            if (part is PartData.FileItem) newPath = FileUtil.saveUpload(part, "kegiatan")
            part.dispose()
        }
        if (newPath == null || !File(newPath!!).exists()) throw AppException(400, "Cover gagal diunggah")

        kegiatanRepo.update(uid, id, old.copy(cover = newPath))
        FileUtil.deleteFile(old.cover)
        call.respond(ApiResponse<Unit>("success", "Cover kegiatan berhasil diperbarui"))
    }

    suspend fun delete(call: ApplicationCall) {
        val uid      = userId(call)
        val id       = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val old      = kegiatanRepo.findById(id)
        if (old == null || old.userId != uid) throw AppException(404, "Kegiatan tidak ditemukan")

        kegiatanRepo.delete(uid, id)
        FileUtil.deleteFile(old.cover)
        call.respond(ApiResponse<Unit>("success", "Kegiatan berhasil dihapus"))
    }

    suspend fun getCover(call: ApplicationCall) {
        val id       = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val kegiatan = kegiatanRepo.findById(id) ?: throw AppException(404, "Kegiatan tidak ditemukan")
        val file     = kegiatan.cover?.let { File(it) } ?: throw AppException(404, "Cover belum tersedia")
        if (!file.exists()) throw AppException(404, "File cover tidak ditemukan")
        call.respondFile(file)
    }
}
