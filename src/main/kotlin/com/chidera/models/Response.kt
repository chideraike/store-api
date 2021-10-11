import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<R> (
    var status: Int,
    var message: String,
    var data: R? = null
)

@Serializable
data class ApiResponseWithoutData (
    var status: Int,
    var message: String,
)