package org.delcom.kampusmanager.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.delcom.kampusmanager.data.dao.JadwalKuliahDao
import org.delcom.kampusmanager.data.table.JadwalKuliahTable
import org.delcom.kampusmanager.domain.model.JadwalKuliah
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface IJadwalKuliahRepository {
    suspend fun findAll(userId: String, search: String, hari: String, semester: String): List<JadwalKuliah>
    suspend fun findById(id: String): JadwalKuliah?
    suspend fun create(jadwal: JadwalKuliah): String
    suspend fun update(userId: String, id: String, jadwal: JadwalKuliah): Boolean
    suspend fun delete(userId: String, id: String): Boolean
}

class JadwalKuliahRepository : IJadwalKuliahRepository {

    private suspend fun <T> dbQuery(block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun JadwalKuliahDao.toModel() = JadwalKuliah(
        id         = this.id.value.toString(),
        userId     = this.userId.toString(),
        mataKuliah = this.mataKuliah,
        dosen      = this.dosen,
        hari       = this.hari,
        jamMulai   = this.jamMulai,
        jamSelesai = this.jamSelesai,
        ruangan    = this.ruangan,
        semester   = this.semester,
        keterangan = this.keterangan,
        cover      = this.cover,
        createdAt  = this.createdAt,
        updatedAt  = this.updatedAt,
    )

    override suspend fun findAll(userId: String, search: String, hari: String, semester: String): List<JadwalKuliah> = dbQuery {
        var cond: Op<Boolean> = JadwalKuliahTable.userId eq UUID.fromString(userId)
        if (hari.isNotBlank())     cond = cond and (JadwalKuliahTable.hari eq hari)
        if (semester.isNotBlank()) cond = cond and (JadwalKuliahTable.semester eq semester)

        val result = JadwalKuliahDao.find { cond }
            .orderBy(JadwalKuliahTable.createdAt to SortOrder.DESC)
            .map { it.toModel() }

        if (search.isBlank()) result
        else {
            val kw = search.lowercase()
            result.filter {
                it.mataKuliah.lowercase().contains(kw) ||
                it.dosen.lowercase().contains(kw) ||
                it.ruangan.lowercase().contains(kw)
            }
        }
    }

    override suspend fun findById(id: String): JadwalKuliah? = dbQuery {
        JadwalKuliahDao.findById(UUID.fromString(id))?.toModel()
    }

    override suspend fun create(jadwal: JadwalKuliah): String = dbQuery {
        JadwalKuliahDao.new {
            userId      = UUID.fromString(jadwal.userId)
            mataKuliah  = jadwal.mataKuliah
            dosen       = jadwal.dosen
            hari        = jadwal.hari
            jamMulai    = jadwal.jamMulai
            jamSelesai  = jadwal.jamSelesai
            ruangan     = jadwal.ruangan
            semester    = jadwal.semester
            keterangan  = jadwal.keterangan
            cover       = jadwal.cover
            createdAt   = jadwal.createdAt
            updatedAt   = jadwal.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(userId: String, id: String, jadwal: JadwalKuliah): Boolean = dbQuery {
        val dao = JadwalKuliahDao.find {
            (JadwalKuliahTable.id     eq UUID.fromString(id)) and
            (JadwalKuliahTable.userId eq UUID.fromString(userId))
        }.firstOrNull() ?: return@dbQuery false

        dao.mataKuliah = jadwal.mataKuliah
        dao.dosen      = jadwal.dosen
        dao.hari       = jadwal.hari
        dao.jamMulai   = jadwal.jamMulai
        dao.jamSelesai = jadwal.jamSelesai
        dao.ruangan    = jadwal.ruangan
        dao.semester   = jadwal.semester
        dao.keterangan = jadwal.keterangan
        dao.cover      = jadwal.cover
        dao.updatedAt  = Clock.System.now()
        true
    }

    override suspend fun delete(userId: String, id: String): Boolean = dbQuery {
        JadwalKuliahTable.deleteWhere {
            (JadwalKuliahTable.id     eq UUID.fromString(id)) and
            (JadwalKuliahTable.userId eq UUID.fromString(userId))
        } >= 1
    }
}
