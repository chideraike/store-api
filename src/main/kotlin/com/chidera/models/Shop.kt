import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Shops: IntIdTable() {
    val name = varchar("name", 255)
    val description = varchar("description", 255)
    override val primaryKey = PrimaryKey(id, name = "Shop_Id")
}

class Shop(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Shop>(Shops)

    var name by Shops.name
    var description by Shops.description

    fun toShop() = ShopObject(id.value, name, description)
}

@Serializable
data class ShopObject(
    val shopId: Int? = null,
    val name: String,
    val description: String,
)