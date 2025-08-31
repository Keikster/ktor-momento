package service

import TokenPairResponse
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import repository.RefreshTokenRepository   // if your repo is under `service`, change to: service.RefreshTokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import io.ktor.util.hex
import java.util.Date
import java.util.UUID

class TokenService(
    private val jwtIssuer: String,
    private val jwtAudience: String,
    private val jwtSecret: String,
    private val accessTtlSeconds: Long = 15 * 60,          // 15m
    private val refreshTtlSeconds: Long = 30L * 24 * 3600, // 30d
    // NEW: decoupled way to get email for a userId (used during refresh rotation)
    private val getEmail: suspend (UUID) -> String?
) {
    private val alg = Algorithm.HMAC256(jwtSecret)

    /** Mint a new access+refresh token pair for a user. */
    suspend fun newTokenPair(
        userId: UUID,
        email: String,
        device: String? = null,
        ipHash: String? = null
    ): TokenPairResponse {
        val nowSec = Clock.System.now().epochSeconds
        val accessExp = nowSec + accessTtlSeconds
        val refreshExp = nowSec + refreshTtlSeconds

        val accessToken = JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withSubject(userId.toString())   // validator reads this as UUID
            .withClaim("email", email)        // validator reads this as email
            // .withClaim("uid", userId.toString()) // optional; not needed if you use `sub`
            .withExpiresAt(Date(accessExp * 1000))
            .sign(alg)

        // Opaque refresh (random) + server-side hash
        val jti = UUID.randomUUID().toString().replace("-", "")
        val refreshPlain = generateOpaqueRefresh()
        val refreshHash = com.example.security.PasswordHasher.hash(refreshPlain.toCharArray())

        withContext(Dispatchers.IO) {
            RefreshTokenRepository.insert(
                tokenId = jti,
                userId = userId,
                tokenHash = refreshHash,
                issuedAt = nowSec,
                expiresAt = refreshExp,
                device = device,
                ipHash = ipHash
            )
        }

        return TokenPairResponse(
            accessToken = accessToken,
            accessExpiresAt = accessExp,
            refreshToken = "$jti.$refreshPlain",
            refreshExpiresAt = refreshExp
        )
    }

    /** Validate + rotate a refresh token, returning a fresh pair. */
    suspend fun rotateRefresh(
        old: String,
        device: String? = null,
        ipHash: String? = null
    ): TokenPairResponse? {
        val parts = old.split('.', limit = 2); if (parts.size != 2) return null
        val (jti, secret) = parts

        val row = withContext(Dispatchers.IO) { RefreshTokenRepository.find(jti) } ?: return null
        val nowSec = Clock.System.now().epochSeconds
        if (row.revoked || row.expiresAt <= nowSec) return null

        val ok = com.example.security.PasswordHasher.verify(secret.toCharArray(), row.tokenHash)
        java.util.Arrays.fill(secret.toCharArray(), '\u0000') // clear sensitive data
        if (!ok) return null

        // revoke the old refresh
        withContext(Dispatchers.IO) { RefreshTokenRepository.revoke(jti) }

        // NEED EMAIL to mint access token consistent with your validator
        val email = getEmail(row.userId) ?: return null

        return newTokenPair(row.userId, email, device, ipHash)
    }

    private fun generateOpaqueRefresh(): String {
        val bytes = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        return hex(bytes) // URL-safe hex
    }
}