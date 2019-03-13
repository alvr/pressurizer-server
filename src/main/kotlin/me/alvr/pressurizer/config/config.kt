package me.alvr.pressurizer.config

import com.jdiazcano.cfg4k.hocon.HoconConfigLoader
import com.jdiazcano.cfg4k.loaders.EnvironmentConfigLoader
import com.jdiazcano.cfg4k.loaders.SystemPropertyConfigLoader
import com.jdiazcano.cfg4k.providers.DefaultConfigProvider
import com.jdiazcano.cfg4k.providers.OverrideConfigProvider
import com.jdiazcano.cfg4k.providers.ProxyConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import com.jdiazcano.cfg4k.providers.cache
import com.jdiazcano.cfg4k.sources.ClasspathConfigSource
import com.jdiazcano.cfg4k.sources.FileConfigSource
import java.io.File

private val classpathConfigSource = if (File("pressurizer.conf").exists())
    FileConfigSource(File("pressurizer.conf"))
else
    ClasspathConfigSource("pressurizer.conf")

private val propertyConfigLoader = HoconConfigLoader(classpathConfigSource)

private val propertyConfigProvider = ProxyConfigProvider(propertyConfigLoader).cache()
private val environmentConfigProvider = ProxyConfigProvider(EnvironmentConfigLoader()).cache()
private val systemPropertyProvider = ProxyConfigProvider(SystemPropertyConfigLoader()).cache()

private val overrideConfigProvider = OverrideConfigProvider(
    systemPropertyProvider,
    environmentConfigProvider,
    propertyConfigProvider
)

val serverConfig = overrideConfigProvider.bind<ServerConfig>()
val databaseConfig = overrideConfigProvider.bind<DatabaseConfig>("db")
val apiKeyConfig = overrideConfigProvider.bind<ApiKeyConfig>("apikey")