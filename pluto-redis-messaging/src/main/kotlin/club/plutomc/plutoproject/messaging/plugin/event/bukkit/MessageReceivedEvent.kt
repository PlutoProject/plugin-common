package club.plutomc.plutoproject.messaging.plugin.event.bukkit

import com.google.gson.JsonObject
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MessageReceivedEvent(val channel: String, val message: JsonObject) : Event(true) {

    companion object {
        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }

}