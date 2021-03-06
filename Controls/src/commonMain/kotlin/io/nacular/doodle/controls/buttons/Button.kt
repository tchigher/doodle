package io.nacular.doodle.controls.buttons

import io.nacular.doodle.accessibility.button
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.Anchor.Left
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.ObservableProperty
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.VerticalAlignment.Middle

/**
 * Created by Nicholas Eddy on 11/10/17.
 */

@Suppress("PrivatePropertyName")
abstract class Button protected constructor(
            text : String        = "",
            icon : Icon<Button>? = null,
            model: ButtonModel  = ButtonModelImpl()): View(accessibilityRole = button()) {

    private val armedChanged_       = { _: ButtonModel, old: Boolean, new: Boolean -> (armedChanged       as PropertyObserversImpl)(old, new) }
    private val pressedChanged_     = { _: ButtonModel, old: Boolean, new: Boolean -> (pressedChanged     as PropertyObserversImpl)(old, new) }
    private val pointerOverChanged_ = { _: ButtonModel, old: Boolean, new: Boolean -> (pointerOverChanged as PropertyObserversImpl)(old, new) }
    private val modelFired          = { _: ButtonModel -> (fired as ChangeObserversImpl).forEach { it(this) } }

    override fun addedToDisplay() {
        super.addedToDisplay()

        registerModel(model)
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        unregisterModel(model)
    }

    private fun registerModel(model: ButtonModel) {
        model.fired              += modelFired
        model.armedChanged       += armedChanged_
        model.pressedChanged     += pressedChanged_
        model.pointerOverChanged += pointerOverChanged_
    }

    private fun unregisterModel(model: ButtonModel) {
        model.fired              -= modelFired
        model.armedChanged       -= armedChanged_
        model.pressedChanged     -= pressedChanged_
        model.pointerOverChanged -= pointerOverChanged_
    }

    val textChanged: PropertyObservers<Button, String> by lazy { PropertyObserversImpl<Button, String>(this) }

    var text by ObservableProperty(text, { this }, textChanged as PropertyObserversImpl<Button, String>)

    val fired: ChangeObservers<Button> by lazy { ChangeObserversImpl(this) }

    val armedChanged      : PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl<Button, Boolean>(this) }
    val pressedChanged    : PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl<Button, Boolean>(this) }
    val pointerOverChanged: PropertyObservers<Button, Boolean> by lazy { PropertyObserversImpl<Button, Boolean>(this) }

    var behavior: Behavior<Button>? = null
        set(new) {
            if (field == new) { return }

            clipCanvasToBounds = true
            field?.uninstall(this)

            field = new?.also {
                it.install(this)
                clipCanvasToBounds = it.clipCanvasToBounds(this)
            }
        }

    var iconTextSpacing = 4.0; set(new) { field = new; styleChanged { true } }

    var verticalAlignment = Middle; set(new) { field = new; styleChanged { true } }

    var horizontalAlignment = Center; set(new) { field = new; styleChanged { true } }

    var iconAnchor = Left; set(new) { field = new; styleChanged { true } }

    var icon                   : Icon<Button>? = icon;                                set(new) { field = new; styleChanged { true } }
    var pressedIcon            : Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }
    var disabledIcon           : Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }
    var selectedIcon           : Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }
    var pointerOverIcon        : Icon<Button>? = null; get() = field ?: icon;         set(new) { field = new; styleChanged { true } }
    var disabledSelectedIcon   : Icon<Button>? = null; get() = field ?: disabledIcon; set(new) { field = new; styleChanged { true } }
    var pointerOverSelectedIcon: Icon<Button>? = null; get() = field ?: selectedIcon; set(new) { field = new; styleChanged { true } }

    var selected: Boolean
        get(   ) = model.selected
        set(new) {
            if (enabled) {
                model.selected = new
            }
        }

    open var model: ButtonModel = model
        set(new) {
            unregisterModel(field)

            field = new

            if (displayed) {
                registerModel(field)
            }
        }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = super.contains(point) && behavior?.contains(this, point) ?: true

    abstract fun click()
}
