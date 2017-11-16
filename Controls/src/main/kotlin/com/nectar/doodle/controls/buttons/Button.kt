package com.nectar.doodle.controls.buttons

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Icon
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.utils.EventObservers
import com.nectar.doodle.utils.EventObserversImpl
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.VerticalAlignment

/**
 * Created by Nicholas Eddy on 11/10/17.
 */
abstract class Button protected constructor(
        text : String         = "",
        icon : Icon<Button>?  = null,
        model: ButtonModel    = ButtonModelImpl()): Gizmo() {

    init {
        model.apply {
            onAction    += ::onAction
            onSelection += ::onSelection
        }
    }

    var text: String        = text
    var icon: Icon<Button>? = icon

    var iconTextSpacing     = 4.0
    var verticalAlignment   = VerticalAlignment.Center
    var horizontalAlignment = HorizontalAlignment.Center

//    var iconAnchor: Anchor?
//        get() = mIconAnchor
//        set(aIconAnchor) {
//            setProperty(object : AbstractNamedProperty<Anchor>(ICON_ANCHOR) {
//                var value: Anchor?
//                    get() = this@Button.mIconAnchor
//                    set(aValue) {
//                        this@Button.mIconAnchor = aValue
//                    }
//            },
//                    aIconAnchor)
//        }

    var pressedIcon          : Icon<Button>? = null
    var disabledIcon         : Icon<Button>? = null
    var selectedIcon         : Icon<Button>? = null
    var mouseOverIcon        : Icon<Button>? = null
    var disabledSelectedIcon : Icon<Button>? = null
    var mouseOverSelectedIcon: Icon<Button>? = null

    var selected: Boolean
        get(   ) = model.selected
        set(new) {
            model.selected = new
        }

    var model: ButtonModel = model
        set(new) {
            field.apply {
                onAction    -= ::onAction
                onSelection -= ::onSelection
            }

            field = new

            field.apply {
                onAction    += ::onAction
                onSelection += ::onSelection
            }
        }

    private val onAction_    by lazy { EventObserversImpl<Button>(this, mutableSetOf()) }
    private val onSelection_ by lazy { EventObserversImpl<Button>(this, mutableSetOf()) }

    val onAction   : EventObservers<Button> = onAction_
    val onSelection: EventObservers<Button> = onSelection_

    abstract fun click()

    override fun handleMouseEvent(event: MouseEvent) {
        super.handleMouseEvent(event)

        when (event.type) {
            SystemMouseEvent.Type.Up    -> mouseReleased(event)
            SystemMouseEvent.Type.Down  -> mousePressed (event)
            SystemMouseEvent.Type.Exit  -> mouseExited  (event)
            SystemMouseEvent.Type.Enter -> mouseEntered (event)
            else                        -> return
        }
    }

    private fun mouseEntered(event: MouseEvent) {
        model.mouseOver = true

        if (enabled) {
            if (event.buttons == setOf(Button1) && model.pressed) {
                model.armed = true
            }

            rerender()
        }
    }

    private fun mouseExited(event: MouseEvent) {
        model.mouseOver = false

        if (enabled) {
            model.armed = false

            rerender()
        }
    }

    private fun mousePressed(event: MouseEvent) {
        if (enabled && event.buttons == setOf(Button1)) {
            model.armed   = true
            model.pressed = true

            rerender()
        }
    }

    private fun mouseReleased(event: MouseEvent) {
        if (enabled) {
            model.pressed = false
            model.armed   = false

            rerender()
        }
    }

    private fun onAction   (model: ButtonModel) = onAction_.forEach    { it(this) }
    private fun onSelection(model: ButtonModel) = onSelection_.forEach { it(this) }
}