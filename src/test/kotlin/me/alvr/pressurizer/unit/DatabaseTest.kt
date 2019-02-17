package me.alvr.pressurizer.unit

import io.kotlintest.Spec
import io.kotlintest.assertSoftly
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.ExpectSpec
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.database.tables.CountriesTable
import me.alvr.pressurizer.database.tables.CurrenciesTable
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.database.tables.VersionTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseTest : ExpectSpec() {
    override fun afterSpec(spec: Spec) {
        transaction {
            SchemaUtils.drop(
                UsersTable,
                GamesTable,
                UserGamesTable,
                CountriesTable,
                CurrenciesTable,
                VersionTable
            )
        }
    }

    init {
        context("a database") {
            expect("user with no country set") {
                val userId = SteamId("test_user_id_1")
                Database.insertUser(userId, null)

                val user = Database.getUserById(userId)

                assertSoftly {
                    user.size shouldBe 1
                    user.single().id shouldBe userId
                    user.single().country shouldBe null
                }
            }

            expect("user with country set") {
                val userId = SteamId("test_user_id_2")
                Database.insertUser(userId, "ES")

                val user = Database.getUserById(userId)

                assertSoftly {
                    user.size shouldBe 1
                    user.single().id shouldBe userId
                    user.single().country?.let {
                        it shouldNotBe null
                        it.code shouldBe "ES"
                    }
                }
            }

            expect("inexistant user") {
                val userId = SteamId("invalid_user_id")

                val user = Database.getUserById(userId)

                assertSoftly {
                    user.isNullOrEmpty() shouldBe true
                }
            }
        }
    }
}