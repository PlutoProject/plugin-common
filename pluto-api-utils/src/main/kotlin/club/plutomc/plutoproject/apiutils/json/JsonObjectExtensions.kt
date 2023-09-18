package club.plutomc.plutoproject.apiutils.json

import com.google.gson.JsonObject

inline fun <reified T> JsonObject.toObject(): T {
    return GsonProvider.gson.fromJson(this, T::class.java)
}