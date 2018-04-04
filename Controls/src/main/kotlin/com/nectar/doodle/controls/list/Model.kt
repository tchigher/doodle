package com.nectar.doodle.controls.list

import com.nectar.doodle.utils.ListObserver
import com.nectar.doodle.utils.Pool

/**
 * Created by Nicholas Eddy on 3/19/18.
 */

interface Model<T>: Iterable<T> {
    val size: Int

    operator fun get(index: Int): T

    fun isEmpty (                       ) = size == 0
    fun section (range: ClosedRange<Int>): kotlin.collections.List<T>
    fun contains(value: T               ): Boolean
}

interface MutableModel<T>: Model<T> {
    fun editable(index: Int): Boolean

    val changed: Pool<ListObserver<MutableModel<T>, T>>
}

open class ListModel<T>(private val list: kotlin.collections.List<T>): Model<T> {

    override val size = list.size

    override fun get     (index: Int             ) = list.get     (index                              )
    override fun section (range: ClosedRange<Int>) = list.subList (range.start, range.endInclusive + 1)
    override fun contains(value: T               ) = list.contains(value                              )
    override fun iterator(                       ) = list.iterator(                                   )
}