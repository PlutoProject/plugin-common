package club.plutomc.plutoproject.apiutils.chat

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

fun Component.sendTo(target: CommandSender) {
    target.sendMessage(this)
}