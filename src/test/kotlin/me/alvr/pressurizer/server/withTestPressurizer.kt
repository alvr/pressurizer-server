package me.alvr.pressurizer.server

import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import me.alvr.pressurizer.pressurizer

fun <T> withTestPressurizer(test: TestApplicationEngine.() -> T) = withTestApplication {
    start()
    application.pressurizer()
    test()
}