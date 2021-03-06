package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Color.Companion.Transparent
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
private infix fun IntArray.contentEquals2(other: IntArray?): Boolean = when(other) {
    null -> false
    else -> this contentEquals other
}

class PenTests {
    @Test @JsName("defaultsCorrect")
    fun `defaults correct`() {
        Stroke().apply {
            expect(Black) { color     }
            expect(1.0  ) { thickness }
            expect(null ) { dashes    }
            expect(true ) { visible   }
        }
    }

    @Test @JsName("handlesDashVarArgs")
    fun `handles dash var args`() {
        Stroke(dash = 1, remainingDashes = *intArrayOf(2, 3, 4)).apply {
            expect(true) { intArrayOf(1,2,3,4) contentEquals2 dashes }
        }
    }

    @Test @JsName("visibilityCorrect")
    fun `visibility correct`() {
        listOf(
            Stroke(                            ) to true,
            Stroke(color     = Red             ) to true,
            Stroke(color     = Transparent     ) to false,
            Stroke(color     = Green opacity 0f) to false,
            Stroke(thickness = 0.0             ) to false
        ).forEach {
            expect(it.second) { it.first.visible }
        }
    }
}