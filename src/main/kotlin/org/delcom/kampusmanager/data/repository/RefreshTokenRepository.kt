package org.delcom.kampusmanager.data.repository

import kotlinx.coroutines.Dispatchers
import org.delcom.kampusmanager.data.dao.RefreshTokenDao
import org.delcom.kampusmanager.data.table.RefreshTokenTable
import org.delcom.kampusmanager.domain.model.RefreshToken
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface IRefreshTokenRepository {
    suspend fun findByTokens(refreshToken: String, authToken: String): RefreshToken?
    suspend fun create(token: RefreshToken)
    suspend fun deleteByAuthToken(authToken: String)
    suspend fun deleteByUserId(userId: String)
}

class RefreshTokenRepository : IRefreshTokenRepository {

    private suspend fun <T> dbQuery(block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun RefreshTokenDao.toModel() = RefreshToken(
        id           = this.id.value.toString(),
        userId       = this.userId.toString(),
        authToken    = this.authToken,
        refreshToken = this.refreshToken,
        createdAt    = this.createdAt,
    )

    override suspend fun findByTokens(refreshToken: String, authToken: String): RefreshToken? = dbQuery {
        RefreshTokenDao.find {
            (RefreshTokenTable.refreshToken eq refreshToken) and
            (RefreshTokenTable.authToken    eq authToken)
        }.firstOrNull()?.toModel()
    }

    override suspend fun create(token: RefreshToken) = dbQuery {
        RefreshTokenDao.new {
            userId           = UUID.fromString(token.userId)
            authToken        = token.authToken
            refreshToken     = token.refreshToken
            createdAt        = token.createdAt
        }
        Unit
    }

    override suspend fun deleteByAuthToken(authToken: String) = dbQuery {
        RefreshTokenTable.deleteWhere { RefreshTokenTable.authToken eq authToken }
        Unit
    }

    override suspend fun deleteByUserId(userId: String) = dbQuery {
        RefreshTokenTable.deleteWhere { RefreshTokenTable.userId eq UUID.fromString(userId) }
        Unit
    }
}
