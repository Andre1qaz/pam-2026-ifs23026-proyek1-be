package org.delcom.kampusmanager.data.dao

import org.delcom.kampusmanager.data.table.KegiatanTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class KegiatanDao(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, KegiatanDao>(KegiatanTable)
    var userId    by KegiatanTable.userId
    var judul     by KegiatanTable.judul
    var deskripsi by KegiatanTable.deskripsi
    var kategori  by KegiatanTable.kategori
    var tanggal   by KegiatanTable.tanggal
    var waktu     by KegiatanTable.waktu
    var lokasi    by KegiatanTable.lokasi
    var cover     by KegiatanTable.cover
    var createdAt by KegiatanTable.createdAt
    var updatedAt by KegiatanTable.updatedAt
}
