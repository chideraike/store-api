import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: String,
    val name: String,
    val description: String,
    val quantity_in_stock: Int,
    val price: Double
)