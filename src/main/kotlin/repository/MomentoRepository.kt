package com.example.service

import com.example.database.tables.Momentos
import com.example.model.MomentoDto
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class MomentoRepository {
    fun getAll(): List<MomentoDto> = transaction {
        Momentos.selectAll().map {
            MomentoDto(
                id = it[Momentos.id].value,
                fullName = it[Momentos.fullName],
                birthDate = it[Momentos.birthDate]?.toString(),
                deathDate = it[Momentos.deathDate]?.toString(),
                bio = it[Momentos.bio],
                coverPhotoUrl = it[Momentos.coverPhotoUrl],
                createdAt = it[Momentos.createdAt].toString(),
                isDeleted = it[Momentos.isDeleted],
                deletedAt = it[Momentos.deletedAt]?.toString()


            )
        }
    }

    fun getById(id: Int): MomentoDto? = transaction {
        Momentos.selectAll().where { Momentos.id eq id }.map {
            MomentoDto(
                id = it[Momentos.id].value,
                fullName = it[Momentos.fullName],
                birthDate = it[Momentos.birthDate]?.toString(),
                deathDate = it[Momentos.deathDate]?.toString(),
                bio = it[Momentos.bio],
                coverPhotoUrl = it[Momentos.coverPhotoUrl],
                createdAt = it[Momentos.createdAt].toString(),
                isDeleted = it[Momentos.isDeleted],
                deletedAt = it[Momentos.deletedAt]?.toString()
            )
        }.singleOrNull()
    }


}