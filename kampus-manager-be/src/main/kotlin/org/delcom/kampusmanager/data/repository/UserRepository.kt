package org.delcom.kampusmanager.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.delcom.kampusmanager.data.dao.UserDao
import org.delcom.kampusmanager.data.table.UserTable
import org.delcom.kampusmanager.domain.model.User
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface IUserRepository {
    suspend fun findById(id: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun create(user: User): String
    suspend fun update(id: String, user: User): Boolean
}

class UserRepository : IUserRepository {

    private suspend fun <T> dbQuery(block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun UserDao.toModel() = User(
        id        = this.id.value.toString(),
        name      = this.name,
        username  = this.username,
        password  = this.password,
        photo     = this.photo,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )

    override suspend fun findById(id: String): User? = dbQuery {
        UserDao.findById(UUID.fromString(id))?.toModel()
    }

    override suspend fun findByUsername(username: String): User? = dbQuery {
        UserDao.find { UserTable.username eq username }.firstOrNull()?.toModel()
    }

    override suspend fun create(user: User): String = dbQuery {
        UserDao.new {
            name      = user.name
            username  = user.username
            password  = user.password
            photo     = user.photo
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(id: String, user: User): Boolean = dbQuery {
        val dao = UserDao.findById(UUID.fromString(id)) ?: return@dbQuery false
        dao.name      = user.name
        dao.username  = user.username
        dao.password  = user.password
        dao.photo     = user.photo
        dao.updatedAt = Clock.System.now()
        true
    }
}
