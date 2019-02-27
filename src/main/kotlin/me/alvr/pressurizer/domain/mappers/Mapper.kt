package me.alvr.pressurizer.domain.mappers


abstract class Mapper<in I, out O> {
    fun map(model: I): O = transform(model)

    protected abstract fun transform(model: I): O
}