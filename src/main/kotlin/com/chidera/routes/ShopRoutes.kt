import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.registerShopRoutes() {
    routing {
        shopRouting()
    }
}

fun Route.shopRouting() {
    route("/shop") {
        get {
            if (shopStorage.isNotEmpty()) {
                call.respond(shopStorage)
            } else {
                call.respondText(
                    "No shop found",
                    status = HttpStatusCode.NotFound
                )
            }
        }
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val shop = shopStorage.find { it.id == id } ?: return@get call.respondText(
                "No shop with id $id",
                status = HttpStatusCode.NotFound
            )
            call.respond(shop)
        }
        post {
            val shop = call.receive<Shop>()
            shopStorage.add(shop)
            call.respondText(
                "Shop created successfully",
                status = HttpStatusCode.Created
            )
        }
        delete("{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (shopStorage.removeIf { it.id == id }) {
                call.respondText(
                    "Shop deleted successfully",
                    status = HttpStatusCode.Accepted
                )
            } else {
                call.respondText(
                    "Not found",
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }
}