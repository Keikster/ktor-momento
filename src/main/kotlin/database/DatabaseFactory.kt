package com.example.database

import com.example.database.tables.Momentos
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val dotenv = dotenv()

        val env = dotenv["ENV"] ?: "development"

        val dbUrl = dotenv["DB_URL"] ?: "jdbc:postgresql://localhost:5432/momento"
        val dbUser = dotenv["DB_USER"] ?: "postgres"
        val dbPassword = dotenv["DB_PASSWORD"] ?: "<PASSWORD>"

        println("🌱 Connecting to database in [$env] mode")

        if (dotenv["DB_URL"] == null) {
            println("⚠️  DB_URL not found in .env — using default localhost URL")
        }

        Database.connect(
            url = dbUrl,
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPassword
        )

        transaction {
            SchemaUtils.create(Momentos)
        }
    }
}