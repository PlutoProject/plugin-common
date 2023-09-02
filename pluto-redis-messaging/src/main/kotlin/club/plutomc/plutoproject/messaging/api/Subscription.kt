package club.plutomc.plutoproject.messaging.api

import com.google.gson.JsonObject

interface Subscription {

    fun onMessage(channel: Channel, content: JsonObject)

}