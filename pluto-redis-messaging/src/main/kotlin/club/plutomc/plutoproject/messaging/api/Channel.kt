package club.plutomc.plutoproject.messaging.api

import com.google.gson.JsonObject
import java.io.Closeable

interface Channel : Closeable {

    val name: String

    fun isValid(): Boolean

    fun publish(content: JsonObject)

    fun subscribe(name: String, subscription: Subscription)

    fun unsubscribe(name: String)

}