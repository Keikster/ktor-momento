package routes

import ChangePasswordRequest
import auth.AuthUserPrincipal
import MessageResponse
import service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authProtectedRoutes(userService: UserService) {
    authenticate("auth-jwt") {
        post("/api/auth/change-password") {
            val p = call.principal<AuthUserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("No principal"))

            val req = call.receive<ChangePasswordRequest>()
            val msg = userService.changePassword(p.userId, req)
            call.respond(HttpStatusCode.OK, msg)
        }
    }
}
