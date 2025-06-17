package com.example.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.date

object Momentos : IntIdTable() {
        val userId = integer("user_id").nullable()
        val fullName = text("full_name")
        val birthDate = date("birth_date").nullable()
        val deathDate = date("death_date").nullable()
        val bio = text("bio").nullable()
        val coverPhotoUrl = text("cover_photo_url").nullable()
        val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
        val isDeleted = bool("is_deleted").default(false)
        val deletedAt = datetime("deleted_at").nullable()
    }
