package me.alvr.pressurizer.config

import com.jdiazcano.cfg4k.loaders.EnvironmentConfigLoader
import com.jdiazcano.cfg4k.loaders.SystemPropertyConfigLoader
import com.jdiazcano.cfg4k.providers.OverrideConfigProvider
import com.jdiazcano.cfg4k.providers.ProxyConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import com.jdiazcano.cfg4k.providers.cache

private val environmentConfigProvider = ProxyConfigProvider(EnvironmentConfigLoader()).cache()
private val systemPropertyProvider = ProxyConfigProvider(SystemPropertyConfigLoader()).cache()

private val overrideConfigProvider = OverrideConfigProvider(
    systemPropertyProvider,
    environmentConfigProvider
)

val serverConfig = overrideConfigProvider.bind<ServerConfig>()
val databaseConfig = overrideConfigProvider.bind<DatabaseConfig>("db")
val apiKeyConfig = overrideConfigProvider.bind<ApiKeyConfig>("apikey")