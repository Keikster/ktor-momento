@file:UseSerializers(UuidAsStringSerializer::class)

package model

import com.example.serialization.UuidAsStringSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class UserPublic(
    val id: UUID,
    val email: String,
    val createdAt: Instant,
    val displayName: String? = null  // <- make optional since repo row doesn’t have it yet
)
