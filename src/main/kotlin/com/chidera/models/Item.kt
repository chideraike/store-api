import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Items: IntIdTable() {
    val shopId = reference("shopId", Shops)
    val name = varchar("name", 255)
    val description = varchar("description", 255)
    val quantityInStock = integer("quantityInStock")
    val price = double("price")
}

class Item(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Item>(Items)

    var shopId by Shop referencedOn Items.shopId
    var name by Items.name
    var description by Items.description
    var quantityInStock by Items.quantityInStock
    var price by Items.price

    fun toItem() = ItemObject(shopId.id.value, name, description, quantityInStock, price)
}

@Serializable
data class ItemObject(
    val shopId: Int? = null,
    val name: String,
    val description: String,
    val quantityInStock: Int,
    val price: Double
)