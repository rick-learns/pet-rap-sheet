package app.petrapsheet.document

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.random.Random

/** Off-white paper drawn in code: a warm base, darker edges, speckles and a few fibres. */
object Paper {
    fun draw(canvas: Canvas, width: Float, height: Float, seed: Long) {
        canvas.drawColor(Ink.PAPER)

        val edge = Paint().apply {
            shader = RadialGradient(
                width / 2f, height / 2f, maxOf(width, height) * 0.75f,
                intArrayOf(0x00000000, 0x00000000, 0x2A5A4630),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width, height, edge)

        val random = Random(seed)
        val speck = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(2600) {
            val dark = random.nextFloat() < 0.7f
            speck.color = if (dark) 0x000000 else 0xFFFFFF
            speck.alpha = if (dark) random.nextInt(8, 30) else random.nextInt(40, 90)
            canvas.drawCircle(
                random.nextFloat() * width,
                random.nextFloat() * height,
                0.6f + random.nextFloat() * 1.4f,
                speck,
            )
        }

        val fibre = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = 0x1A6B5A44
        }
        repeat(70) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val length = 8f + random.nextFloat() * 22f
            val angle = random.nextFloat() * Math.PI.toFloat()
            canvas.drawLine(
                x, y,
                x + length * kotlin.math.cos(angle), y + length * kotlin.math.sin(angle),
                fibre,
            )
        }
    }
}
