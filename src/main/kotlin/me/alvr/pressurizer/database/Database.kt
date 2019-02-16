package me.alvr.pressurizer.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.asCoroutineDispatcher
import me.alvr.pressurizer.config.DatabaseSpec
import me.alvr.pressurizer.config.config
import me.alvr.pressurizer.database.tables.CountriesTable
import me.alvr.pressurizer.database.tables.CurrenciesTable
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.database.tables.VersionTable
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import org.jetbrains.exposed.sql.Database as Exposed

/**
 *
 */
object Database {
    private val dispatcher: CoroutineContext

    private val migrations = listOf(
        "/migrations/001_currencies_and_countries.sql"
    )

    init {
        val pool = config[DatabaseSpec.pool]
        dispatcher = Executors.newFixedThreadPool(pool).asCoroutineDispatcher()

        val cfg = HikariConfig()
        cfg.jdbcUrl = config[DatabaseSpec.url]
        cfg.username = config[DatabaseSpec.user]
        cfg.password = config[DatabaseSpec.pass]
        cfg.maximumPoolSize = pool
        cfg.validate()

        val connection = HikariDataSource(cfg)
        Exposed.connect(connection)

        transaction {
            createMissingTablesAndColumns(
                UsersTable,
                GamesTable,
                UserGamesTable,
                CountriesTable,
                CurrenciesTable,
                VersionTable
            )

            migrateDatabase()
        }
    }

    private fun migrateDatabase() = transaction {
        val currentVersionRow = VersionTable.selectAll()
        val isInitialized = VersionTable.selectAll().count() < 1

        if (isInitialized) {
            VersionTable.insertIgnore {
                it[current] = 0
            }
        }

        val currentVersion = currentVersionRow.first()[VersionTable.current]

        for ((index, migration) in migrations.withIndex()) {
            if (index + 1 > currentVersion) {
                val statement = this.javaClass.getResource(migration).readText()
                exec(statement)
            }
        }
    }
}