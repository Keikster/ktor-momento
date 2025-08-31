// src/main/kotlin/com/example/routes/UserRoutes.kt

import service.UserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

// Mount with: routing { authenticate("auth-jwt") { userRoutes(userService) } }
fun Route.userRoutes(userService: UserService) {
    authenticate("auth-jwt") {
        route("/users") {
            get("/{id}") {
                val idParam = call.parameters["id"]
                val id = runCatching { UUID.fromString(idParam) }.getOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid UUID")

                val user = userService.getById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respond(user)
            }
        }
    }
}
