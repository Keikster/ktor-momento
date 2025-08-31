package service

import TokenPairResponse
import model.UserPublic
import repository.UserRepository
import java.util.UUID

class UserService(
    private val repo: UserRepository = UserRepository,
    private val tokens: TokenService
) {
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        device: String?,
        ipHash: String?
    ): TokenPairResponse {
        require(email.contains("@")) { "Invalid email" }
        require(password.length >= 8) { "Password must be at least 8 characters" }
        require(firstName.isNotBlank()) { "First name required" }
        require(lastName.isNotBlank()) { "Last name required" }

        val row = repo.insert(email, password.toCharArray(), firstName.trim(), lastName.trim())
        return tokens.newTokenPair(row.id, device, ipHash)
    }

    suspend fun login(email: String, password: String, device: String?, ipHash: String?): TokenPairResponse? {
        val row = repo.authenticate(email, password.toCharArray()) ?: return null
        return tokens.newTokenPair(row.id, device, ipHash)
    }

    suspend fun getById(id: UUID): UserPublic? =
        repo.selectById(id)?.let {
            UserPublic(
                id = it.id,
                email = it.email,
                createdAt = it.createdAt,
                displayName = it.displayName   // computed from first + last
            )
        }

    suspend fun changePassword(userId: UUID, oldPassword: String, newPassword: String): Boolean {
        require(newPassword.length >= 8) { "New password must be at least 8 characters" }
        val user = repo.selectById(userId) ?: return false
        val ok = security.PasswordHasher.verify(oldPassword.toCharArray(), user.passwordHash)
        if (!ok) return false
        val updated = repo.updatePassword(userId, newPassword.toCharArray())
        return updated == 1
    }

    suspend fun refresh(refreshToken: String, device: String?, ipHash: String?): TokenPairResponse? =
        tokens.rotateRefresh(refreshToken, device, ipHash)
}
