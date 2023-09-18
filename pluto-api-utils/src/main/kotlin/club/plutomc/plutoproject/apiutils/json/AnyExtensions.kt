package club.plutomc.plutoproject.apiutils.json

import com.google.gson.JsonObject

fun Any.toJsonObject(): JsonObject {
    return GsonProvider.gson.toJsonTree(this).asJsonObject
}

fun Any.toJsonString(): String {
    return GsonProvider.gson.toJson(this)
}