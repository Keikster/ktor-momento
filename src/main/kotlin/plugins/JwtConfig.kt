package plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import auth.AuthUserPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.get
import service.TokenService
import repository.UserRepository
import java.util.UUID

fun Application.configureJwt(): TokenService {
    val cfg = environment.config
    val issuer  = cfg.property("jwt.issuer").getString()
    val audience = cfg.property("jwt.audience").getString()
    val secret  = cfg.property("jwt.secret").getString()
    val accessTtlSeconds  = cfg.propertyOrNull("jwt.accessTtlSeconds")?.getString()?.toLong() ?: 15L * 60L
    val refreshTtlSeconds = cfg.propertyOrNull("jwt.refreshTtlSeconds")?.getString()?.toLong() ?: 30L * 24L * 3600L

    val algorithm = Algorithm.HMAC256(secret)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "momento"
            verifier(
                JWT.require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { cred ->
                val sub = cred.payload.subject ?: return@validate null
                val email = cred.payload.getClaim("email")?.asString() ?: return@validate null
                val userId = runCatching { UUID.fromString(sub) }.getOrNull() ?: return@validate null
                AuthUserPrincipal(userId = userId, email = email)
            }
            challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized) }
        }
    }

    // Return the TokenService; look up email via repo during refresh rotation
    return TokenService(
        jwtIssuer = issuer,
        jwtAudience = audience,
        jwtSecret = secret,
        accessTtlSeconds = accessTtlSeconds,
        refreshTtlSeconds = refreshTtlSeconds,
        getEmail = { id ->
            withContext(Dispatchers.IO) {
                UserRepository.selectById(id)?.email   // <-- use selectById, not findById
            }
        }
    )
}
