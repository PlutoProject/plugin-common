package club.plutomc.plutoproject.messaging.test

import club.plutomc.plutoproject.messaging.impl.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

fun main() = runBlocking {
    val producer = KafkaProducer<String, String>(Constants.PRODUCER_BASIC_PROPS.apply {
        this["bootstrap.servers"] = "localhost:9094"
    })


    val job = launch {
        var i = 0
        while (true) {
            producer.send(ProducerRecord("test", "test-$i", "test-$i"))
            i++
            Thread.sleep(1000L)
            println("sent-$i")
        }
    }

    val job1 = launch {
        var i = 100
        while (true) {
            producer.send(ProducerRecord("test-1", "test-1-$i", "test-1-$i"))
            i++
            Thread.sleep(1000L)
            println("sent-1-$i")
        }
    }

}

class ProducerTest {
}