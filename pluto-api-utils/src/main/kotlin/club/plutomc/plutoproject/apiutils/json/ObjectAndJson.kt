package club.plutomc.plutoproject.apiutils.json

import com.google.gson.Gson
import com.google.gson.JsonObject

object ObjectAndJson {

    val gson = Gson()

    fun toJsonObject(obj: Any): JsonObject {
        return gson.toJsonTree(obj).asJsonObject
    }

    fun toJsonString(obj: Any): String {
        return toJsonObject(obj).asString
    }

    inline fun <reified T> fromJsonObject(jsonObj: JsonObject): T {
        return gson.fromJson(jsonObj, T::class.java)
    }

    inline fun <reified T> fromJsonString(jsonStr: String): T {
        return gson.fromJson(jsonStr, T::class.java)
    }

}