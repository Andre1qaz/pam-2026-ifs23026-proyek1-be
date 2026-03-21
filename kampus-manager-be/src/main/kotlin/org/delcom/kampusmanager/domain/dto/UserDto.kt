package org.delcom.kampusmanager.domain.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id        : String,
    val name      : String,
    val username  : String,
    @Contextual val createdAt : Instant = Clock.System.now(),
    @Contextual val updatedAt : Instant = Clock.System.now(),
)

@Serializable
data class UpdateProfileRequest(
    val name     : String = "",
    val username : String = "",
)

@Serializable
data class UpdatePasswordRequest(
    val password    : String = "",
    val newPassword : String = "",
)
