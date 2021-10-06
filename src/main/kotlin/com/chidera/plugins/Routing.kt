package com.chidera.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import registerItemRoutes
import registerShopRoutes

fun Application.configureRouting() {
    registerShopRoutes()
    registerItemRoutes()

    routing {
        get("/") {
                call.respondText("Welcome to Shop World!")
            }
    }
}
