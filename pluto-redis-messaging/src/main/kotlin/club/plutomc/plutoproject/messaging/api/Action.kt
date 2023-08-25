package club.plutomc.plutoproject.messaging.api

import com.google.gson.JsonObject

interface Action {

    fun onMessage(channel: Channel, content: JsonObject)

}