package me.alvr.pressurizer.config

import com.uchuhimo.konf.Config

/**
 * This variable contains all the configuration. First reads from the *resource* file **pressurizer.conf** all the
 * values. Then from the **pressurizer.conf** file located in the same path the application is running. If a value
 * exists in both file, the second overrides the first. Next from **environment variables** and last from
 * **system properties**.
 *
 * pressurizer.conf files are written in HOCON
 *
 * All environment variables should be in uppercase and if is in a group, separated by an underscore. Example:
 *
 * `HASH`: not in a group
 * `DATABASE_URL`: `URL` is in the `DATABASE` group
 *
 * System properties declaration is similar, but all letters should be in lowercase and separated by a dot. Example
 *
 * `salt`: not in a group
 * `database.url`: `url` is in the `database` group
 *
 * @see ServerSpec
 */
val config = Config {
    addSpec(ServerSpec)
    addSpec(DatabaseSpec)
}
    .from.hocon.resource("pressurizer.conf")
    .from.hocon.file("pressurizer.conf")
    .from.env()
    .from.systemProperties()