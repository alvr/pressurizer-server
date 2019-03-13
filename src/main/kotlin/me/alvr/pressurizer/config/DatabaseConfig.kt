package me.alvr.pressurizer.config

/**
 * [Configuration][databaseConfig] for database.
 *
 * [url] is the JDBC url of the database. It should follow this format: jdbc:postgresql://host[:port]/database_name
 * (port can be ommited if it is running in the default port: 5432)
 * [user] is the owner of the database.
 * [pass] is the password of the database. Use at least 48 characters (letters, numbers and symbols).
 * [pool] is the maximum active connections the database can has.
 */
interface DatabaseConfig {
    fun url(): String
    fun user(): String
    fun pass(): String
    fun pool(): Int = 20
}