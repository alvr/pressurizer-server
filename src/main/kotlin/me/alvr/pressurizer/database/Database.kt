package me.alvr.pressurizer.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import me.alvr.pressurizer.config.databaseConfig
import me.alvr.pressurizer.database.tables.CountriesTable
import me.alvr.pressurizer.database.tables.CurrenciesTable
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.database.tables.VersionTable
import me.alvr.pressurizer.database.tables.WishlistTable
import me.alvr.pressurizer.domain.Country
import me.alvr.pressurizer.domain.Game
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.domain.mappers.CountryMapper
import me.alvr.pressurizer.domain.mappers.CurrencyMapper
import me.alvr.pressurizer.domain.mappers.GameMapper
import me.alvr.pressurizer.domain.mappers.UserGameMapper
import me.alvr.pressurizer.domain.mappers.UserMapper
import me.alvr.pressurizer.routes.wishlist.WishlistStatus
import me.alvr.pressurizer.utils.average
import me.alvr.pressurizer.utils.sum
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.math.round
import org.jetbrains.exposed.sql.Database as Exposed

/**
 * Database singleton to do CRUD operations.
 */
object Database {
    private val pool: Int = databaseConfig.pool()
    private val dispatcher: CoroutineContext = Executors.newFixedThreadPool(pool).asCoroutineDispatcher()

    private val migrations = listOf(
        "/migrations/001_currencies_and_countries.sql"
    )

    init {
        val cfg = HikariConfig()
        cfg.jdbcUrl = databaseConfig.url()
        cfg.username = databaseConfig.user()
        cfg.password = databaseConfig.pass()
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
                WishlistTable,
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

    //region [User Queries]
    suspend fun insertUser(user: SteamId, countryCode: String? = "US") = withContext(dispatcher) {
        transaction {
            UsersTable.insertIgnore { u ->
                u[steamId] = user.id
                countryCode?.let { u[country] = it.toUpperCase() }
            }
        }
    }

    suspend fun getUserById(user: SteamId) = withContext(dispatcher) {
        transaction {
            UsersTable.select { UsersTable.steamId eq user.id }
                .mapNotNull { UserMapper.map(it) }
                .first()
        }
    }

    suspend fun getCountry(user: SteamId) = withContext(dispatcher) {
        transaction {
            (CountriesTable innerJoin UsersTable)
                .slice(CountriesTable.code, CountriesTable.name)
                .select { (UsersTable.steamId eq user.id) and (UsersTable.country eq CountriesTable.code) }
                .map { CountryMapper.map(it) }
                .first()
        }
    }

    suspend fun updateCountry(user: SteamId, country: Country) = withContext(dispatcher) {
        transaction {
            UsersTable.update({ UsersTable.steamId eq user.id }) {
                it[UsersTable.country] = country.code.toUpperCase()
            }
        }
    }

    suspend fun updateUpdatedAt(user: SteamId) = withContext(dispatcher) {
        transaction {
            UsersTable.update({ UsersTable.steamId eq user.id }) {
                it[UsersTable.updatedAt] = Instant.now()
            }
        }
    }
    //endregion [User Queries]

    //region [Games Queries]
    suspend fun insertGame(gameId: String, gameName: String) = withContext(dispatcher) {
        transaction {
            GamesTable.insertIgnore {
                it[appId] = gameId
                it[title] = gameName
            }
        }
    }

    suspend fun getGamesByUser(user: SteamId) = withContext(dispatcher) {
        transaction {
            UserGamesTable.select { UserGamesTable.steamId eq user.id }
                .map { it[UserGamesTable.appId] }
        }
    }
    //endregion [Games Queries]

    //region [UserGames Queries]
    suspend fun getGamesCompleteByUser(user: SteamId) = withContext(dispatcher) {
        transaction {
            val games = (UserGamesTable innerJoin GamesTable)
                .select { UserGamesTable.steamId eq user.id }
                .orderBy(UserGamesTable.timePlayed, SortOrder.DESC)
                .mapNotNull { UserGameMapper.map(it) }

            val totalCost = games.mapNotNull { it.cost }
            val totalTime = games.mapNotNull { it.timePlayed }
            val totalCostSum = totalCost.sum()
            val totalTimeSum = totalTime.sum()

            val stats = when {
                games.isNotEmpty() -> mapOf<String, Number>(
                    "totalGames" to games.size,
                    "totalCost" to totalCostSum,
                    "totalTime" to totalTimeSum,
                    "avgCost" to totalCost.average(),
                    "avgTime" to round(totalTime.average()),
                    "avgCostTime" to totalCostSum / totalTimeSum.toBigDecimal()
                )
                else -> emptyMap()
            }

            val country = when {
                games.isNotEmpty() -> UsersTable
                    .slice(UsersTable.country)
                    .select { UsersTable.steamId eq user.id }
                    .map { it[UsersTable.country] }
                    .first()
                else -> ""
            }

            mapOf("games" to games, "stats" to stats, "country" to country)
        }
    }

    suspend fun insertUserGame(user: SteamId, gameId: String, price: BigDecimal, time: Int) = withContext(dispatcher) {
        transaction {
            UserGamesTable.insertIgnore {
                it[steamId] = user.id
                it[appId] = gameId
                it[cost] = price
                it[timePlayed] = time
            }
        }
    }

    suspend fun updateUserGame(user: SteamId, game: Game) = withContext(dispatcher) {
        transaction {
            UserGamesTable.update({ (UserGamesTable.steamId eq user.id) and (UserGamesTable.appId eq game.appId) }) { new ->
                game.cost?.let {
                    when {
                        it < BigDecimal.ZERO -> new[cost] = BigDecimal.ZERO
                        it > 999999999.toBigDecimal() -> new[cost] = 999999999.toBigDecimal()
                        else -> new[cost] = it
                    }
                }
                game.timePlayed?.let { new[timePlayed] = it }
                game.finished?.let { new[finished] = it }
            }
        }
    }
    //endregion [UserGames Queries]

    //region [Currency Queries]
    suspend fun getCurrencyInfo(countryCode: String) = withContext(dispatcher) {
        transaction {
            (CurrenciesTable innerJoin CountriesTable)
                .slice(CurrenciesTable.code, CurrenciesTable.symbol, CurrenciesTable.thousand, CurrenciesTable.decimal)
                .select { (CountriesTable.code eq countryCode) and
                    (CurrenciesTable.code eq CountriesTable.currency)
                }
                .map { CurrencyMapper.map(it) }
                .first()
        }
    }
    //endregion [Currency Queries]

    //region [Country Queries]
    suspend fun getCountries() = withContext(dispatcher) {
        transaction {
            CountriesTable
                .slice(CountriesTable.code, CountriesTable.name)
                .selectAll()
                .orderBy(CountriesTable.name)
                .map { CountryMapper.map(it) }
        }
    }
    //endregion [Country Queries]

    //region [Wishlist Queries]
    suspend fun getWishlist(user: SteamId) = withContext(dispatcher) {
        transaction {
            WishlistTable
                .slice(WishlistTable.appId)
                .select { WishlistTable.steamId eq user.id }
                .map { it[WishlistTable.appId] }
        }
    }

    suspend fun updateWishlist(user: SteamId, games: Map<WishlistStatus, List<String>>) = withContext(dispatcher) {
        games[WishlistStatus.NEW]?.forEach { app ->
            transaction {
                WishlistTable
                    .insertIgnore {
                        it[WishlistTable.steamId] = user.id
                        it[WishlistTable.appId] = app
                    }
            }
        }

        games[WishlistStatus.REMOVE]?.forEach {
            transaction {
                WishlistTable
                    .deleteWhere {
                        (WishlistTable.steamId eq user.id) and
                                (WishlistTable.appId eq it)
                    }
            }
        }
    }
    //endregion [Wishlist Queries]
}