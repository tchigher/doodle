package io.nacular.doodle.controls

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.image.Image

/**
 * A simple wrapper around an [Image]. The image is scaled to fit within the
 * bounds of this Photo when drawn.
 */
class Photo(private var image: Image): View() {
    init {
        size = image.size
    }

    override fun render(canvas: Canvas) {
        canvas.image(image, destination = bounds.atOrigin)
    }
}