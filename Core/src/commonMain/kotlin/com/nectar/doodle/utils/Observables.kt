package com.nectar.doodle.utils

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Nicholas Eddy on 10/21/17.
 */


typealias SetObserver     <S, T> = (source: ObservableSet <S, T>, removed: Set<T>,      added: Set<T>                                    ) -> Unit
typealias ListObserver    <S, T> = (source: ObservableList<S, T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) -> Unit
typealias ChangeObserver  <S>    = (source: S                                                                                            ) -> Unit
typealias PropertyObserver<S, T> = (source: S, old: T, new: T                                                                            ) -> Unit

interface Pool<in T> {
    operator fun plusAssign (item: T)
    operator fun minusAssign(item: T)
}

typealias ChangeObservers<S>      = Pool<ChangeObserver<S>>
typealias PropertyObservers<S, T> = Pool<PropertyObserver<S, T>>

open class SetPool<T>(protected val delegate: MutableSet<T> = mutableSetOf()): Pool<T>, Set<T> by delegate {
    override fun plusAssign (item: T) { delegate += item }
    override fun minusAssign(item: T) { delegate += item }
}

class ChangeObserversImpl<S>(private val source: S, mutableSet: MutableSet<ChangeObserver<S>> = mutableSetOf()): SetPool<ChangeObserver<S>>(mutableSet) {
    operator fun invoke() = delegate.forEach { it(source) }
}

class PropertyObserversImpl<S, T>(private val source: S, mutableSet: MutableSet<PropertyObserver<S, T>> = mutableSetOf()): SetPool<PropertyObserver<S, T>>(mutableSet) {
    operator fun invoke(old: T, new: T) = delegate.forEach { it(source, old, new) }
}

class ObservableList<S, E>(val source: S, val list: MutableList<E> = mutableListOf()): MutableList<E> by list {

    private val changed_ = SetPool<ListObserver<S, E>>()
    val changed: Pool<ListObserver<S, E>> = changed_

    fun move(element: E, to: Int): Boolean {
        val oldIndex = indexOf(element)

        if (to !in 0 until size || oldIndex < 0 || oldIndex == to) return false

        list.remove(element    )
        list.add   (to, element)

        changed_.forEach {
            it(this, mapOf(), mapOf(), mapOf(to to (oldIndex to element)))
        }

        return true
    }

    override fun add(element: E) = list.add(element).ifTrue {
        changed_.forEach {
            it(this, mapOf(), mapOf(list.size - 1 to element), mapOf())
        }
    }

    override fun remove(element: E): Boolean {
        val index = list.indexOf(element)

        return when {
            index < 0 -> false
            else      -> list.remove(element).ifTrue { changed_.forEach { it(this, mapOf(index to element), mapOf(), mapOf()) } }
        }
    }

    override fun addAll(elements: Collection<E>) = batch { addAll(elements) }

    override fun addAll(index: Int, elements: Collection<E>) = batch { addAll(index, elements) }

    override fun removeAll(elements: Collection<E>) = batch { removeAll(elements) }
    override fun retainAll(elements: Collection<E>) = batch { retainAll(elements) }

    fun replaceAll(elements: Collection<E>) = batch { clear(); addAll(elements) }

    fun <T> batch(block: MutableList<E>.() -> T): T {
        return if (changed_.isEmpty()) {
            list.run(block)
        } else {
            // TODO: Can this be optimized?
            val old = ArrayList(list)

            list.run(block).also {
                if (old != this) {
                    val removed       = mutableMapOf<Int, E>()
                    val added         = mutableMapOf<Int, E>()
                    val moved         = mutableMapOf<Int, Pair<Int, E>>()
                    var unusedIndexes = (0 until this.size).toSet()

                    old.forEachIndexed { index, item ->
                        if (index >= this.size || this[index] != item) {

                            val newIndex = unusedIndexes.firstOrNull { this.getOrNull(it) == item }

                            when (newIndex) {
                                null -> removed[index] = item
                                else -> {
                                    moved[index]   = newIndex to item
                                    unusedIndexes -= newIndex
                                }
                            }
                        }
                    }

                    removed.forEach { (removedIndex, _) ->
                        moved.filterKeys { index -> index >= removedIndex }.entries.sortedBy { it.key }.forEach {
                            if (it.key - 1 == it.value.first) {
                                moved.remove(it.key)
                            } else {
                                moved[it.key - 1] = it.value
                                moved.remove(it.key)
                            }
                        }
                    }

                    unusedIndexes.forEach {
                        val item = this[it]

                        if (it >= old.size || old[it] != item) {
                            added[it] = item

                            // Adjust all the moves
                            moved.filterKeys { index -> index >= it }.entries.sortedByDescending { it.key } .forEach {
                                if (it.key + 1 == it.value.first) {
                                    moved.remove(it.key)
                                } else {
                                    moved[it.key + 1] = it.value
                                    moved.remove(it.key)
                                }
                            }
                        }
                    }

                    changed_.forEach {
                        it(this, removed, added, moved)
                    }
                }
            }
        }
    }

    override fun clear() {
        val size    = list.size
        val oldList = ArrayList(list)

        list.clear()

        changed_.forEach {
            it(this, (0 until size).associate { it to oldList[it] }, mapOf(), mapOf())
        }
    }

    override operator fun set(index: Int, element: E) = list.set(index, element).also { new ->
        if (new !== element) {
            changed_.forEach {
                it(this, mapOf(index to new), mapOf(index to element), mapOf())
            }
        }
    }

    override fun add(index: Int, element: E) {
        list.add(index, element)

        changed_.forEach {
            it(this, mapOf(), mapOf(index to element), mapOf())
        }
    }

    override fun removeAt(index: Int) = list.removeAt(index).also { removed ->
        changed_.forEach {
            it(this, mapOf(index to removed), mapOf(), mapOf())
        }
    }
}

class AdaptingObservableSet<T, S, E>(source: S, delegate: ObservableSet<T, E>): ObservableSet<S, E>(source, delegate)

open class ObservableSet<S, E>(val source: S, private val set: MutableSet<E> = mutableSetOf()): MutableSet<E> by set {
    private val changed_ = SetPool<SetObserver<S, E>>()
    val changed: Pool<SetObserver<S, E>> = changed_

    override fun add(element: E) = set.add(element).ifTrue {
        changed_.forEach {
            it(this, emptySet(), setOf(element))
        }
    }

    override fun remove(element: E): Boolean {
        return set.remove(element).ifTrue { changed_.forEach { it(this, setOf(element), emptySet()) } }
    }

    override fun addAll(elements: Collection<E>) = batch { addAll(elements) }

    override fun removeAll (elements: Collection<E>) = batch { removeAll(elements) }
    override fun retainAll (elements: Collection<E>) = batch { retainAll(elements) }

    fun replaceAll(elements: Collection<E>) = batch { clear(); addAll(elements) }

    fun <T> batch(block: MutableSet<E>.() -> T): T {
        return if (changed_.isEmpty()) {
            set.run(block)
        } else {
            // TODO: Can this be optimized?
            val old = HashSet(set)

            set.run(block).also {
                if (old != this) {
                    changed_.forEach {
                        it(this, old.asSequence().filter { it !in set }.toSet(), set.asSequence().filter { it !in old }.toSet())
                    }
                }
            }
        }
    }

    override fun clear() {
        val oldSet = HashSet(set)

        set.clear()

        changed_.forEach {
            it(this, oldSet, emptySet())
        }
    }
}

open class ObservableProperty<S, T>(initial: T, private val owner: () -> S, private val observers: Iterable<PropertyObserver<S, T>>): ObservableProperty<T>(initial) {
    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T) = newValue != oldValue

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        super.afterChange(property, oldValue, newValue)

        if (oldValue != newValue) {
            observers.forEach { it(owner(), oldValue, newValue) }
        }
    }
}

open class OverridableProperty<T>(initialValue: T, private val changed: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit): ObservableProperty<T>(initialValue) {
    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T) = newValue != oldValue
    override fun afterChange (property: KProperty<*>, oldValue: T, newValue: T) = changed(property, oldValue, newValue)
}

fun <T> observable(initialValue: T, onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit): ReadWriteProperty<Any?, T> = OverridableProperty(initialValue, onChange)