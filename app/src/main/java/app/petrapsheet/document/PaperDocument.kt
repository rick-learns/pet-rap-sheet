package app.petrapsheet.document

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.withScale

/**
 * Base for the booking-pack documents: paper texture underneath, the parody notice on top, and
 * the content laid out in a fixed 1080x1350 design space that is scaled to the requested size.
 */
abstract class PaperDocument(
    protected val fonts: DocumentFonts,
    private val paperSeed: Long,
) : Document {

    final override fun render(canvas: Canvas, size: DocumentSize) {
        canvas.withScale(size.width / WIDTH, size.height / HEIGHT) {
            Paper.draw(this, WIDTH, HEIGHT, paperSeed)
            drawContent(this)
            drawParodyNotice(this)
        }
    }

    /** Draws the document body in design coordinates ([WIDTH] x [HEIGHT]). */
    protected abstract fun drawContent(canvas: Canvas)

    private fun drawParodyNotice(canvas: Canvas) {
        val paint = Ink.text(fonts.regular, 22f, Ink.FADED).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(PARODY_NOTICE, WIDTH / 2f, HEIGHT - 34f, paint)
    }

    companion object {
        const val PARODY_NOTICE = "Parody. Not an official document."
        val WIDTH = DocumentSize.PORTRAIT.width.toFloat()
        val HEIGHT = DocumentSize.PORTRAIT.height.toFloat()
    }
}
