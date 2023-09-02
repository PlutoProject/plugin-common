package club.plutomc.plutoproject.messaging.plugin.event.velocity

import com.google.gson.JsonObject

class SelfMessageReceivedEvent(val channel: String, val message: JsonObject) {

}