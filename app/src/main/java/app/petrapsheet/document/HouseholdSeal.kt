package app.petrapsheet.document

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF

/**
 * The invented seal of the Bureau of Domestic Mischief: a ringed roundel with a paw print.
 * Deliberately not modelled on any real agency's insignia.
 */
object HouseholdSeal {
    const val AGENCY = "BUREAU OF DOMESTIC MISCHIEF"

    fun draw(canvas: Canvas, cx: Float, cy: Float, radius: Float, fonts: DocumentFonts, color: Int = Ink.BLACK) {
        canvas.drawCircle(cx, cy, radius, Ink.stroke(radius * 0.05f, color))
        canvas.drawCircle(cx, cy, radius * 0.68f, Ink.stroke(radius * 0.025f, color))

        val ringText = Ink.text(fonts.bold, radius * 0.17f, color)
        val upperRadius = radius * 0.78f
        drawOnArc(canvas, "DOMESTIC MISCHIEF", cx, cy, upperRadius, clockwise = true, paint = ringText)
        // The lower arc runs the other way, so its glyphs hang inward from a slightly wider baseline.
        val lowerRadius = upperRadius + ringText.textSize * 0.7f
        drawOnArc(canvas, "· HOUSEHOLD DIVISION ·", cx, cy, lowerRadius, clockwise = false, paint = ringText)

        drawPaw(canvas, cx, cy + radius * 0.06f, radius * 0.5f, Ink.fill(color))
    }

    /** Draws [text] centred on the upper (clockwise) or lower half of a circle, reading left to right. */
    private fun drawOnArc(
        canvas: Canvas,
        text: String,
        cx: Float,
        cy: Float,
        radius: Float,
        clockwise: Boolean,
        paint: Paint,
    ) {
        val arc = Path().apply {
            addArc(RectF(cx - radius, cy - radius, cx + radius, cy + radius), 180f, if (clockwise) 180f else -180f)
        }
        val length = PathMeasure(arc, false).length
        canvas.drawTextOnPath(text, arc, (length - paint.measureText(text)) / 2f, 0f, paint)
    }

    private fun drawPaw(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
        val pad = RectF(cx - size * 0.42f, cy - size * 0.05f, cx + size * 0.42f, cy + size * 0.6f)
        canvas.drawOval(pad, paint)
        val toes = listOf(-0.62f to -0.12f, -0.24f to -0.48f, 0.24f to -0.48f, 0.62f to -0.12f)
        for ((dx, dy) in toes) {
            val x = cx + dx * size
            val y = cy + dy * size
            canvas.drawOval(RectF(x - size * 0.17f, y - size * 0.22f, x + size * 0.17f, y + size * 0.22f), paint)
        }
    }
}
