package club.plutomc.plutoproject.apiutils.chat

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component

fun Component.sendTo(target: CommandSource) {
    target.sendMessage(this)
}