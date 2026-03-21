package org.delcom.kampusmanager.data.dao

import org.delcom.kampusmanager.data.table.RefreshTokenTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class RefreshTokenDao(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, RefreshTokenDao>(RefreshTokenTable)
    var userId       by RefreshTokenTable.userId
    var authToken    by RefreshTokenTable.authToken
    var refreshToken by RefreshTokenTable.refreshToken
    var createdAt    by RefreshTokenTable.createdAt
}
