package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.controls.Table
import com.nectar.doodle.controls.Table.Column
import com.nectar.doodle.controls.TableBehavior
import com.nectar.doodle.controls.TableBehavior.CellGenerator
import com.nectar.doodle.controls.TableBehavior.HeaderCellGenerator
import com.nectar.doodle.controls.TableBehavior.HeaderGeometry
import com.nectar.doodle.controls.TableBehavior.HeaderPositioner
import com.nectar.doodle.controls.TableBehavior.RowPositioner
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.CanvasBrush
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.system.SystemInputEvent
import com.nectar.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
class BasicTableBehavior<T>(
        private val focusManager: FocusManager?,
        private val textMetrics : TextMetrics,
                val rowHeight   : Double = 20.0,
                val headerColor : Color? = lightgray,
                val evenRowColor: Color? = white,
                val oddRowColor : Color? = lightgray.lighter().lighter()): TableBehavior<T>, KeyListener {

    override val cellGenerator = object: CellGenerator<T> {
        override fun <A> invoke(table: Table<T, *>, cell: A, row: Int, current: View?): View = when (current) {
            is ListRow<*> -> (current as ListRow<A>).apply { update(table, cell, row) }
            else          -> ListRow(textMetrics, table, cell, row).apply {
                mouseChanged += object: MouseListener {
                    override fun mouseReleased(event: MouseEvent) {
                        focusManager?.requestFocus(table)
                    }
                }
            }
        }
    }

    override val headerPositioner = object: HeaderPositioner<T> {
        override fun invoke(table: Table<T, *>) = HeaderGeometry(0.0, 1.1 * rowHeight)
    }

    override val rowPositioner = object: RowPositioner<T> {
        private val delegate = ListPositioner(rowHeight)

        override fun invoke(table: Table<T, *>, row: T, index: Int) = delegate.invoke(table, table.insets, index)
        override fun rowFor(table: Table<T, *>, y: Double)          = delegate.rowFor(table.insets, y)
    }

    override val headerCellGenerator = object: HeaderCellGenerator<T> {
        override fun invoke(table: Table<T, *>, column: Column<T, *>) = Label(textMetrics, StyledText(column.text)).apply { fitText = false; backgroundColor = headerColor }
    }

    override fun render(view: Table<T, *>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, CanvasBrush(Size(rowHeight, 2 * rowHeight)) {
            evenRowColor?.let { rect(Rectangle(                rowHeight, rowHeight), ColorBrush(it)) }
            oddRowColor?.let  { rect(Rectangle(0.0, rowHeight, rowHeight, rowHeight), ColorBrush(it )) }
        })
    }

    // FIXME: Centralize
    override fun install(view: Table<T, *>) {
        view.keyChanged += this
    }

    override fun uninstall(view: Table<T, *>) {
        view.keyChanged -= this
    }

    override fun keyPressed(event: KeyEvent) {
        (event.source as Selectable<Int>).let { list ->
            when (event.code) {
                KeyEvent.VK_UP, KeyEvent.VK_DOWN -> {
                    when (SystemInputEvent.Modifier.Shift) {
                        in event -> {
                            list.selectionAnchor?.let { anchor ->
                                list.lastSelection?.let { if (event.code == KeyEvent.VK_UP) list.previous(it) else list.next(it) }?.let { current ->
                                    when {
                                        current < anchor  -> list.setSelection((current .. anchor ).reversed().toSet())
                                        anchor  < current -> list.setSelection((anchor  .. current).           toSet())
                                        else              -> list.setSelection(setOf(current))
                                    }
                                }
                            }
                        }
                        else -> list.lastSelection?.let { if (event.code == KeyEvent.VK_UP) list.previous(it) else list.next(it) }?.let { list.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }

                KeyEvent.VK_A                    -> {
                    if (SystemInputEvent.Modifier.Ctrl in event || SystemInputEvent.Modifier.Meta in event) {
                        list.selectAll()
                    }
                }
            }
        }
    }
}