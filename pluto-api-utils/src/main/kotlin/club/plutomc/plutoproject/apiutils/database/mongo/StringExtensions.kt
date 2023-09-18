package club.plutomc.plutoproject.apiutils.database.mongo

import org.bson.Document

fun String.toDocument(): Document {
    return Document.parse(this)
}