package com.example

import com.example.di.appModules
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
    install(Koin) {
        slf4jLogger()
        modules(appModules)
    }

    install(ContentNegotiation) {
        json()
    }

    DatabaseFactory.init()
    configureRouting()
}
