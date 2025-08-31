package auth

import java.util.UUID


data class AuthUserPrincipal(
    val userId: UUID,
    val email: String
)
