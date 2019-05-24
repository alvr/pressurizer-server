package me.alvr.pressurizer.unit

import io.kotlintest.Spec
import io.kotlintest.assertSoftly
import io.kotlintest.inspectors.forAll
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.collections.shouldNotContainAll
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ExpectSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.database.tables.CountriesTable
import me.alvr.pressurizer.database.tables.CurrenciesTable
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UserWishlistTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.database.tables.VersionTable
import me.alvr.pressurizer.domain.Country
import me.alvr.pressurizer.domain.Game
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.domain.mappers.GameMapper
import me.alvr.pressurizer.routes.wishlist.WishlistStatus
import me.alvr.pressurizer.utils.round
import me.alvr.pressurizer.utils.shopsName
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

class DatabaseTest : ExpectSpec() {
    override fun afterSpec(spec: Spec) {
        transaction {
            listOf(
                UsersTable.tableName,
                GamesTable.tableName,
                UserGamesTable.tableName,
                UserWishlistTable.tableName
            ).forEach {
                exec("TRUNCATE TABLE $it CASCADE;")
            }
            closeExecutedStatements()
        }
    }

    override fun afterProject() {
        transaction {
            SchemaUtils.drop(
                CountriesTable,
                CurrenciesTable,
                GamesTable,
                UserGamesTable,
                UsersTable,
                UserWishlistTable,
                VersionTable
            )
        }
    }

    init {
        context("users table") {
            expect("user with no country set") {
                val userId = SteamId("test_user_id_1")
                Database.insertUser(userId, null)

                val user = Database.getUserById(userId)

                assertSoftly {
                    user.id shouldBe userId
                    user.country.code shouldBe "US"
                }
            }

            expect("user with country set") {
                val userId = SteamId("test_user_id_2")
                Database.insertUser(userId, "ES")

                val user = Database.getUserById(userId)

                assertSoftly {
                    user.id shouldBe userId
                    user.country.let {
                        it shouldNotBe null
                        it.code shouldBe "ES"
                    }
                }
            }

            expect("inexistant user") {
                val userId = SteamId("invalid_user_id")

                shouldThrow<NoSuchElementException> {
                    Database.getUserById(userId)
                }
            }
        }

        context("games table") {
            expect("insert games") {
                for (g in 1..1000) {
                    Database.insertGame(g.toString(), "Game $g")
                }

                allGames().size shouldBe 1000
            }

            expect("each user has 1000 games") {
                val users = listOf(
                    SteamId("test_user_id_1"),
                    SteamId("test_user_id_2")
                )

                users.forEach {
                    (1..1000).forEach { g ->
                        Database.insertUserGame(
                            it,
                            g.toString(),
                            g.toBigDecimal().multiply(0.01.toBigDecimal()),
                            g
                        )
                    }
                }
            }

            expect("check values for user game").config(5) {
                val users = listOf(
                    SteamId("test_user_id_1"),
                    SteamId("test_user_id_2")
                )

                val allGames = Database.getGamesCompleteByUser(users.shuffled().first())

                val games = (allGames["games"] as List<*>).filterIsInstance<Game>()
                val stats = allGames["stats"] as Map<*, *>

                val selectedGames = games.shuffled().take(10)

                assertSoftly {
                    games.size shouldBe 1000

                    selectedGames.size shouldBe 10

                    selectedGames.forAll {
                        it.title shouldBe "Game ${it.appId}"
                        it.cost shouldBe it.appId.toBigDecimal() * 0.01.toBigDecimal()
                        it.timePlayed shouldBe it.appId.toInt()
                        it.finished?.shouldBeFalse()
                    }

                    val totalCost = 0.01.toBigDecimal() * 1000.toBigDecimal() * 1001.toBigDecimal() / 2.toBigDecimal()

                    stats["totalCost"] shouldBe totalCost
                    stats["totalTime"] shouldBe (1000 * 1001) / 2
                    stats["avgCost"] shouldBe (totalCost / 1000.toBigDecimal()).round()
                    stats["avgTime"] shouldBe ((1000 * 3001) / (2 * 3000)).toFloat().round()
                    stats["avgCostTime"] shouldBe (((0.01 * 1000 * 3001) / (2 * 1000)) / ((1000 * 3001) / (2 * 1000))).toBigDecimal().round()
                }
            }

            expect("update games") {
                val user = listOf(
                    SteamId("test_user_id_1"),
                    SteamId("test_user_id_2")
                ).shuffled().first()

                var allGames = Database.getGamesCompleteByUser(user)
                var games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                val selectedGames = games.shuffled().take(4)

                val updateAll = selectedGames[0].copy(
                    cost = 55.55.toBigDecimal(),
                    timePlayed = 5555,
                    finished = true
                )

                val updateCost = selectedGames[1].copy(
                    cost = 123.45.toBigDecimal()
                )

                val updateTimePlayed = selectedGames[2].copy(
                    timePlayed = 1234
                )

                val updateFinished = selectedGames[3].copy(
                    finished = true
                )

                Database.updateUserGame(user, updateAll)
                Database.updateUserGame(user, updateCost)
                Database.updateUserGame(user, updateTimePlayed)
                Database.updateUserGame(user, updateFinished)

                allGames = Database.getGamesCompleteByUser(user)
                games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                val newUpdateAll = games.find { updateAll.appId == it.appId }
                val newUpdateCost = games.find { updateCost.appId == it.appId }
                val newUpdateTimePlayed = games.find { updateTimePlayed.appId == it.appId }
                val newUpdateFinished = games.find { updateFinished.appId == it.appId }

                assertSoftly {
                    newUpdateAll?.cost shouldBe 55.55.toBigDecimal()
                    newUpdateAll?.timePlayed shouldBe 5555
                    newUpdateAll?.finished shouldBe true

                    newUpdateCost?.cost shouldBe 123.45.toBigDecimal()

                    newUpdateTimePlayed?.timePlayed shouldBe 1234

                    newUpdateFinished?.finished shouldBe true
                }

            }

            expect("cost out of bounds") {
                val user = listOf(
                    SteamId("test_user_id_1"),
                    SteamId("test_user_id_2")
                ).shuffled().first()

                var allGames = Database.getGamesCompleteByUser(user)
                var games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                val selectedGames = games.shuffled().take(2)

                val zero = selectedGames[0].copy(
                    cost = (-100).toBigDecimal()
                )

                val nine = selectedGames[1].copy(
                    cost = 1_000_000_000.toBigDecimal()
                )

                Database.updateUserGame(user, zero)
                Database.updateUserGame(user, nine)

                allGames = Database.getGamesCompleteByUser(user)
                games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                val newZero = games.find { zero.appId == it.appId }
                val newNine = games.find { nine.appId == it.appId }

                assertSoftly {
                    newZero?.cost shouldBe 0.toBigDecimal().round()
                    newNine?.cost shouldBe 999_999_999.toBigDecimal().round()
                }
            }

            expect("game name is valid") {
                val ids = List(100) { Random.nextInt(1, 1000) }

                ids.forEach {
                    assertSoftly {
                        Database.getGameName(it.toString()) shouldBe "Game $it"
                    }
                }
            }

            expect("get user country") {
                val country = Database.getCountry(SteamId("test_user_id_1"))

                assertSoftly {
                    country shouldBe Country("US", "United States")
                }
            }

            expect("update user country") {
                val newCountry = Country("ES", "Spain")
                Database.updateCountry(SteamId("test_user_id_1"), newCountry)
                val country = Database.getCountry(SteamId("test_user_id_1"))

                assertSoftly {
                    country shouldBe newCountry
                }
            }
        }

        context("wishlist table") {
            val user = SteamId("user")
            Database.insertUser(user)

            expect("preferred shops") {
                val shops = Database.getShopWishlist(user)

                shops shouldBe "steam"
            }

            expect("update preferred shops") {
                Database.updateShopWishlist(user, "steam,bundlestars,voidu")
                val shops = Database.getShopWishlist(user)

                assertSoftly {
                    shops shouldContain "steam"
                    shops shouldContain "bundlestars"
                    shops shouldContain "voidu"

                    shops.split(",").size shouldBe 3
                }
            }

            expect("shops size") {
                shopsName.size shouldBe 29
            }

            expect("update user wishlist") {
                val wishlist = mutableMapOf<WishlistStatus, List<String>>()
                val ids = List(100) { Random.nextInt(1, 1000).toString() }.toSet().toList()
                val removedIds = ids.shuffled().take(30).toSet().toList()

                wishlist[WishlistStatus.NEW] = ids
                Database.updateWishlist(user, wishlist)
                var userWishlist = Database.getWishlist(user)

                assertSoftly {
                    userWishlist.size shouldBe ids.size
                    userWishlist shouldContainAll ids
                }

                wishlist.clear()

                wishlist[WishlistStatus.REMOVE] = removedIds
                Database.updateWishlist(user, wishlist)
                userWishlist = Database.getWishlist(user)

                assertSoftly {
                    userWishlist.size shouldBe (ids.size - removedIds.size)
                    userWishlist shouldNotContainAll removedIds
                }
            }
        }

        context("export data") {
            expect("export data is correct") {
                val export = Database.exportData(SteamId("test_user_id_1"))

                export shouldHaveSize 1000
            }
        }

    }

    private suspend fun allGames() = withContext(Dispatchers.Default) {
        transaction {
            GamesTable.selectAll().map { GameMapper.map(it) }
        }
    }

}