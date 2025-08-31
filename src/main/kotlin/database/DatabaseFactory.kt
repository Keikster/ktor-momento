package database

import com.example.database.tables.Momentos
import database.tables.Users
import database.tables.RefreshTokens
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object DatabaseFactory {

    fun init() {
        // Try environment variables first (prod), then .env (dev)
        val dotEnv = runCatching {
            dotenv {
                ignoreIfMissing = true
                ignoreIfMalformed = true
            }
        }.getOrNull()

        val url  = System.getenv("MOMENTO_DB_URL")
            ?: dotEnv?.get("DB_URL")
            ?: "jdbc:postgresql://localhost:5432/momento"

        val user = System.getenv("MOMENTO_DB_USER")
            ?: dotEnv?.get("DB_USER")
            ?: "postgres"

        val pass = System.getenv("MOMENTO_DB_PASS")
            ?: dotEnv?.get("DB_PASSWORD")
            ?: ""

        val env  = dotEnv?.get("ENV") ?: System.getenv("ENV") ?: "development"

        println("🌱 Connecting to database in [$env] mode → $url (user=$user)")

        Database.connect(
            url = url,
            driver = "org.postgresql.Driver",
            user = user,
            password = pass
        )

        // Sensible default for read-stability
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

        // Create non-destructively; safe to run at startup
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users,
                RefreshTokens,
                Momentos
            )
        }
    }

    // Suspended helper for DB calls
    suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
