package com.example

import com.example.serialization.UuidAsStringSerializer
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.util.UUID

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                isLenient = false
                prettyPrint = false
                Json {
                    serializersModule = SerializersModule {
                        contextual(UUID::class, UuidAsStringSerializer)
                    }
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            }
        )
    }
}
