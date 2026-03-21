package org.delcom.kampusmanager.data.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserTable : UUIDTable("users") {
    val name      = varchar("name", 100)
    val username  = varchar("username", 50).uniqueIndex()
    val password  = varchar("password", 255)
    val photo     = varchar("photo", 255).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
