import com.example.configureSerialization
import database.DatabaseFactory
import com.example.di.appModules

import routes.authRoutes
import routes.userRoutes

import security.configureJwt   // your configureJwt(usersRepo) function lives here
import service.TokenService
import service.UserService
import repository.UserRepository

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.*

import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.ktor.ext.get   // <-- Koin 'get()' extension to resolve beans

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // 1) DB
    DatabaseFactory.init()

    // 2) DI
    install(Koin) {
        slf4jLogger()
        modules(appModules)
    }

    // 3) JSON
    configureSerialization()

    // 4) Repos via Koin
    val usersRepo: UserRepository = get()     // <-- this fixes 'usersRepo' not found

    // 5) JWT (returns TokenService) — pass the repo so refresh can look up email
    val tokenService: TokenService = configureJwt(usersRepo)

    // 6) Services
    val userService = UserService(tokens = tokenService)

    // 7) Routes
    configureRouting(tokenService, userService)
}

/** Central place to register all routes. */
fun Application.configureRouting(
    tokenService: TokenService,
    userService: UserService
) {
    routing {
        authRoutes(tokenService, userService) // /api/auth/*
        userRoutes(userService)               // /api/me, /api/users/{id}
    }
}
