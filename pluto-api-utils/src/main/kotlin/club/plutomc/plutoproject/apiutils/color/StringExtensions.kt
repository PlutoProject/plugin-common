package club.plutomc.plutoproject.apiutils.color

import net.kyori.adventure.text.format.TextColor

fun String.asTextColor(): TextColor {
    return TextColor.fromHexString(this)!!
}

fun String.mmFormat(): String {
    return "<$this>"
}