package repository


import database.DatabaseFactory
import database.tables.RefreshTokens
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.time.Instant
import java.util.UUID

data class RefreshRow(
    val tokenId: String,
    val userId: UUID,
    val tokenHash: String,
    val issuedAt: Long,
    val expiresAt: Long,
    val device: String?,
    val ipHash: String?,
    val revoked: Boolean
)

object RefreshTokenRepository {

    suspend fun insert(
        tokenId: String,
        userId: UUID,
        tokenHash: String,
        issuedAt: Long,
        expiresAt: Long,
        device: String?,
        ipHash: String?
    ) = DatabaseFactory.dbQuery {
        RefreshTokens.insert {
            it[RefreshTokens.tokenId] = tokenId
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.tokenHash] = tokenHash
            it[RefreshTokens.issuedAt] = issuedAt
            it[RefreshTokens.expiresAt] = expiresAt
            it[RefreshTokens.device] = device
            it[RefreshTokens.ipHash] = ipHash
            it[revoked] = false
        }
    }

    suspend fun find(tokenId: String): RefreshRow? = DatabaseFactory.dbQuery {
        RefreshTokens
            .selectAll().where { RefreshTokens.tokenId eq tokenId }
            .limit(1)
            .singleOrNull()
            ?.let {
                RefreshRow(
                    tokenId = it[RefreshTokens.tokenId],
                    userId = it[RefreshTokens.userId],
                    tokenHash = it[RefreshTokens.tokenHash],
                    issuedAt = it[RefreshTokens.issuedAt],
                    expiresAt = it[RefreshTokens.expiresAt],
                    device = it[RefreshTokens.device],
                    ipHash = it[RefreshTokens.ipHash],
                    revoked = it[RefreshTokens.revoked]
                )
            }
    }

    suspend fun revoke(tokenId: String): Int = DatabaseFactory.dbQuery {
        RefreshTokens.update({ RefreshTokens.tokenId eq tokenId }) { it[revoked] = true }
    }

    suspend fun revokeAllForUser(userId: UUID): Int = DatabaseFactory.dbQuery {
        RefreshTokens.update({ RefreshTokens.userId eq userId }) { it[revoked] = true }
    }

    suspend fun purgeExpired(now: Long = Instant.now().epochSecond): Int = DatabaseFactory.dbQuery {
        RefreshTokens.deleteWhere { expiresAt lessEq now }
    }
}
