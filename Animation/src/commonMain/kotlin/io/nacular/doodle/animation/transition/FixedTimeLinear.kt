package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.div
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class FixedTimeLinear<T: Unit>(duration: Measure<Time>, private val endValue: Measure<T>): FixedDuration<T>(duration) {
    override fun value(initial: Moment<T>, timeOffset: Measure<Time>) = Moment(initial.position + ((endValue - initial.position) / duration(initial) * timeOffset), initial.velocity)

    override fun endState(initial: Moment<T>) = Moment(endValue, initial.velocity)
}