package org.delcom.kampusmanager.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    val id       : String  = UUID.randomUUID().toString(),
    var name     : String,
    var username : String,
    var password : String,
    var photo    : String? = null,
    @Contextual val createdAt : Instant = Clock.System.now(),
    @Contextual var updatedAt : Instant = Clock.System.now(),
)
