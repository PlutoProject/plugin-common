package club.plutomc.plutoproject.messaging.test

import club.plutomc.plutoproject.messaging.impl.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

fun main() = runBlocking {
    /*val producer = KafkaProducer<String, String>(Constants.PRODUCER_BASIC_PROPS.apply {
    this["bootstrap.servers"] = "localhost:9094"
})

producer.send(ProducerRecord("test", "test", "test"))*/

    val consumer = KafkaConsumer<String, String>(Constants.CONSUMER_BASIC_PROPS.apply {
        this["bootstrap.servers"] = "localhost:9094"
        this["group.id"] = "test"
    })

    var enabled = false

    consumer.subscribe(listOf("test"))

    val job1 = launch {
        while (true) {
            val records = consumer.poll(Duration.ofMillis(100))

            records.forEach {
                println(it.value())
            }
        }
    }

    val job3 = launch {
        while (true) {
            val readIn = checkNotNull(readlnOrNull())

            if (!enabled) {
                consumer.subscribe(listOf("test", "test-1"))
            } else {
                consumer.subscribe(listOf("test"))
            }
        }
    }
}

class ConsumerTest {
}