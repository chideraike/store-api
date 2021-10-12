import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.registerItemRoutes() {
    routing {
        route("/v1/shops"){
            listItemRoute()
            addItemRoute()
            deleteItemRoute()
            updateItemRoute()
        }
    }
}

fun Route.listItemRoute() {
    get("{id}/items") {
        val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
            ApiResponseWithoutData(
                HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                "Missing or malformed id"
            )
        )
        if ( transaction { Shop.findById(id) == null } ){
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.NotFound.toString().split(" ").first().toInt(),
                    "No shop with id $id"
                )
            )
        }
        val items = transaction {
            Item.find { Items.shopId eq id }.iterator().asSequence().toList().map(Item::toItem)
        }
        if (items.isNotEmpty()) {
            call.respond(
                ApiResponse(
                    HttpStatusCode.OK.toString().split(" ").first().toInt(),
                    "Success",
                    items
                )
            )
        } else {
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.NotFound.toString().split(" ").first().toInt(),
                    "No Items available"
                )
            )
        }
    }
    get("{id}/items/{itemId}") {
        val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
            ApiResponseWithoutData(
                HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                "Missing or malformed id"
            )
        )
        val itemId = call.parameters["itemId"]?.toIntOrNull() ?: return@get call.respond(
            ApiResponseWithoutData(
                HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                "Missing or malformed item id"
            )
        )
        if ( transaction { Shop.findById(id) == null } ){
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.NotFound.toString().split(" ").first().toInt(),
                    "No shop with id $id"
                )
            )
        } else {
            val shopItems = transaction {
                Item.find { Items.shopId eq id }.iterator().asSequence().toList()
            }
            val item = shopItems.find { it.id.value == itemId } ?: return@get call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.NotFound.toString().split(" ").first().toInt(),
                    "No item with id $itemId found"
                )
            )
            call.respond(
                ApiResponse(
                    HttpStatusCode.OK.toString().split(" ").first().toInt(),
                    "Success",
                    transaction {
                        item.toItem()
                    }
                )
            )
        }
    }
}

fun Route.addItemRoute() {
    post("{id}") {
        try {
            val item = call.receive<ItemObject>()
            val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                    "Missing or malformed id"
                )
            )
            if (transaction { Shop.findById(id) == null }) {
                call.respond(
                    ApiResponseWithoutData(
                        HttpStatusCode.NotFound.toString().split(" ").first().toInt(),
                        "No shop with id $id"
                    )
                )
            } else {
                if (item.name.isNotBlank()) {
                    transaction {
                        Item.new {
                            shopId = Shop[id]
                            name = item.name
                            description = item.description
                            quantityInStock = item.quantityInStock
                            price = item.price
                        }
                    }
                    call.respond(
                        ApiResponseWithoutData(
                            HttpStatusCode.Created.toString().split(" ").first().toInt(),
                            "Item added successfully"
                        )
                    )
                } else {
                    call.respond(
                        ApiResponseWithoutData(
                            HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                            "Item name cannot be empty"
                        )
                    )
                }
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

fun Route.deleteItemRoute() {
    delete("{id}/items/{itemId}") {
        val id = call.parameters["id"]?.toIntOrNull()
        val itemId = call.parameters["itemId"]?.toIntOrNull() ?: return@delete call.respond(
            ApiResponseWithoutData(
                HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                "Missing or malformed item id"
            )
        )
        if (id != null){
            if ( transaction { Shop.findById(id) == null } ){
                call.respond(
                    ApiResponseWithoutData(
                        HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                        "No shop with id $id"
                    )
                )
            } else {
                if (transaction { Item.findById(itemId) == null }) {
                    call.respond(
                        ApiResponseWithoutData(
                            HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                            "No item with id $itemId"
                        )
                    )
                } else {
                    val itemCheck = transaction { Item.findById(itemId)?.toItem() }
                    if ( itemCheck?.shopId == id ) {
                        transaction {
                            Item[itemId].delete()
                        }
                        call.respond(
                            ApiResponseWithoutData(
                                HttpStatusCode.Accepted.toString().split(" ").first().toInt(),
                                "Item deleted successfully"
                            )
                        )
                    } else {
                        call.respond(
                            ApiResponseWithoutData(
                                HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                                "Item $itemId is not in Shop $id"
                            )
                        )
                    }
                }
            }
        } else {
            call.respond(
                ApiResponseWithoutData(
                    HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                    "Missing or malformed id"
                )
            )
        }
    }
}

fun Route.updateItemRoute() {
    put("{id}/items/{itemId}") {
        try {
            val item = call.receive<ItemObject>()
            val id = call.parameters["id"]?.toIntOrNull()
            val itemId = call.parameters["itemId"]?.toIntOrNull() ?: return@put call.respondText(
                "Missing or malformed item id",
                status = HttpStatusCode.BadRequest
            )
            if (id != null) {
                if (transaction { Shop.findById(id) == null }) {
                    call.respond(
                        ApiResponseWithoutData(
                            HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                            "No shop with id $id"
                        )
                    )
                } else {
                    if (transaction { Item.findById(itemId) == null }) {
                        call.respond(
                            ApiResponseWithoutData(
                                HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                                "No item with id $itemId"
                            )
                        )
                    } else {
                        transaction {
                            val itemToChange = Item[itemId]
                            itemToChange.name = item.name
                            itemToChange.description = item.description
                            itemToChange.quantityInStock = item.quantityInStock
                            itemToChange.price = item.price
                        }
                        call.respond(
                            ApiResponseWithoutData(
                                HttpStatusCode.Accepted.toString().split(" ").first().toInt(),
                                "Item updated successfully"
                            )
                        )
                    }
                }
            } else {
                call.respond(
                    ApiResponseWithoutData(
                        HttpStatusCode.BadRequest.toString().split(" ").first().toInt(),
                        "Missing or malformed id"
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