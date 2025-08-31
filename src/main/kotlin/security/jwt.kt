package security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import service.TokenService // <- use your service TokenService

/**
 * Installs JWT authentication and returns a TokenService for minting tokens.
 *
 * HOCON config (application.conf / application-dev.conf):
 *
 * jwt {
 *   issuer = "http://localhost:8080"
 *   audience = "momento-clients-dev"
 *   secret = ${?JWT_SECRET}       # e.g. $env:JWT_SECRET="dev-secret"
 * }
 */
fun Application.configureJwt(): TokenService {
    val issuer   = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val secret   = environment.config.property("jwt.secret").getString()

    val algorithm = Algorithm.HMAC256(secret)

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            // Access tokens minted by TokenService include "uid" claim (user id)
            validate { cred ->
                val uid = cred.payload.getClaim("uid")?.asString()
                if (!uid.isNullOrBlank()) JWTPrincipal(cred.payload) else null
            }
        }
    }

    // Return the TokenService instance used by routes/services
    return TokenService(
        jwtIssuer = issuer,
        jwtAudience = audience,
        jwtSecret = secret,
        getEmail =
    )
}
