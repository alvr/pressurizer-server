package me.alvr.pressurizer.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.DateColumnType
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import java.time.Instant

fun Table.instant(name: String): Column<Instant> = registerColumn(name, InstantColumnType(true))

private fun DateTime.toJavaInstant() = Instant.ofEpochMilli(this.millis)
private fun Instant.toDateTime() = DateTime(this.toEpochMilli())

internal class InstantColumnType(time: Boolean) : ColumnType() {
    private val delegate = DateColumnType(time)

    override fun sqlType(): String = delegate.sqlType()

    override fun nonNullValueToString(value: Any): String = when (value) {
        is Instant -> delegate.nonNullValueToString(value.toDateTime())
        else -> delegate.nonNullValueToString(value)
    }

    override fun valueFromDB(value: Any): Any {
        val fromDb = when (value) {
            is Instant -> delegate.valueFromDB(value.toDateTime())
            else -> delegate.valueFromDB(value)
        }
        return when (fromDb) {
            is DateTime -> fromDb.toJavaInstant()
            else -> error("Failed to convert value to Instant")
        }
    }

    override fun notNullValueToDB(value: Any): Any = when (value) {
        is Instant -> delegate.notNullValueToDB(value.toDateTime())
        else -> delegate.notNullValueToDB(value)
    }
}