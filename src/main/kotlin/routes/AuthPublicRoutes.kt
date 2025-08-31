package routes

import MessageResponse
import RefreshRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.TokenService

fun Route.authPublicRoutes(tokenService: TokenService) {
    route("/api/auth") {

        post("/refresh") {
            val req = call.receive<RefreshRequest>()
            val pair = tokenService.rotateRefresh(
                old = req.refreshToken,
                device = req.device,
                ipHash = null // or derive from call.request.origin.remoteHost
            )
            if (pair == null) {
                call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid or expired refresh token"))
            } else {
                call.respond(HttpStatusCode.OK, pair)
            }
        }

        // TODO: add /register and /login here if you haven’t already
        // post("/register") { ... }
        // post("/login")    { ... }
    }
}
