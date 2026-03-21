package org.delcom.kampusmanager.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class KegiatanRequest(
    val judul     : String  = "",
    val deskripsi : String? = null,
    val kategori  : String  = "",
    val tanggal   : String  = "",
    val waktu     : String  = "",
    val lokasi    : String? = null,
)

@Serializable
data class KegiatanListResponse(
    val kegiatans : List<org.delcom.kampusmanager.domain.model.Kegiatan>,
    val meta      : MetaDto,
)
