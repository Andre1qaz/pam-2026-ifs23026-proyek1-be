package org.delcom.kampusmanager.data.dao

import org.delcom.kampusmanager.data.table.JadwalKuliahTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class JadwalKuliahDao(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, JadwalKuliahDao>(JadwalKuliahTable)
    var userId      by JadwalKuliahTable.userId
    var mataKuliah  by JadwalKuliahTable.mataKuliah
    var dosen       by JadwalKuliahTable.dosen
    var hari        by JadwalKuliahTable.hari
    var jamMulai    by JadwalKuliahTable.jamMulai
    var jamSelesai  by JadwalKuliahTable.jamSelesai
    var ruangan     by JadwalKuliahTable.ruangan
    var semester    by JadwalKuliahTable.semester
    var keterangan  by JadwalKuliahTable.keterangan
    var cover       by JadwalKuliahTable.cover
    var createdAt   by JadwalKuliahTable.createdAt
    var updatedAt   by JadwalKuliahTable.updatedAt
}
