package club.plutomc.plutoproject.messaging.api.operation

import club.plutomc.plutoproject.messaging.api.Contents
import club.plutomc.plutoproject.messaging.api.message.ReceivedMessage

interface ReceivedOperation : ReceivedMessage {

    fun returnResults(contents: Contents)

}