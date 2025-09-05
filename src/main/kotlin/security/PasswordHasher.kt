package security

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import kotlin.math.max

object PasswordHasher {private val argon2: Argon2 by lazy {
    // saltLen=16, hashLen=32
    Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 16, 32)
}


    private val memoryKb: Int by lazy {

        128 * 1024
    }
    private val iterations = 3
    private val parallelism = max(2, Runtime.getRuntime().availableProcessors() / 2)

    private val pepper: String by lazy {
        System.getenv("MOMENTO_PEPPER") ?: error("MOMENTO_PEPPER not set")
    }

    fun hash(plainPassword: CharArray): String {
        val material = (pepper + String(plainPassword)).toCharArray()
        return argon2.hash(iterations, memoryKb, parallelism, material)
    }

    fun verify(plainPassword: CharArray, storedPhc: String): Boolean {
        val material = (pepper + String(plainPassword)).toCharArray()

        return argon2.verify(storedPhc, material)
    }

}