package club.plutomc.plutoproject.connector.api

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase

fun MongoClient.defaultDatabase(): MongoDatabase {
    return this.getDatabase(ConnectorApiProvider.connectionManager.getSettingsSection("defaults.mongo").getString("database"))
}