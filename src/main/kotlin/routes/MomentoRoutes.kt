package routes

import MessageResponse
import com.example.database.tables.Momentos
import auth.AuthUserPrincipal
import database.DatabaseFactory.dbQuery
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate
import model.CreateMomentoRequest
import org.jetbrains.exposed.sql.insert

fun Route.momentoRoutes() {

    authenticate("auth-jwt") {

        route("/api/momentos") {

            post {
                val principal = call.principal<AuthUserPrincipal>()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        MessageResponse("Unauthorized")
                    )

                val request = call.receive<CreateMomentoRequest>()

                if (request.fullName.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Name is required")
                    )
                }

                val momentoId = dbQuery {
                    Momentos.insert { row ->
                        row[userId] = principal.userId
                        row[fullName] = request.fullName.trim()

                        row[birthDate] = request.birthDate
                            ?.takeIf { it.isNotBlank() }
                            ?.let { LocalDate.parse(it) }

                        row[deathDate] = request.deathDate
                            ?.takeIf { it.isNotBlank() }
                            ?.let { LocalDate.parse(it) }

                        row[bio] = request.bio
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }

                        row[coverPhotoUrl] = request.coverPhotoUrl
                            ?.takeIf { it.isNotBlank() }
                    } get Momentos.id
                }

                call.respond(
                    HttpStatusCode.Created,
                    MessageResponse(
                        "Momento created with id ${momentoId.value}"
                    )
                )
            }
        }
    }
}