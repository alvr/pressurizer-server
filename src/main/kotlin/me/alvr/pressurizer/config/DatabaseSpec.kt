package me.alvr.pressurizer.config

import com.uchuhimo.konf.ConfigSpec

/**
 * [Configuration][config] for database.
 *
 * [url] is the JDBC url of the database. It should follow this format: jdbc:postgresql://host[:port]/database_name
 * (port can be ommited if it is running in the default port: 5432)
 * [user] is the owner of the database.
 * [pass] is the password of the database. Use at least 48 characters (letters, numbers and symbols).
 * [pool] is the maximum active connections the database can has.
 */
object DatabaseSpec : ConfigSpec("db") {
    val url by required<String>("url", "URL of the database")
    val user by required<String>("user", "User of the database")
    val pass by required<String>("pass", "Password of the database")
    val pool by optional(20, "pool", "Number of maximum connections")
}