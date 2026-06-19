package repository

import database.DatabaseFactory
import database.tables.Users
import database.tables.Users.passwordHash
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.Arrays
import java.util.UUID

data class UserRow(
    val id: UUID,
    val email: String,
    val passwordHash: String?,
    val createdAt: Instant,
    val firstName: String,
    val lastName: String,
    val firebaseUid: String? = null
) {
    val displayName: String get() = "$firstName $lastName".trim()
}

object UserRepository {

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

    suspend fun findByFirebaseUid(firebaseUid: String): UserRow? = DatabaseFactory.dbQuery {
        Users
            .selectAll().where { Users.firebaseUid eq firebaseUid }
            .limit(1)
            .singleOrNull()
            ?.toUserRow()
    }

    suspend fun insert(
        email: String,
        passwordPlain: CharArray,
        firstName: String,
        lastName: String
    ): UserRow = DatabaseFactory.dbQuery {
        val normalized = email.trim().lowercase()

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

    suspend fun insertFirebaseUser(email: String, firebaseUid: String): UserRow = DatabaseFactory.dbQuery {
        val normalized = email.trim().lowercase()

        val existing = Users.selectAll().where { Users.email eq normalized }.limit(1).singleOrNull()
        if (existing != null) {
            val row = existing.toUserRow()
            if (row.firebaseUid == null) {
                Users.update({ Users.id eq row.id }) { it[Users.firebaseUid] = firebaseUid }
                return@dbQuery Users.selectAll().where { Users.id eq row.id }.limit(1).single().toUserRow()
            }
            return@dbQuery row
        }

        val id = UUID.randomUUID()

        Users.insert {
            it[Users.id] = id
            it[Users.email] = normalized
            it[Users.passwordHash] = null
            it[Users.firstName] = "Momento"
            it[Users.lastName] = "User"
            it[Users.firebaseUid] = firebaseUid
        }

        Users.selectAll().where { Users.id eq id }.limit(1).single().toUserRow()
    }

    suspend fun attachFirebaseUid(userId: UUID, firebaseUid: String): Int = DatabaseFactory.dbQuery {
        Users.update({ Users.id eq userId }) {
            it[Users.firebaseUid] = firebaseUid
        }
    }

    suspend fun authenticate(email: String, plainPassword: CharArray): UserRow? {
        val user = findByEmail(email) ?: return null
        val hash = user.passwordHash ?: return null

        val verified = try {
            security.PasswordHasher.verify(plainPassword, hash)
        } finally {
            Arrays.fill(plainPassword, '\u0000')
        }

        return if (verified) user else null
    }

    private fun ResultRow.toUserRow() = UserRow(
        id = this[Users.id],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        createdAt = this[Users.createdAt],
        firstName = this[Users.firstName],
        lastName = this[Users.lastName],
        firebaseUid = this[Users.firebaseUid]
    )
}