package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateMomentoRequest(
    val fullName: String,
    val birthDate: String?,
    val deathDate: String?,
    val bio: String?,
    val coverPhotoUrl: String?
)
