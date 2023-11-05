package database

import com.mongodb.client.model.Indexes
import org.litote.kmongo.coroutine.CoroutineClient
import config.DatabaseConfig
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.reactivestreams.KMongo
import org.bson.types.ObjectId
import org.litote.kmongo.*

object DatabaseFactory {
    private lateinit var client: CoroutineClient
    lateinit var database: CoroutineDatabase

    suspend fun init(config: DatabaseConfig) {
        // Construct the MongoDB URI including the username and password
        val mongoURI = "mongodb://${config.username}:${config.password}@${config.host}:${config.port}/${config.databaseName}"

        // Create a client with the specified URI
        client = KMongo.createClient(mongoURI).coroutine

        // Access the database
        database = client.getDatabase(config.databaseName)

        val articleCollection = database.getCollection<ArticleJson>("articlejson")
        articleCollection.createIndex(Indexes.ascending("sourceId"))
    }

    fun destroy() {
        client.close()
    }
}

data class ArticleJson(
    val _id: ObjectId = ObjectId.get(),
    val sourceId: String,
    val jsonData: String
)