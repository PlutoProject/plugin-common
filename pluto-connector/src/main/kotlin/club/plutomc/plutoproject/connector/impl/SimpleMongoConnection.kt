package club.plutomc.plutoproject.connector.impl

import club.plutomc.plutoproject.connector.api.Connection
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.UuidRepresentation

class SimpleMongoConnection(
    host: String,
    port: Int,
    database: String,
    username: String,
    password: String
) : Connection<MongoClient> {

    private val mongo: MongoClient

    init {
        val connectionString = ConnectionString("mongodb://$host:$port")

        val credentials = MongoCredential.createCredential(
            username,
            database,
            password.toCharArray()
        )

        val settings = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(connectionString)
            .credential(credentials)
            .build()

        mongo = MongoClients.create(settings)
    }

    override fun get(): MongoClient = mongo

    override fun close() {
        mongo.close()
    }

}