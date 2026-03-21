package org.delcom.kampusmanager.data.dao

import org.delcom.kampusmanager.data.table.UserTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserDao(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, UserDao>(UserTable)
    var name      by UserTable.name
    var username  by UserTable.username
    var password  by UserTable.password
    var photo     by UserTable.photo
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt
}
