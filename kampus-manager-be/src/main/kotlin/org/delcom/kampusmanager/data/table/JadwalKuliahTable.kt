package org.delcom.kampusmanager.data.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object JadwalKuliahTable : UUIDTable("jadwal_kuliahs") {
    val userId      = uuid("user_id")
    val mataKuliah  = varchar("mata_kuliah", 100)
    val dosen       = varchar("dosen", 100)
    val hari        = varchar("hari", 10)
    val jamMulai    = varchar("jam_mulai", 10)
    val jamSelesai  = varchar("jam_selesai", 10)
    val ruangan     = varchar("ruangan", 100)
    val semester    = varchar("semester", 20).nullable()
    val keterangan  = text("keterangan").nullable()
    val cover       = text("cover").nullable()
    val createdAt   = timestamp("created_at")
    val updatedAt   = timestamp("updated_at")
}
