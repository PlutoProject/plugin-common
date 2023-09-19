package club.plutomc.plutoproject.messaging.impl.simple.message

import club.plutomc.plutoproject.apiutils.concurrent.launchWithPluto
import club.plutomc.plutoproject.messaging.api.Channel
import club.plutomc.plutoproject.messaging.api.Member
import club.plutomc.plutoproject.messaging.api.message.Message
import club.plutomc.plutoproject.messaging.api.message.RepliedMessage
import club.plutomc.plutoproject.messaging.api.message.SendResult
import club.plutomc.plutoproject.messaging.impl.simple.ImplementedChannel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class SimpleSendResult(
    private val channel: ImplementedChannel,
    private val message: Message
) : SendResult {

    private val scope = channel.getCoroutineScope()

    override fun getReplies(from: Collection<Member>): Deferred<Collection<RepliedMessage>> {
        val deferred: CompletableDeferred<Collection<RepliedMessage>> = CompletableDeferred()
        scope.launchWithPluto {
            while (true) {
                if (channel.getRepliedMessages()
                        .filter { it.getUniqueId() == message.getUniqueId() }.map { it.getSender().getUniqueId() }
                        .containsAll(from.map { it.getUniqueId() })
                ) {
                    continue
                }

                break
            }

            deferred.complete(channel.getRepliedMessages().filter { it.getUniqueId() == message.getUniqueId() })
        }

        return deferred
    }

    override fun getTarget(): Collection<Member> {
        return message.getTargets()
    }

}