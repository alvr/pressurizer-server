package me.alvr.pressurizer.config

import com.uchuhimo.konf.ConfigSpec

/**
 * [Configuration][config] for server.
 *
 * [host] is the local address where the application is running, usually *localhost* or *0.0.0.0*
 * [port] is the port where the application is running. By default 5930 but can be changed
 * [salt] will be used to encrypt auth tokens. Use at least 48 characters (letters, numbers and symbols)
 *
 */
object ServerSpec : ConfigSpec() {
    val host by optional("0.0.0.0", "host", "Local address where the application is running")
    val port by optional(5930, "port", "Port where the application is running")
    val salt by required<String>("salt", "String to encrypt auth token")
}