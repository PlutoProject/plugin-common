package club.plutomc.plutoproject.messaging.impl

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.*

internal object Constants {

    const val DEFAULT_KEY: String = "_default"
    val PLACEHOLDER_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    val PRODUCER_BASIC_PROPS = Properties().apply {
        // this["bootstrap.servers"] = "localhost:9092"
        this[ProducerConfig.LINGER_MS_CONFIG] = 1
        this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
        this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
        this[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = "true"
        this[ProducerConfig.ACKS_CONFIG] = "all"
    }
    val CONSUMER_BASIC_PROPS = Properties().apply {
        // this["bootstrap.servers"] = "localhost:9092"
        // this["group.id"] = "test"
        this[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        this[ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG] = "1000"
        this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"
        this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] =
            "org.apache.kafka.common.serialization.StringDeserializer"
    }

}