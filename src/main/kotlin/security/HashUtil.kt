package security

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets.UTF_8

/** HMAC-SHA256(data) using a secret pepper, returns lowercase hex. */
fun hmacSha256Hex(secret: String, data: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(UTF_8), "HmacSHA256"))
    val bytes = mac.doFinal(data.toByteArray(UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}
