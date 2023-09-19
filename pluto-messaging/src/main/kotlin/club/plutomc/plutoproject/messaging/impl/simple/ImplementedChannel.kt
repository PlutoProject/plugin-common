package club.plutomc.plutoproject.messaging.impl.simple

import club.plutomc.plutoproject.messaging.api.message.RepliedMessage
import kotlinx.coroutines.CoroutineScope

abstract class ImplementedChannel {

    internal abstract fun getCoroutineScope(): CoroutineScope

    internal abstract fun getRepliedMessages(): Collection<RepliedMessage>

    internal abstract fun getRepliedMessagesByMe(): Collection<RepliedMessage>

}