package config

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig

class DatabaseConfig {

    private val config = HoconApplicationConfig(ConfigFactory.load("application.properties"))

    val host = config.property("db.host").getString()
    val port = config.property("db.port").getString()
    val databaseName = config.property("db.databaseName").getString()
    val username =  config.property("db.username").getString()
    val password = config.property("db.password").getString()
    val source = config.property("db.authSource").getString()

}

