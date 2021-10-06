import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.registerItemRoutes() {
    routing {
        listItemRoute()
        addItemRoute()
        deleteItemRoute()
    }
}

fun Route.listItemRoute() {
    route("/shop") {
        get("{id}/items") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val shop = shopStorage.find { it.id == id } ?: return@get call.respondText(
                "No shop with id $id",
                status = HttpStatusCode.NotFound
            )
            call.respond(shop.items)
        }
        get("{id}/items/{itemId}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val itemId = call.parameters["itemId"] ?: return@get call.respondText(
                "Missing or malformed item id",
                status = HttpStatusCode.BadRequest
            )
            val shop = shopStorage.find { it.id == id } ?: return@get call.respondText(
                "No shop with id $id",
                status = HttpStatusCode.NotFound
            )
            val item = shop.items.find { it.id == itemId } ?: return@get call.respondText(
                "No item with id $itemId"
            )
            call.respond(item)
        }
    }
}

fun Route.addItemRoute() {
    route("/shop") {
        post("{id}") {
            val item = call.receive<Item>()
            val id = call.parameters["id"] ?: return@post call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val shop = shopStorage.find { it.id == id } ?: return@post call.respondText(
                "No shop with id $id",
                status = HttpStatusCode.NotFound
            )
            shop.items.add(item)
            call.respondText(
                "Item stored successfully",
                status = HttpStatusCode.Created
            )
        }
    }
}

fun Route.deleteItemRoute() {
    route("/shop") {
        delete("{id}/items/{itemId}") {
            val id = call.parameters["id"] ?: return@delete call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val itemId = call.parameters["itemId"] ?: return@delete call.respondText(
                "Missing or malformed item id",
                status = HttpStatusCode.BadRequest
            )
            val shop = shopStorage.find { it.id == id } ?: return@delete call.respondText(
                "No shop with id $id",
                status = HttpStatusCode.NotFound
            )
            if (shop.items.removeIf { it.id == itemId }) {
                call.respondText(
                    "Item removed successfully",
                    status = HttpStatusCode.Accepted
                )
            } else {
                call.respondText(
                    "Item not found",
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }
}