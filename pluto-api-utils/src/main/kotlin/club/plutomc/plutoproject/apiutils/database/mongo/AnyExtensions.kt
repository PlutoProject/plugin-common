package club.plutomc.plutoproject.apiutils.database.mongo

import club.plutomc.plutoproject.apiutils.json.GsonProvider
import org.bson.Document

fun Any.toDocument(): Document {
    return GsonProvider.gson.toJson(this).toDocument()
}