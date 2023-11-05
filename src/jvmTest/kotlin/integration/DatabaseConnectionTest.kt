package integration

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo

// Assuming you have an Article class
data class Article(val sourceId: String, val content: String)

// Test class
class DatabaseTests {
    lateinit var database: CoroutineDatabase
    lateinit var articlesCollection: CoroutineCollection<Article>

    @BeforeEach
    fun setup() {

        val config = HoconApplicationConfig(ConfigFactory.load("application.properties"))

        // Retrieve properties using config.property function
        val user = config.property("db.username").getString()
        val password = config.property("db.password").getString()
        val dbHost = config.property("db.host").getString()
        val dbPort = config.property("db.port").getString()


        // Initialize the database and collection with a test database
        val client = KMongo.createClient("mongodb://$user:$password@$dbHost:$dbPort").coroutine
        database = client.getDatabase("testDatabase")
        articlesCollection = database.getCollection<Article>()

        // Clean the test collection before each test
        runBlocking { articlesCollection.deleteMany() }
    }

    @AfterEach
    fun tearDown() {
        // Cleanup after tests
        runBlocking { articlesCollection.deleteMany() }
    }

    @Test
    fun `writing to the database works`() = runBlocking {
        // Arrange
        val article = Article("source123", "Some content")

        // Act
        articlesCollection.insertOne(article)

        // Assert
        val retrievedArticle = articlesCollection.findOne(Article::sourceId eq "source123")
        assert(retrievedArticle != null)
        assert(retrievedArticle?.content == "Some content")
    }
}
