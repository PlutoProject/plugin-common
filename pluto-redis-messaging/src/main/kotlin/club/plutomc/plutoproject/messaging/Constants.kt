package club.plutomc.plutoproject.messaging

import club.plutomc.plutoproject.apiutils.chat.append
import club.plutomc.plutoproject.apiutils.chat.asComponent
import club.plutomc.plutoproject.apiutils.color.*

object Constants {

    val MESSAGE_PREFIX =
        "${MACCHIATO_SURFACE_0.mmFormat()}[${MACCHIATO_GREEN.mmFormat()}消息通道${MACCHIATO_SURFACE_0.mmFormat()}]".asComponent()

    val MESSAGE_COMMAND = MESSAGE_PREFIX.append(
        " ${MACCHIATO_YELLOW.mmFormat()}Pluto 消息通道，${MACCHIATO_TEXT.mmFormat()}版本 ${MACCHIATO_YELLOW.mmFormat()}<version>${MACCHIATO_TEXT.mmFormat()}。"
    )

    val MESSAGE_CHANNEL_NOT_EXIST = MESSAGE_PREFIX.append(
        " ${MACCHIATO_TEXT.mmFormat()}频道 ${MACCHIATO_YELLOW.mmFormat()}<channel> ${MACCHIATO_TEXT.mmFormat()}不存在。"
    )

    val MESSAGE_SEND_MESSAGE = MESSAGE_PREFIX.append(
        " ${MACCHIATO_TEXT.mmFormat()}向频道 ${MACCHIATO_YELLOW.mmFormat()}<channel> ${MACCHIATO_TEXT.mmFormat()}发送消息：${MACCHIATO_TEXT.mmFormat()}<content>${MACCHIATO_TEXT.mmFormat()}。"
    )

    val MESSAGE_CHANNEL_START_MONITOR = MESSAGE_PREFIX.append(
        " ${MACCHIATO_TEXT.mmFormat()}开始监听频道 ${MACCHIATO_YELLOW.mmFormat()}<channel>${MACCHIATO_TEXT.mmFormat()}。"
    )

    val MESSAGE_CHANNEL_STOP_MONITOR = MESSAGE_PREFIX.append(
        " ${MACCHIATO_TEXT.mmFormat()}已结束监听频道 ${MACCHIATO_YELLOW.mmFormat()}<channel>${MACCHIATO_TEXT.mmFormat()}。"
    )

    val MESSAGE_CHANNEL_MONITOR_RECEIVED = MESSAGE_PREFIX.append(
        " ${MACCHIATO_TEXT.mmFormat()}从频道 ${MACCHIATO_YELLOW.mmFormat()}<channel> ${MACCHIATO_TEXT.mmFormat()}接收到消息：${MACCHIATO_YELLOW.mmFormat()}<content>${MACCHIATO_TEXT.mmFormat()}。"
    )

}