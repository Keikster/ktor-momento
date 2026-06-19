package routes

import ChangePasswordRequest
import FirebaseTokenRequest
import LoginRequest
import MessageResponse
import RefreshRequest
import RegisterRequest
import auth.AuthUserPrincipal
import com.google.firebase.auth.FirebaseAuth
import service.UserService
import io.ktor.http.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.origin
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import security.hmacSha256Hex
import service.TokenService
import java.util.UUID

fun Route.authRoutes(
    tokenService: TokenService,
    userService: UserService
) {
    route("/api/auth") {

        post("/register") {
            val req = call.receive<RegisterRequest>()
            val device = call.request.headers["X-Device"] ?: "unknown"
            val ipHash = call.clientIpHash()   // <-- fixed

            try {
                val tokens = userService.register(
                    email = req.email,
                    password = req.password,
                    firstName = req.firstName,
                    lastName = req.lastName,
                    device = req.device ?: device,
                    ipHash = ipHash
                )
                call.respond(HttpStatusCode.Created, tokens)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message ?: "Bad request"))
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.Conflict, MessageResponse(e.message ?: "Conflict"))
            } catch (_: Exception) {
                call.respond(HttpStatusCode.InternalServerError, MessageResponse("Server error"))
            }
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val device = call.request.headers["X-Device"] ?: "unknown"
            val ipHash = call.clientIpHash()   // <-- fixed

            val tokens = userService.login(
                email = req.email,
                password = req.password,
                device = req.device ?: device,
                ipHash = ipHash
            )
            if (tokens == null) {
                call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid credentials"))
            } else {
                call.respond(HttpStatusCode.OK, tokens)
            }
        }

        post("/firebase") {
            val req = call.receive<FirebaseTokenRequest>()
            val device = call.request.headers["X-Device"] ?: "unknown"
            val ipHash = call.clientIpHash()

            try {
                // Verify the Firebase ID token (requires Firebase Admin SDK initialized)
                val decoded = FirebaseAuth.getInstance().verifyIdToken(req.idToken)

                val firebaseUid = decoded.uid
                val email = decoded.email ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse("Firebase token missing email")
                )

                val tokens = userService.loginWithFirebase(
                    firebaseUid = firebaseUid,
                    email = email,
                    device = device,
                    ipHash = ipHash
                )

                call.respond(HttpStatusCode.OK, tokens)

            } catch (e: Exception) {
                // Token invalid, expired, wrong project, admin not initialized, etc.
                call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid Firebase token"))
            }
        }

        post("/refresh") {
            val req = call.receive<RefreshRequest>()
            val device = call.request.headers["X-Device"] ?: "unknown"
            val ipHash = call.clientIpHash()   // <-- fixed

            val tokens = userService.refresh(
                refreshToken = req.refreshToken,
                device = req.device ?: device,
                ipHash = ipHash
            )
            if (tokens == null) {
                call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid or expired refresh token"))
            } else {
                call.respond(HttpStatusCode.OK, tokens)
            }
        }

        authenticate("auth-jwt") {
            post("/change-password") {
                val p = call.principal<AuthUserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("No principal"))

                val payload = call.receive<ChangePasswordRequest>()
                val ok = userService.changePassword(p.userId, payload.oldPassword, payload.newPassword)
                if (!ok) {
                    call.respond(HttpStatusCode.Unauthorized, MessageResponse("Old password incorrect"))
                } else {
                    call.respond(HttpStatusCode.OK, MessageResponse("Password changed"))
                }
            }
        }
    }
}

/* --- helpers (same file) --- */

private fun ApplicationCall.clientIp(): String =
    request.headers["X-Forwarded-For"]?.substringBefore(',')?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: request.origin.remoteHost

private fun ApplicationCall.userAgent(): String =
    request.headers["User-Agent"] ?: ""

private fun ApplicationCall.clientIpHash(): String {
    // use jwt.secret as a pepper (or replace with your own app.pepper key)
    val secret = application.environment.config.property("jwt.secret").getString()
    val data = "${clientIp()}|${userAgent()}"
    return security.hmacSha256Hex(secret, data)
}