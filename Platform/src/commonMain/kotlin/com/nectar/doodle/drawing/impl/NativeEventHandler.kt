package com.nectar.doodle.drawing.impl

import com.nectar.doodle.HTMLElement


typealias NativeEventHandlerFactory = (element: HTMLElement, listener: NativeEventListener) -> NativeEventHandler

interface NativeEventHandler {
    fun startConsumingMouseMoveEvents(onlySelf: Boolean = false)
    fun stopConsumingMouseMoveEvents ()

    fun startConsumingMousePressEvents()
    fun stopConsumingMousePressEvents ()

    fun startConsumingSelectionEvents()
    fun stopConsumingSelectionEvents ()

    fun registerFocusListener  ()
    fun unregisterFocusListener()

    fun registerFocusInListener  ()
    fun unregisterFocusInListener()

    fun registerClickListener  ()
    fun unregisterClickListener()

    fun registerKeyListener  ()
    fun unregisterKeyListener()

    fun registerScrollListener  ()
    fun unregisterScrollListener()

    fun registerChangeListener  ()
    fun unregisterChangeListener()

    fun registerInputListener  ()
    fun unregisterInputListener()
}