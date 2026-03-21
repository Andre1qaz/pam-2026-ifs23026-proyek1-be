package org.delcom.kampusmanager.data.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RefreshTokenTable : UUIDTable("refresh_tokens") {
    val userId       = uuid("user_id")
    val authToken    = text("auth_token")
    val refreshToken = text("refresh_token")
    val createdAt    = timestamp("created_at")
}
