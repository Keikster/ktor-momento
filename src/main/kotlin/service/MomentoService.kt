package com.example.service

import com.example.service.MomentoRepository
import com.example.database.tables.Momentos
import com.example.model.CreateMomentoRequest
import com.example.model.MomentoDto
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import kotlinx.datetime.LocalDate as KxLocalDate
import kotlinx.datetime.toLocalDate

class MomentoService(private val repo: MomentoRepository) {

    fun getMomentos(): List<MomentoDto> = repo.getAll()

    fun createMomento(request: CreateMomentoRequest): MomentoDto = transaction {
        val id = Momentos.insertAndGetId {
            it[fullName] = request.fullName
            it[birthDate] = request.birthDate?.let { KxLocalDate.parse(it) }
            it[deathDate] = request.deathDate?.let { KxLocalDate.parse(it) }
            it[bio] = request.bio
            it[coverPhotoUrl] = request.coverPhotoUrl
        }.value

        repo.getById(id) ?: throw IllegalStateException("Failed to retrieve newly created momento with ID: $id")
    }
}
