package club.plutomc.plutoproject.messaging

import club.plutomc.plutoproject.messaging.impl.Constants
import club.plutomc.plutoproject.messaging.impl.kafka.KafkaMessenger

fun main() {
    val messenger = KafkaMessenger("testMessengerReceiver", Constants.PRODUCER_BASIC_PROPS.apply {
        this["bootstrap.servers"] = "localhost:9094"
    }, Constants.CONSUMER_BASIC_PROPS.apply {
        this["bootstrap.servers"] = "localhost:9094"
    })
    val channel = messenger.get("testChannel")
    channel.subscribe("testSub") { _, receivedMessage ->
        println("Sender ${receivedMessage.getSender()}")
        val person = receivedMessage.getContents().getWithCast<Person>("testKey")
        println("Content ${person.name} ${person.age}")
    }
}

class ReceiveTest {
}