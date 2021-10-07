import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.registerShopRoutes() {
    routing {
        shopRouting()
    }
}

fun Route.shopRouting() {
    route("/shop") {
        get {
            val shops = transaction {
                Shop.all().map(Shop::toShop)
            }
            if (shops.isEmpty()) {
                call.respondText(
                    "No shop found",
                    status = HttpStatusCode.NotFound
                )
            } else {
                call.respond(shops)
            }
        }
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val shop = transaction {
                Shop.findById(id)
            }
            if (shop != null) {
                call.respond(shop.toShop())
            } else {
                call.respondText(
                    "No shop available",
                    status = HttpStatusCode.NotFound
                )
            }
        }
        post {
            val shop = call.receive<ShopObject>()
            transaction {
                Shop.new {
                    name = shop.name
                    description = shop.description
                }
            }
            call.respondText(
                "Shop created successfully",
                status = HttpStatusCode.Created
            )
        }
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val shop = transaction {
                Shop.findById(id)
            }
            if (shop != null) {
                transaction {
                    shop.delete()
                }
                call.respondText(
                    "Shop deleted successfully",
                    status = HttpStatusCode.Accepted
                )
            } else {
                call.respondText(
                    "No shop found",
                    status = HttpStatusCode.NotFound
                )
            }
        }
        put("{id}") {
            val item = call.receive<ShopObject>()
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val shop = transaction {
                Shop.findById(id)
            }
            if (shop != null) {
                transaction {
                    shop.name = item.name
                    shop.description = item.description
                }
                call.respondText(
                    "Shop updated successfully",
                    status = HttpStatusCode.Accepted
                )
            } else {
                call.respondText(
                    "No shop found",
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }
}