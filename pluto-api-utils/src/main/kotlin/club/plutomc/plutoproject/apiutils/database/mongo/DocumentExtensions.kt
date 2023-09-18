package club.plutomc.plutoproject.apiutils.database.mongo

import club.plutomc.plutoproject.apiutils.json.toObject
import com.google.gson.JsonParser
import org.bson.Document

inline fun <reified T> Document.toObject(): T {
    val json = this.toJson()
    val jsonObject = JsonParser.parseString(json).asJsonObject
    jsonObject.remove("_id")
    return jsonObject.toObject()
}