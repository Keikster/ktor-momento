package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class MomentoDto(
    val id: Int,
    val fullName: String,
    val birthDate: String?,
    val deathDate: String?,
    val bio: String?,
    val coverPhotoUrl: String?,
    val createdAt: String,
    val isDeleted: Boolean,
    val deletedAt: String?
)


