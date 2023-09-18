package club.plutomc.plutoproject.apiutils.json

inline fun <reified T> String.toObject(): T {
    return GsonProvider.gson.fromJson(this, T::class.java)
}