import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.registerShopRoutes() {
    routing {
        route("/v1/shops") {
            shopRouting()
        }
    }
}

fun Route.shopRouting() {
    get {
        val shops = transaction {
            Shop.all().map(Shop::toShop)
        }
        if (shops.isEmpty()) {
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.NotFound.toString().split(" ").first().toInt(),
                    HttpStatusCode.NotFound.toString().split(" ")[1]
                )
            )
        } else {
            call.respond(
                ApiResponse(
                    HttpStatusCode.OK.toString().split(" ").first().toInt(),
                    "Success",
                    shops
                )
            )
        }
    }
    get("{id}") {
        val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
            ApiResponseWithoutData(
                HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                "Missing or malformed id"
            )
        )
        val shop = transaction {
            Shop.findById(id)
        }
        if (shop != null) {
            call.respond(
                ApiResponse(
                    HttpStatusCode.OK.toString().split(" ").first().toInt(),
                    "Success",
                    shop.toShop()
                )
            )
        } else {
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.NotFound.toString().split(" ").first().toInt(),
                    "No shop available"
                )
            )
        }
    }
    post {
        try {
            val shop = call.receive<ShopObject>()
            if (shop.name.isNotBlank()) {
                transaction {
                    Shop.new {
                        name = shop.name
                        description = shop.description
                    }
                }
                call.respond(
                    ApiResponseWithoutData(
                        HttpStatusCode.Created.toString().split(" ").first().toInt(),
                        "Shop created successfully"
                    )
                )
            } else {
                call.respond(
                    ApiResponseWithoutData(
                        HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                        "Name cannot be empty"
                    )
                )
            }
        } catch (e: Exception) {
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                    "Invalid type specified"
                )
            )
        }
    }
    delete("{id}") {
        val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(
            ApiResponseWithoutData(
                HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                "Missing or malformed id"
            )
        )
        val shop = transaction {
            Shop.findById(id)
        }
        val itemsInShop = transaction { Item.find { Items.shopId eq id } }
        if (shop != null) {
            transaction {
                shop.delete()
                itemsInShop.map {
                    it.delete()
                }
            }
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.Accepted.toString().split(" ").first().toInt(),
                    "Shop deleted successfully"
                )
            )
        } else {
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.NotFound.toString().split(" ").first().toInt(),
                    "No shop found"
                )
            )
        }
    }
    put("{id}") {
        try {
            val item = call.receive<ShopObject>()
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                    "Missing or malformed id"
                )
            )
            val shop = transaction {
                Shop.findById(id)
            }
            if (shop != null) {
                if (item.name.isNotBlank()) {
                    transaction {
                        shop.name = item.name
                        shop.description = item.description
                    }
                    call.respond(
                        ApiResponseWithoutData(
                            HttpStatusCode.Accepted.toString().split(" ").first().toInt(),
                            "Shop updated successfully"
                        )
                    )
                } else {
                    call.respond(
                        ApiResponseWithoutData(
                            HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                            "Name cannot be empty"
                        )
                    )
                }
            } else {
                call.respond(
                    ApiResponseWithoutData(
                        HttpStatusCode.NotFound.toString().split(" ").first().toInt(),
                        "No shop found"
                    )
                )
            }
        } catch (e: Exception) {
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                    "Invalid type specified"
                )
            )
        }
    }
}