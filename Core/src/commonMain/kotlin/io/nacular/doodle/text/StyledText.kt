package io.nacular.doodle.text

import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.Fill
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.text.Target.Background
import io.nacular.doodle.text.Target.Foreground

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
interface Style {
    val font      : Font?
    val foreground: Fill?
    val background: Fill?
}

class StyledText private constructor(val data: MutableList<MutablePair<String, StyleImpl>>): Iterable<Pair<String, Style>> {
    constructor(
        text      : String,
        font      : Font?  = null,
        foreground: Fill? = null,
        background: Fill? = null): this(mutableListOf(MutablePair(text, StyleImpl(font, foreground = foreground, background = background))))

    data class MutablePair<A, B>(var first: A, var second: B) {
        override fun toString() = "($first, $second)"
    }

    val text  get() = data.joinToString { it.first }
    val count get() = data.size

    private var hashCode = data.hashCode()

    override fun iterator() = data.map { it.first to it.second }.iterator()

    operator fun plus(other: StyledText) = this.also { other.data.forEach { style -> add(style) } }

    operator fun rangeTo(font : Font      ) = this.also { add(MutablePair("",   StyleImpl(font))) }
    operator fun rangeTo(color: Color     ) = this.also { add(MutablePair("",   StyleImpl(foreground = ColorFill(color)))) }
    operator fun rangeTo(text : String    ) = this.also { add(MutablePair(text, StyleImpl())) }
    operator fun rangeTo(text : StyledText) = this.also { text.data.forEach { add(MutablePair(it.first, it.second)) } }

    fun copy() = StyledText(mutableListOf(*data.map { MutablePair(it.first, it.second.copy()) }.toTypedArray()))

    private fun add(pair: MutablePair<String, StyleImpl>) {
        val (_, style) = data.last()

        return when (style) {
            pair.second -> data.last().first += pair.first
            else        -> data.plusAssign(pair)
        }.also {
            hashCode = data.hashCode()
        }
    }

    override fun hashCode() = hashCode

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StyledText) return false

        if (data != other.data) return false

        return true
    }

    operator fun Font.invoke(text: StyledText): StyledText {
        text.data.forEach { (_, style) ->
            if (style.font == null) {
                style.font = this
            }
        }

        return text
    }

    data class StyleImpl(override var font: Font? = null, override var foreground: Fill? = null, override var background: Fill? = null): Style
}

// TODO: Change to invoke(text: () -> String) when fixed (https://youtrack.jetbrains.com/issue/KT-22119)
operator fun Font.invoke(text: String          ) = StyledText(text = text, font = this)
operator fun Font.invoke(text: () -> StyledText) = text().apply {
    data.forEach { (_, style) ->
        if (style.font == null) {
            style.font = this@invoke
        }
    }
}

//operator fun Font.get(text: String    ) = StyledText(text = text, font = this)
//operator fun Font.get(text: StyledText) = text.apply {
//    data.forEach { (_, style) ->
//        if (style.font == null) {
//            style.font = this@get
//        }
//    }
//}


enum class Target {
    Background,
    Foreground
}

// TODO: Change to invoke(text: () -> String) when fixed (https://youtrack.jetbrains.com/issue/KT-22119)
operator fun Color.invoke(text: String, target: Target = Foreground) = ColorFill(this).let {
    StyledText(text = text, background = if (target == Background) it else null, foreground = if (target == Foreground) it else null)
}
operator fun Color.invoke(text: () -> StyledText) = text().apply {
    data.forEach { (_, style) ->
        if (style.foreground == null) {
            style.foreground = ColorFill(this@invoke)
        }
    }
}

//operator fun Color.get(text: String, fill: Fill = Foreground) = ColorFill(this).let {
//    StyledText(text = text, background = if (fill == Background) it else null, foreground = if (fill == Foreground) it else null)
//}
//operator fun Color.get(text: StyledText) = text.apply {
//    data.forEach { (_, style) ->
//        if (style.foreground == null) {
//            style.foreground = ColorFill(this@get)
//        }
//    }
//}

operator fun String.rangeTo(styled: StyledText) = StyledText(this) + styled

// "foo" .. font {  } + color { }
