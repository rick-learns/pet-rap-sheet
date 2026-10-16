package app.petrapsheet.document

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface

/** Paint helpers shared by every document. */
object Ink {
    const val BLACK = 0xFF1E1C1A.toInt()
    const val FADED = 0xFF6B655C.toInt()
    const val STAMP_RED = 0xFFB0281F.toInt()
    const val PAPER = 0xFFF3EDDD.toInt()

    fun text(typeface: Typeface, size: Float, color: Int = BLACK) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = size
        this.color = color
    }

    fun stroke(width: Float, color: Int = BLACK) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = width
        this.color = color
    }

    fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

    /** Shrinks [paint] so [text] fits in [maxWidth], never growing past its current size. */
    fun fitText(paint: Paint, text: String, maxWidth: Float, minSize: Float = 12f): Paint {
        val width = paint.measureText(text)
        if (width > maxWidth) paint.textSize = (paint.textSize * maxWidth / width).coerceAtLeast(minSize)
        return paint
    }

    /** Draws [text] centred on ([cx], [cy]) both ways. */
    fun drawCentered(canvas: Canvas, text: String, cx: Float, cy: Float, paint: Paint) {
        val metrics = paint.fontMetrics
        val baseline = cy - (metrics.ascent + metrics.descent) / 2f
        val align = paint.textAlign
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(text, cx, baseline, paint)
        paint.textAlign = align
    }
}
