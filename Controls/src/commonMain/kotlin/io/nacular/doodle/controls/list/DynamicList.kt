package io.nacular.doodle.controls.list

import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.IndexedItemVisualizer
import io.nacular.doodle.controls.SelectableItemVisualizer
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.mutableListModelOf
import io.nacular.doodle.controls.ignoreIndex
import io.nacular.doodle.utils.size

open class DynamicList<T, M: DynamicListModel<T>>(
        model         : M,
        itemGenerator : IndexedItemVisualizer<T>? = null,
        selectionModel: SelectionModel<Int>?      = null,
        fitContent    : Boolean                   = true,
        scrollCache   : Int                       = 10): List<T, M>(model, itemGenerator, selectionModel, fitContent, scrollCache) {

    private val modelChanged: ModelObserver<T> = { _,removed,added,moved ->
        var trueRemoved = removed.filterKeys { it !in added   }
        var trueAdded   = added.filterKeys   { it !in removed }

        itemsChanged(added = trueAdded, removed = trueRemoved, moved = moved)

        val oldVisibleRange = firstVisibleRow..lastVisibleRow

        trueRemoved = trueRemoved.filterKeys { it <= lastVisibleRow }

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty()) {
            updateVisibleHeight()
        }

        trueAdded = trueAdded.filterKeys   { it <= lastVisibleRow }

        if (trueRemoved.size > trueAdded.size && oldVisibleRange.size != (firstVisibleRow..lastVisibleRow).size) {
            val numToRemove = oldVisibleRange.size - (firstVisibleRow..lastVisibleRow).size
            children.batch {
                for (it in 0 until numToRemove) {
                    removeAt(0)
                }
            }
        }

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty() || moved.isNotEmpty()) {
            // FIXME: Make this more efficient
            (firstVisibleRow..lastVisibleRow).forEach { update(children, it) }
        } else {
            // These are the edited rows
            added.keys.filter { it in removed }.forEach { update(children, it) }
        }
    }

    init {
        model.changed += modelChanged
    }

    override fun removedFromDisplay() {
        model.changed -= modelChanged

        super.removedFromDisplay()
    }

    private fun itemsChanged(added: Map<Int, T>, removed: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) {
        if (selectionModel != null) {

            val effectiveAdded  : Map<Int, T> = added   + moved.values.associate { it.first to it.second }
            val effectiveRemoved: Map<Int, T> = removed + moved.mapValues { it.value.second }

            val updatedSelection = mutableSetOf<Int>()

            for (selectionItem in selectionModel) {
                var delta = 0

                for (index in effectiveAdded.keys) {
                    if (selectionItem >= index) {
                        ++delta
                    }
                }

                for (index in effectiveRemoved.keys) {
                    if (selectionItem > index) {
                        delta--
                    }
                }

                if (delta != 0) {
                    updatedSelection.add(selectionItem + delta)
                }
            }

            removeSelection(removed.keys)

            if (updatedSelection.isNotEmpty()) {
                setSelection(updatedSelection)
            }
        }
    }

    companion object {
        operator fun invoke(
                progression   : IntProgression,
                itemGenerator : IndexedItemVisualizer<Int>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10) =
                DynamicList(progression.toMutableList(), itemGenerator, selectionModel, fitContent, scrollCache)

        inline operator fun <reified T> invoke(
                values        : kotlin.collections.List<T>,
                itemGenerator : IndexedItemVisualizer<T>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): DynamicList<T, MutableListModel<T>> =
                DynamicList(mutableListModelOf(*values.toTypedArray()), itemGenerator, selectionModel, fitContent, scrollCache)

        operator fun <T, M: DynamicListModel<T>> invoke(
                model         : M,
                itemGenerator : SelectableItemVisualizer<T>?   = null,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10) =
                DynamicList(model, itemGenerator?.let { ignoreIndex(it) }, selectionModel, fitContent, scrollCache)

    }
}