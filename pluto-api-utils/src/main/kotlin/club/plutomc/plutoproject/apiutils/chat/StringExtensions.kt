package club.plutomc.plutoproject.apiutils.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

fun String.asComponent(vararg tagResolvers: TagResolver): Component {
    return MiniMessage.miniMessage().deserialize(this, *tagResolvers)
}