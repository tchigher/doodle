package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.HorizontalAlignment.Right
import io.nacular.doodle.utils.VerticalAlignment.Bottom
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.VerticalAlignment.Top

/**
 * Created by Nicholas Eddy on 9/25/19.
 */
open class LabelBehavior(protected open val foregroundColor: Color? = null, protected open val backgroundColor: Color? = null): Behavior<Label> {
    override fun install(view: Label) {
        foregroundColor?.let { view.foregroundColor = it }
        backgroundColor?.let { view.backgroundColor = it }
    }

    override fun uninstall(view: Label) {
        view.foregroundColor = null // FIXME: This might override a user-pref
    }

    override fun render(view: Label, canvas: Canvas) {
        view.apply {
            val y = when (verticalAlignment) {
                Top    -> 0.0
                Middle -> (height - textSize.height) / 2
                Bottom ->  height - textSize.height
            }

            val x = when (horizontalAlignment) {
                Left   -> 0.0
                Center -> (width - textSize.width) / 2
                Right  ->  width - textSize.width
            }

            backgroundColor?.let {
                canvas.rect(bounds.atOrigin, ColorFill(it))
            }

            if (wrapsWords) {
                canvas.wrapped(styledText, Point(x, y), 0.0, width)
            } else {
                canvas.text(styledText, Point(x, y))
            }
        }
    }
}