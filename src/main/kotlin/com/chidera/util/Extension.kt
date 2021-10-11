import kotlin.reflect.full.declaredMemberProperties

fun Any.hasNullProperty(vararg except: String): Boolean {
    if (this::class.declaredMemberProperties.filter {
        !except.contains(it.name)
        }.any {
            it.getter.call(this) == null
        }) return true
    return false
}

fun Any.hasNoNullProperty(vararg except: String): Boolean = !this.hasNullProperty(*except)