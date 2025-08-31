package model

import kotlinx.datetime.Instant
import java.util.UUID

data class UserDto (
    val id: UUID,
    val email: String,
    val displayName: String,
    val createdAt: Instant
    )

data class CreatedUserRequest(
    val email: String,
    val displayName: String,
    val passwordPlain: String
)