package com.example.database.tables

import database.tables.Users
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Momentos : IntIdTable("momentos") {

        val userId = uuid("user_id")
                .references(
                        Users.id,
                        onDelete = ReferenceOption.CASCADE
                )

        val fullName = text("full_name")

        val birthDate = date("birth_date").nullable()

        val deathDate = date("death_date").nullable()

        val bio = text("bio").nullable()

        val coverPhotoUrl = text("cover_photo_url").nullable()

        val createdAt =
                datetime("created_at")
                        .defaultExpression(CurrentDateTime)

        val isDeleted =
                bool("is_deleted")
                        .default(false)

        val deletedAt =
                datetime("deleted_at")
                        .nullable()
}