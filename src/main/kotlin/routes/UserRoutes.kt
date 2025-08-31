package routes

import auth.AuthUserPrincipal
import MessageResponse
import service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.userRoutes(userService: UserService) {
    authenticate("auth-jwt") {
        get("/api/me") {
            val p = call.principal<AuthUserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, MessageResponse("No principal"))

            val user = userService.getById(p.userId)
                ?: return@get call.respond(HttpStatusCode.NotFound, MessageResponse("User not found"))

            call.respond(user) // or user.toPublic()
        }
    }

    // Optional admin/public route to fetch other users by id
    get("/api/users/{id}") {
        val idParam = call.parameters["id"]
        val id = runCatching { UUID.fromString(idParam) }.getOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid id"))

        val user = userService.getById(id)
            ?: return@get call.respond(HttpStatusCode.NotFound, MessageResponse("User not found"))

        call.respond(user) // or user.toPublic()
    }
}
