package io.nacular.doodle.datatransport.dragdrop

import io.nacular.doodle.core.View
import io.nacular.doodle.datatransport.DataBundle
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action
import io.nacular.doodle.geometry.Point

/**
 * Event describing a data drop attemtp for a [View].
 *
 * @property view The target of this event
 * @property location the x-y location within the [view] where this drop is targeted
 * @property bundle the data being dropped
 * @property action the form of data transfer being attempted
 */
class DropEvent(val view: View, val location: Point, val bundle: DataBundle, val action: Action?)


/**
 * Manages data drop attempts for a [View].  It is responsible (through return values from various methods) for
 * determining when a drop operation can be completed for a given [View].
 */
interface DropReceiver {
    /** Indicates whether the receiver is active (responding to drag attempts) */
    val active: Boolean

    /**
     * Indicates that the cursor (with a drop operation) has moved over a [View] monitored by this receiver.
     *
     * @param event The event
     * @return ```true``` if the drop is allowed
     */
    fun dropEnter(event: DropEvent): Boolean

    /**
     * Indicates that the cursor (with a drop operation) has left a [View] monitored by this receiver, or that the view is no
     * longer eligible for drops (i.e. it is invisible or disabled).
     *
     * @param event The event
     */
    fun dropExit(event: DropEvent) {}

    /**
     * Informs receiver that the cursor has moved while over the current drop receiver
     *
     * @param event The event
     * @return ```true``` if the drop is allowed
     */
    fun dropOver(event: DropEvent): Boolean

    /**
     * Informs receiver that the user has selected a new action for this drop operation
     *
     * @param event The event
     * @return ```true``` if the drop is allowed
     */
    fun dropActionChanged(event: DropEvent): Boolean

    /**
     * Informs receiver that the drop operation has ended
     *
     * @param event The event
     * @return ```true``` if the drop is accepted
     */
    fun drop(event: DropEvent): Boolean
}