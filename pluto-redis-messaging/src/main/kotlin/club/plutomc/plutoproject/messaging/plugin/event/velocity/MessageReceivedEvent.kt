package club.plutomc.plutoproject.messaging.plugin.event.velocity

import com.google.gson.JsonObject

class MessageReceivedEvent(val channel: String, val message: JsonObject) {
}