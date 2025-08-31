package com.example.security

import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

object Keys {

    private fun base64DecodePemBody(pem: String, begin: String, end: String): ByteArray {
        val start = pem.indexOf(begin)
        val finish = pem.indexOf(end)
        require(start >= 0 && finish > start) { "PEM is missing $begin / $end block" }
        val body = pem.substring(start + begin.length, finish)
        // remove whitespace, including Windows \r and any spaces
        val sanitized = body.filterNot { it.isWhitespace() }
        return Base64.getDecoder().decode(sanitized)
    }

    private fun isPkcs1Private(pem: String) =
        pem.contains("-----BEGIN RSA PRIVATE KEY-----")

    private fun isPkcs1Public(pem: String) =
        pem.contains("-----BEGIN RSA PUBLIC KEY-----")

    val publicKey: RSAPublicKey by lazy {
        val pem = System.getenv("MOMENTO_JWT_RS256_PUBLIC_PEM")
            ?: error("Missing env MOMENTO_JWT_RS256_PUBLIC_PEM")
        require(!isPkcs1Public(pem)) {
            "Public key is PKCS#1 (-----BEGIN RSA PUBLIC KEY-----). Provide an X.509 public key (-----BEGIN PUBLIC KEY-----)."
        }
        val der = base64DecodePemBody(
            pem,
            "-----BEGIN PUBLIC KEY-----",
            "-----END PUBLIC KEY-----"
        )
        val spec = X509EncodedKeySpec(der)
        KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
    }

    val privateKey: RSAPrivateKey by lazy {
        val pem = System.getenv("MOMENTO_JWT_RS256_PRIVATE_PEM")
            ?: error("Missing env MOMENTO_JWT_RS256_PRIVATE_PEM")
        require(!isPkcs1Private(pem)) {
            "Private key is PKCS#1 (-----BEGIN RSA PRIVATE KEY-----). Provide a PKCS#8 private key (-----BEGIN PRIVATE KEY-----)."
        }
        val der = base64DecodePemBody(
            pem,
            "-----BEGIN PRIVATE KEY-----",
            "-----END PRIVATE KEY-----"
        )
        val spec = PKCS8EncodedKeySpec(der)
        KeyFactory.getInstance("RSA").generatePrivate(spec) as RSAPrivateKey
    }
}
