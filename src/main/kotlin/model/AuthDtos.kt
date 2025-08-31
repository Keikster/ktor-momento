import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.gradle.internal.DisplayName
import java.util.UUID

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val device: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val device: String? = null
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
    val device: String? = null
)

@Serializable
data class TokenPairResponse(
    val accessToken: String,
    val accessExpiresAt: Long,
    val refreshToken: String,
    val refreshExpiresAt: Long
)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)

@Serializable
data class UserPublic(
    @Contextual val id: UUID,
    val email: String,
    val createdAt: Instant,
    val displayName: DisplayName
)