package database.tables


import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object RefreshTokens : Table("refresh_tokens") {
    val tokenId   = varchar("token_id", 64) // jti
    val userId    = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val tokenHash = varchar("token_hash", 255) // Argon2/BCrypt hash
    val issuedAt  = long("issued_at")   // epoch seconds
    val expiresAt = long("expires_at")  // epoch seconds
    val device    = varchar("device", 128).nullable()
    val ipHash    = varchar("ip_hash", 64).nullable()
    val revoked   = bool("revoked").default(false)

    override val primaryKey = PrimaryKey(tokenId)
    init {
        index(true, tokenId)
        index(false, userId)
        index(false, expiresAt)
        index(false, revoked)
    }
}