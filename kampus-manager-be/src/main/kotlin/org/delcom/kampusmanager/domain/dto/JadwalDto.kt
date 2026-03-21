package org.delcom.kampusmanager.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class JadwalRequest(
    val mataKuliah : String  = "",
    val dosen      : String  = "",
    val hari       : String  = "",
    val jamMulai   : String  = "",
    val jamSelesai : String  = "",
    val ruangan    : String  = "",
    val semester   : String? = null,
    val keterangan : String? = null,
)

@Serializable
data class JadwalListResponse(
    val jadwalKuliahs : List<org.delcom.kampusmanager.domain.model.JadwalKuliah>,
    val meta          : MetaDto,
)
