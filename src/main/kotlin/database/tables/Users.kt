package database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Users : Table("users") {
    val id = uuid("id")
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = text("password_hash")
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    override val primaryKey = PrimaryKey(id)
}

object RefreshTokens : Table("refresh_tokens") {
    val tokenId = varchar("token_id", 64)
    val userId = uuid("user_id").index()
    val tokenHash = text("token_hash")
    val issuedAt = long("issued_at")
    val expiresAt = long("expires_at").index()
    val device = varchar("device", 128).nullable()
    val ipHash = varchar("ip_hash", 128).nullable()
    val revoked = bool("revoked").default(false).index()
    override val primaryKey = PrimaryKey(tokenId)
}