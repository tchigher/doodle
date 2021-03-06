package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.text.StyledText

/**
 * Provides a mechanism to measure the size of various types of text.
 *
 * Created by Nicholas Eddy on 10/30/17.
 *
 * @author Nicholas Eddy
 */
interface TextMetrics {
    fun width (text: String, font: Font? = null                                     ): Double
    fun width (text: String, width: Double, indent: Double = 0.0, font: Font? = null): Double

    fun width (text: StyledText                                                     ): Double
    fun width (text: StyledText, width: Double, indent: Double = 0.0                ): Double

    fun height(text: String, font: Font? = null                                     ): Double
    fun height(text: String, width: Double, indent: Double = 0.0, font: Font? = null): Double

    fun height(text: StyledText                                                     ): Double
    fun height(text: StyledText, width: Double, indent: Double = 0.0                ): Double

    fun size(text: String, font: Font? = null                                     ) = Size(width(text, font), height(text, font))
    fun size(text: String, width: Double, indent: Double = 0.0, font: Font? = null) = Size(width(text, width, indent, font), height(text, width, indent, font))

    fun size(text: StyledText                                     ) = Size(width(text), height(text))
    fun size(text: StyledText, width: Double, indent: Double = 0.0) = Size(width(text, width, indent), height(text, width, indent))
}