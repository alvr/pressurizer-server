package me.alvr.pressurizer.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import me.alvr.pressurizer.config.DatabaseSpec
import me.alvr.pressurizer.config.config
import me.alvr.pressurizer.database.tables.CountriesTable
import me.alvr.pressurizer.database.tables.CurrenciesTable
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.database.tables.VersionTable
import me.alvr.pressurizer.domain.Country
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.domain.User
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import org.jetbrains.exposed.sql.Database as Exposed

/**
 * Database singleton to do CRUD operations.
 */
object Database {
    private val pool: Int = config[DatabaseSpec.pool]
    private val dispatcher: CoroutineContext = Executors.newFixedThreadPool(pool).asCoroutineDispatcher()

    private val migrations = listOf(
        "/migrations/001_currencies_and_countries.sql"
    )

    init {
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

    suspend fun insertUser(user: SteamId, countryCode: String? = null) = withContext(dispatcher) {
        transaction {
            UsersTable.insertIgnore {
                it[steamId] = user.id
                it[country] = countryCode
            }
        }
    }

    suspend fun getUserById(user: SteamId) = withContext(dispatcher) {
        transaction {
            UsersTable.select { UsersTable.steamId eq user.id }.mapNotNull {
                User(
                    id = SteamId(it[UsersTable.steamId]),
                    country = it[UsersTable.country]?.let { c -> Country(c) },
                    updatedAt = it[UsersTable.updatedAt]
                )
            }
        }
    }
}