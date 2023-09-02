package club.plutomc.plutoproject.messaging.api

import com.google.gson.JsonObject

@Suppress("UNUSED_PARAMETER")
class Subscription {

    fun onMessage(channel: Channel, content: JsonObject) {

    }

    fun onMessageWithSelf(channel: Channel, content: JsonObject) {

    }

}