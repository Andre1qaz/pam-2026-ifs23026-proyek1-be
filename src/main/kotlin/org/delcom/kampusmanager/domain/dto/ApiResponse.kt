package org.delcom.kampusmanager.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val status  : String,
    val message : String,
    val data    : T? = null,
)

@Serializable
data class ErrorResponse(
    val status  : String = "error",
    val message : String,
    val errors  : String? = null,
)

@Serializable
data class MetaDto(
    val total       : Int,
    val perPage     : Int,
    val currentPage : Int,
    val lastPage    : Int,
)
