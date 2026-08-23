package model

import kotlinx.serialization.Serializable

@Serializable
data class CreateMomentoRequest(
    val fullName: String,
    val birthDate: String? = null,
    val deathDate: String? = null,
    val bio: String? = null,
    val coverPhotoUrl: String? = null
)