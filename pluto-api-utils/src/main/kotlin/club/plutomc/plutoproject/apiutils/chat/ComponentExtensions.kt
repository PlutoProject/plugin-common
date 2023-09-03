package club.plutomc.plutoproject.apiutils.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

fun Component.tag(vararg tagResolvers: TagResolver): Component {
    return MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(this).replace("\\<", "<"), *tagResolvers)
}

fun Component.append(miniMessage: String, vararg tagResolvers: TagResolver): Component {
    return this.append(MiniMessage.miniMessage().deserialize(miniMessage, *tagResolvers))
}