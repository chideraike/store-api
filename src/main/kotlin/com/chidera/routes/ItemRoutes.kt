import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.registerItemRoutes() {
    routing {
        listItemRoute()
        addItemRoute()
        deleteItemRoute()
        updateItemRoute()
    }
}

fun Route.listItemRoute() {
    route("/shop") {
        get("{id}/items") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val items = transaction {
                Item.find { Items.shopId eq id }.iterator().asSequence().toList().map(Item::toItem)
            }
            if (items.isNotEmpty()) {
                call.respond(items)
            } else {
                call.respondText(
                    "No Items available",
                    status = HttpStatusCode.NotFound
                )
            }
        }
        get("{id}/items/{itemId}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val itemId = call.parameters["itemId"]?.toIntOrNull() ?: return@get call.respondText(
                "Missing or malformed item id",
                status = HttpStatusCode.BadRequest
            )
            val shopItems = transaction {
                Item.find { Items.shopId eq id }.iterator().asSequence().toList()
            }
            val item = shopItems.find { it.id.value == itemId } ?: return@get call.respondText(
                "No item with id $itemId found",
                status = HttpStatusCode.NotFound
            )
            call.respond(
                transaction {
                    item.toItem()
                }
            )
        }
    }
}

fun Route.addItemRoute() {
    route("/shop") {
        post("{id}") {
            val item = call.receive<ItemObject>()
            val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            if ( transaction { Shop.findById(id) == null } ){
                call.respondText(
                    "No shop with id $id",
                    status = HttpStatusCode.BadRequest
                )
            }
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
                call.respondText(
                    "Item added successfully",
                    status = HttpStatusCode.Created
                )
            } else {
                call.respondText(
                    "Item name cannot be empty",
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }
}

fun Route.deleteItemRoute() {
    route("/shop") {
        delete("{id}/items/{itemId}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val itemId = call.parameters["itemId"]?.toIntOrNull() ?: return@delete call.respondText(
                "Missing or malformed item id",
                status = HttpStatusCode.BadRequest
            )
            if (id != null){
                if ( transaction { Shop.findById(id) == null } ){
                    call.respondText(
                        "No shop with id $id",
                        status = HttpStatusCode.BadRequest
                    )
                }
                if ( transaction { Item.findById(itemId) == null } ) {
                    call.respondText(
                        "No item with id $itemId",
                        status = HttpStatusCode.BadRequest
                    )
                }
                transaction {
                    Item[itemId].delete()
                }
                call.respondText(
                    "Item deleted successfully",
                    status = HttpStatusCode.Accepted
                )
            } else {
                call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }
}

fun Route.updateItemRoute() {
    route("/shop") {
        put("{id}/items/{itemId}") {
            val item = call.receive<ItemObject>()
            val id = call.parameters["id"]?.toIntOrNull()
            val itemId = call.parameters["itemId"]?.toIntOrNull() ?: return@put call.respondText(
                "Missing or malformed item id",
                status = HttpStatusCode.BadRequest
            )
            if (id != null){
                if ( transaction { Shop.findById(id) == null } ){
                    call.respondText(
                        "No shop with id $id",
                        status = HttpStatusCode.BadRequest
                    )
                }
                if ( transaction { Item.findById(itemId) == null } ) {
                    call.respondText(
                        "No item with id $itemId",
                        status = HttpStatusCode.BadRequest
                    )
                }
                transaction {
                    val itemToChange = Item[itemId]
                    itemToChange.name = item.name
                    itemToChange.description = item.description
                    itemToChange.quantityInStock = item.quantityInStock
                    itemToChange.price = item.price
                }
                call.respondText(
                    "Item updated successfully",
                    status = HttpStatusCode.Accepted
                )
            } else {
                call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }
}