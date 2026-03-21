package org.delcom.kampusmanager.data.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object KegiatanTable : UUIDTable("kegiatans") {
    val userId    = uuid("user_id")
    val judul     = varchar("judul", 100)
    val deskripsi = text("deskripsi").nullable()
    val kategori  = varchar("kategori", 20)
    val tanggal   = varchar("tanggal", 20)
    val waktu     = varchar("waktu", 10)
    val lokasi    = varchar("lokasi", 100).nullable()
    val cover     = text("cover").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
