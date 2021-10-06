import kotlinx.serialization.Serializable

val shopStorage = mutableListOf<Shop>()

@Serializable
data class Shop(
    val id: String,
    val name: String,
    val description: String,
    val items: MutableList<Item>
)