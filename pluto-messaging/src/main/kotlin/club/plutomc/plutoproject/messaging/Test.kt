package club.plutomc.plutoproject.messaging

import club.plutomc.plutoproject.messaging.api.Contents
import club.plutomc.plutoproject.messaging.impl.Constants
import club.plutomc.plutoproject.messaging.impl.kafka.KafkaMessenger

fun main() {
    val messenger = KafkaMessenger("testMessenger", Constants.PRODUCER_BASIC_PROPS.apply {
        this["bootstrap.servers"] = "localhost:9094"
    }, Constants.CONSUMER_BASIC_PROPS.apply {
        this["bootstrap.servers"] = "localhost:9094"
    })
    val channel = messenger.get("testChannel")

    var age = 17

    while (true) {
        channel.send(Contents.of("testKey" to Person("John", age)))
        age++
        Thread.sleep(1000)
    }
}

class Test {
}