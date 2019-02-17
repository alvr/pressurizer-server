package me.alvr.pressurizer.domain.mappers

abstract class Mapper<in I, out O> {
    fun map(model: I?): O? = model?.let { transform(model) }

    fun map(model: List<I>?): ArrayList<out O>? = model?.let {
        val outList = ArrayList<O>()
        for (t1 in model) {
            map(t1)?.let { outList.add(it) }
        }

        outList
    }

    protected abstract fun transform(model: I): O
}