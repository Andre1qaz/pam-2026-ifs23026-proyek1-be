package org.delcom.kampusmanager.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.delcom.kampusmanager.data.dao.KegiatanDao
import org.delcom.kampusmanager.data.table.KegiatanTable
import org.delcom.kampusmanager.domain.model.Kegiatan
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface IKegiatanRepository {
    suspend fun findAll(userId: String, search: String, tanggal: String, kategori: String): List<Kegiatan>
    suspend fun findById(id: String): Kegiatan?
    suspend fun create(kegiatan: Kegiatan): String
    suspend fun update(userId: String, id: String, kegiatan: Kegiatan): Boolean
    suspend fun delete(userId: String, id: String): Boolean
}

class KegiatanRepository : IKegiatanRepository {

    private suspend fun <T> dbQuery(block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun KegiatanDao.toModel() = Kegiatan(
        id        = this.id.value.toString(),
        userId    = this.userId.toString(),
        judul     = this.judul,
        deskripsi = this.deskripsi,
        kategori  = this.kategori,
        tanggal   = this.tanggal,
        waktu     = this.waktu,
        lokasi    = this.lokasi,
        cover     = this.cover,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )

    override suspend fun findAll(userId: String, search: String, tanggal: String, kategori: String): List<Kegiatan> = dbQuery {
        var cond: Op<Boolean> = KegiatanTable.userId eq UUID.fromString(userId)
        if (tanggal.isNotBlank())  cond = cond and (KegiatanTable.tanggal  eq tanggal)
        if (kategori.isNotBlank()) cond = cond and (KegiatanTable.kategori eq kategori)

        val result = KegiatanDao.find { cond }
            .orderBy(KegiatanTable.createdAt to SortOrder.DESC)
            .map { it.toModel() }

        if (search.isBlank()) result
        else {
            val kw = search.lowercase()
            result.filter {
                it.judul.lowercase().contains(kw) ||
                it.deskripsi?.lowercase()?.contains(kw) == true ||
                it.lokasi?.lowercase()?.contains(kw) == true
            }
        }
    }

    override suspend fun findById(id: String): Kegiatan? = dbQuery {
        KegiatanDao.findById(UUID.fromString(id))?.toModel()
    }

    override suspend fun create(kegiatan: Kegiatan): String = dbQuery {
        KegiatanDao.new {
            userId    = UUID.fromString(kegiatan.userId)
            judul     = kegiatan.judul
            deskripsi = kegiatan.deskripsi
            kategori  = kegiatan.kategori
            tanggal   = kegiatan.tanggal
            waktu     = kegiatan.waktu
            lokasi    = kegiatan.lokasi
            cover     = kegiatan.cover
            createdAt = kegiatan.createdAt
            updatedAt = kegiatan.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(userId: String, id: String, kegiatan: Kegiatan): Boolean = dbQuery {
        val dao = KegiatanDao.find {
            (KegiatanTable.id     eq UUID.fromString(id)) and
            (KegiatanTable.userId eq UUID.fromString(userId))
        }.firstOrNull() ?: return@dbQuery false

        dao.judul     = kegiatan.judul
        dao.deskripsi = kegiatan.deskripsi
        dao.kategori  = kegiatan.kategori
        dao.tanggal   = kegiatan.tanggal
        dao.waktu     = kegiatan.waktu
        dao.lokasi    = kegiatan.lokasi
        dao.cover     = kegiatan.cover
        dao.updatedAt = Clock.System.now()
        true
    }

    override suspend fun delete(userId: String, id: String): Boolean = dbQuery {
        KegiatanTable.deleteWhere {
            (KegiatanTable.id     eq UUID.fromString(id)) and
            (KegiatanTable.userId eq UUID.fromString(userId))
        } >= 1
    }
}
