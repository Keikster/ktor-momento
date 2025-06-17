package com.example.database.tables

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/momento",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "<PASSWORD>"
        )

        transaction {
            SchemaUtils.create(Momentos)
        }
    }
}