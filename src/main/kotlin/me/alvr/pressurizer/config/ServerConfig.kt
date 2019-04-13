package me.alvr.pressurizer.config

import java.net.URI

/**
 * [Configuration][serverConfig] for server.
 *
 * [host] is the local address where the application is running, usually *localhost* or *0.0.0.0*.
 * [port] is the local port where the application is running. By default 5930 but can be changed.
 * [salt] will be used to encrypt auth tokens. Use at least 48 characters (letters, numbers and symbols).
 * [client] is the URL of the client with http(s)://host(:port) regex.
 * [publicHost] is the public host where this server is running. Don't include scheme: pressurizer-api-server.com
 * [publicPort] is the public port where this server is running. By default is null if when someone visits [publicHost] no port is required.
 */
interface ServerConfig {
    fun host(): String = "0.0.0.0"
    fun port(): Int = 5930
    fun salt(): String
    fun client(): URI
    fun publicHost(): String
    fun publicPort(): Int? = null
}