// ========================= repository/UserRepository.kt =========================
package repository


import database.DatabaseFactory
import database.tables.Users
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select            // ★ brings in the DSL 'select { }'
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq   // ★ brings in 'eq'
import org.jetbrains.exposed.sql.selectAll
import security.PasswordHasher                     // ★ use your security package
import java.util.Arrays
import java.util.UUID

data class UserRow(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val createdAt: Instant,   // Exposed Kotlinx Instant
    val firstName: String,
    val lastName: String
) {
    val displayName: String get() = "$firstName $lastName".trim()
}

object UserRepository {

    // ---------- Reads ----------

    suspend fun findByEmail(email: String): UserRow? = DatabaseFactory.dbQuery {
        val normalized = email.trim().lowercase()
        Users
            .selectAll().where { Users.email eq normalized }
            .limit(1)
            .singleOrNull()
            ?.toUserRow()
    }

    suspend fun selectById(userId: UUID): UserRow? = DatabaseFactory.dbQuery {
        Users
            .selectAll().where { Users.id eq userId }
            .limit(1)
            .singleOrNull()
            ?.toUserRow()
    }

    // ---------- Writes ----------

    /**
     * Insert a new user. Hashes the password and clears the provided CharArray.
     * Returns the full row read back from DB (so createdAt is accurate).
     */
    suspend fun insert(
        email: String,
        passwordPlain: CharArray,
        firstName: String,
        lastName: String
    ): UserRow = DatabaseFactory.dbQuery {
        val normalized = email.trim().lowercase()

        // Friendly pre-check (you should also have a unique index at DB level)
        val exists = Users
            .selectAll().where { Users.email eq normalized }
            .limit(1)
            .any()
        require(!exists) { "Email already in use" }

        val phc = try {
            security.PasswordHasher.hash(passwordPlain)
        } finally {
            Arrays.fill(passwordPlain, '\u0000')
        }

        val id = UUID.randomUUID()
        Users.insert {
            it[Users.id] = id
            it[Users.email] = normalized
            it[Users.passwordHash] = phc
            it[Users.firstName] = firstName.trim()
            it[Users.lastName] = lastName.trim()
            // createdAt uses DB default (now())
        }

        Users
            .selectAll().where { Users.id eq id }
            .limit(1)
            .single()
            .toUserRow()
    }

    suspend fun updatePassword(userId: UUID, newPassword: CharArray): Int = DatabaseFactory.dbQuery {
        val phc = try {
            security.PasswordHasher.hash(newPassword)
        } finally {
            Arrays.fill(newPassword, '\u0000')
        }
        Users.update({ Users.id eq userId }) { it[passwordHash] = phc }
    }

    // ---------- Auth helper ----------

    /**
     * Verify a plaintext password for the given email.
     * Returns the UserRow on success, or null on failure.
     */
    suspend fun authenticate(email: String, plainPassword: CharArray): UserRow? {
        val user = findByEmail(email) ?: return null
        val verified = try {
            security.PasswordHasher.verify(plainPassword, user.passwordHash)
        } finally {
            Arrays.fill(plainPassword, '\u0000')
        }
        return if (verified) user else null
    }

    // ---------- Mapping ----------

    private fun ResultRow.toUserRow() = UserRow(
        id = this[Users.id],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        createdAt = this[Users.createdAt],          // kotlinx.datetime.Instant
        firstName = this[Users.firstName],
        lastName = this[Users.lastName]
    )
}