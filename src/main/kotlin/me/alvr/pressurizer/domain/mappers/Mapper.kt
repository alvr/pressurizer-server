package me.alvr.pressurizer.domain.mappers


abstract class Mapper<in I, out O> {
    fun map(model: I): O = transform(model)

    fun map(model: List<I>): List<O> = model.map { map(it) }

    protected abstract fun transform(model: I): O
}