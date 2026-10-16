package app.petrapsheet.document

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.core.graphics.withRotation
import kotlin.random.Random

/** A rubber-stamp mark ("BOOKED", "CASE PENDING"): boxed capitals at a slight angle, worn ink. */
object RubberStamp {
    fun draw(
        canvas: Canvas,
        text: String,
        cx: Float,
        cy: Float,
        angle: Float,
        textSize: Float,
        fonts: DocumentFonts,
        color: Int = Ink.STAMP_RED,
        seed: Long = text.hashCode().toLong(),
    ) {
        val paint = Ink.text(fonts.bold, textSize, color).apply { letterSpacing = 0.12f }
        val textWidth = paint.measureText(text)
        val padX = textSize * 0.45f
        val padY = textSize * 0.32f
        val box = RectF(
            cx - textWidth / 2f - padX,
            cy - textSize / 2f - padY,
            cx + textWidth / 2f + padX,
            cy + textSize / 2f + padY,
        )
        val outer = textSize * 0.09f
        val corner = textSize * 0.16f

        canvas.withRotation(angle, cx, cy) {
            val bounds = RectF(box).apply { inset(-outer * 3, -outer * 3) }
            val layer = canvas.saveLayerAlpha(bounds, 225)

            canvas.drawRoundRect(box, corner, corner, Ink.stroke(outer, color))
            val inner = RectF(box).apply { inset(outer * 2f, outer * 2f) }
            canvas.drawRoundRect(inner, corner * 0.6f, corner * 0.6f, Ink.stroke(outer * 0.45f, color))
            Ink.drawCentered(canvas, text, cx, cy, paint)

            // Wear: knock small holes out of the ink so it reads as a hand-pressed stamp.
            val random = Random(seed)
            val eraser = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            }
            repeat(((bounds.width() * bounds.height()) / 220f).toInt()) {
                eraser.alpha = random.nextInt(60, 255)
                canvas.drawCircle(
                    bounds.left + random.nextFloat() * bounds.width(),
                    bounds.top + random.nextFloat() * bounds.height(),
                    0.8f + random.nextFloat() * textSize * 0.035f,
                    eraser,
                )
            }
            canvas.restoreToCount(layer)
        }
    }
}
