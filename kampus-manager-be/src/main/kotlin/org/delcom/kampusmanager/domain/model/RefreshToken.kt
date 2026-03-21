package org.delcom.kampusmanager.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RefreshToken(
    val id           : String = UUID.randomUUID().toString(),
    val userId       : String,
    val authToken    : String,
    val refreshToken : String,
    @Contextual val createdAt : Instant = Clock.System.now(),
)
