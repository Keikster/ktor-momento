package com.example.model

import kotlinx.datetime.Instant
import java.util.UUID

data class UserWithPassword(
    val id: UUID,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val createdAt: Instant
)
