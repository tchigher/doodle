package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl

/**
 * Represents a selection slider that can be [Horizontal] or [Vertical][io.nacular.doodle.utils.Orientation.Vertical].
 *
 * @constructor
 * @param model containing range and value
 * @param orientation of the control
 */
open class Slider(model: ConfinedValueModel<Double>, val orientation: Orientation = Horizontal): ValueSlider(model) {
    /**
     * Creates a Slider with a given range and starting value.
     *
     * @param range of the bar
     * @param value to start with
     * @param orientation of the control
     */
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start, orientation: Orientation = Horizontal): this(BasicConfinedValueModel(range, value), orientation)

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<Slider, Double>(this) }

    val changed: PropertyObservers<Slider, Double> = changed_

    var behavior: Behavior<Slider>? = null
        set(new) {
            if (field == new) { return }

            field?.uninstall(this)
            field = new?.apply { install(this@Slider) }
        }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = super.contains(point) && behavior?.contains(this, point) ?: true

    override fun changed(old: Double, new: Double) {
        changed_(old, new)
    }
}