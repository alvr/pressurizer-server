package me.alvr.pressurizer.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * Database table with the current migration of the database.
 *
 * [current] current database version. Every migration is a newer version.
 */
object VersionTable : Table("version") {
    val current = integer("current_version")
}