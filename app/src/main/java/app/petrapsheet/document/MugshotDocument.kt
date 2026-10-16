package app.petrapsheet.document

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import app.petrapsheet.booking.Booking
import java.util.Locale

/**
 * The booking photograph: the pet in a booking frame against a height chart, holding the county
 * placard, with a BOOKED stamp. [precinctCamera] renders the photo in grayscale.
 */
class MugshotDocument(
    private val booking: Booking,
    private val photo: Bitmap,
    private val precinctCamera: Boolean,
    fonts: DocumentFonts,
) : PaperDocument(fonts, paperSeed = booking.bookingNumber.hashCode().toLong()) {

    override val slug = "mugshot"

    override fun drawContent(canvas: Canvas) {
        drawHeader(canvas)
        drawHeightChart(canvas)
        drawPhoto(canvas)
        canvas.drawRect(FRAME, Ink.stroke(8f))
        drawPlacard(canvas)
        drawDetails(canvas)
        RubberStamp.draw(canvas, "BOOKED", cx = 820f, cy = 1205f, angle = -9f, textSize = 74f, fonts = fonts)
    }

    private fun drawHeader(canvas: Canvas) {
        HouseholdSeal.draw(canvas, cx = 140f, cy = 132f, radius = 78f, fonts = fonts)
        val title = Ink.text(fonts.bold, 42f)
        canvas.drawText(HouseholdSeal.AGENCY, 248f, 118f, Ink.fitText(title, HouseholdSeal.AGENCY, 772f))
        val subtitle = Ink.text(fonts.regular, 27f, Ink.FADED)
        canvas.drawText("Intake Division · Booking Photograph", 250f, 162f, subtitle)
        canvas.drawRect(60f, 222f, 1020f, 226f, Ink.fill(Ink.BLACK))
        canvas.drawRect(60f, 232f, 1020f, 233.5f, Ink.fill(Ink.BLACK))
    }

    /** Inch marks up the left gutter, centimetres up the right, measured from the frame floor. */
    private fun drawHeightChart(canvas: Canvas) {
        canvas.drawRect(FRAME, Ink.fill(CHART_WALL))
        val floor = FRAME.bottom
        val line = Ink.stroke(2f, CHART_INK)
        val faint = Ink.stroke(1.2f, CHART_INK).apply { alpha = 110 }
        val label = Ink.text(fonts.bold, 22f, CHART_INK)

        var inch = 1
        while (floor - inch * PX_PER_INCH > LINE_TOP) {
            val y = floor - inch * PX_PER_INCH
            canvas.drawLine(FRAME.left, y, PHOTO.left, y, if (inch % 2 == 0) line else faint)
            if (inch % 4 == 0 && y > LABEL_TOP) {
                label.textAlign = Paint.Align.LEFT
                canvas.drawText("$inch\"", FRAME.left + 12f, y - 6f, label)
            }
            inch++
        }

        var cm = 1
        while (floor - cm * PX_PER_CM > LINE_TOP) {
            val y = floor - cm * PX_PER_CM
            val length = when {
                cm % 10 == 0 -> FRAME.right - PHOTO.right
                cm % 5 == 0 -> 44f
                else -> 22f
            }
            canvas.drawLine(PHOTO.right, y, PHOTO.right + length, y, if (cm % 5 == 0) line else faint)
            if (cm % 10 == 0 && y > LABEL_TOP) {
                label.textAlign = Paint.Align.RIGHT
                canvas.drawText("$cm cm", FRAME.right - 10f, y - 6f, label)
            }
            cm++
        }
        label.textAlign = Paint.Align.LEFT
        canvas.drawText("IN", FRAME.left + 12f, FRAME.top + 34f, label)
        label.textAlign = Paint.Align.RIGHT
        canvas.drawText("CM", FRAME.right - 10f, FRAME.top + 34f, label)
    }

    private fun drawPhoto(canvas: Canvas) {
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        if (precinctCamera) paint.colorFilter = ColorMatrixColorFilter(precinctMatrix())
        canvas.drawBitmap(photo, cropToFill(photo.width, photo.height, PHOTO), PHOTO, paint)
        canvas.drawRect(PHOTO, Ink.stroke(4f))

        if (precinctCamera) {
            val stamp = Ink.text(fonts.bold, 24f, 0xE6FFFFFF.toInt()).apply {
                setShadowLayer(3f, 0f, 1f, 0xAA000000.toInt())
            }
            canvas.drawText("PRECINCT CAM 2  ${booking.bookedOnText}", PHOTO.left + 18f, PHOTO.top + 38f, stamp)
        }
    }

    private fun drawPlacard(canvas: Canvas) {
        val board = PLACARD
        canvas.drawRoundRect(board, 10f, 10f, Ink.fill(PLACARD_BLACK))
        val trim = RectF(board).apply { inset(8f, 8f) }
        canvas.drawRoundRect(trim, 6f, 6f, Ink.stroke(2f, PLACARD_WHITE).apply { alpha = 150 })

        val maxWidth = board.width() - 48f
        val name = booking.name.uppercase(Locale.US)
        val namePaint = Ink.fitText(Ink.text(fonts.bold, 46f, PLACARD_WHITE), name, maxWidth)
        Ink.drawCentered(canvas, name, board.centerX(), board.top + 42f, namePaint)

        val linePaint = Ink.fitText(Ink.text(fonts.bold, 27f, PLACARD_WHITE), booking.placardLine, maxWidth)
        Ink.drawCentered(canvas, booking.placardLine, board.centerX(), board.top + 92f, linePaint)

        val datePaint = Ink.text(fonts.regular, 25f, PLACARD_WHITE)
        Ink.drawCentered(canvas, "DATE ${booking.bookedOnText}", board.centerX(), board.top + 130f, datePaint)
    }

    private fun drawDetails(canvas: Canvas) {
        val key = Ink.text(fonts.regular, 28f, Ink.FADED)
        val rows = listOf(
            "NAME" to booking.name.uppercase(Locale.US),
            "A.K.A." to (booking.alias?.uppercase(Locale.US) ?: "NONE ON FILE"),
            "SPECIES" to booking.species.name,
            "BOOKED" to booking.bookedOnText,
        )
        var y = 1152f
        for ((label, text) in rows) {
            canvas.drawText(label, 64f, y, key)
            canvas.drawText(text, 230f, y, Ink.fitText(Ink.text(fonts.bold, 28f), text, 420f))
            y += 40f
        }
    }

    private fun precinctMatrix() = ColorMatrix().apply {
        setSaturation(0f)
        // A touch of contrast, like a cheap booking-desk camera.
        val contrast = 1.12f
        val offset = 128f * (1f - contrast)
        postConcat(
            ColorMatrix(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, offset,
                    0f, contrast, 0f, 0f, offset,
                    0f, 0f, contrast, 0f, offset,
                    0f, 0f, 0f, 1f, 0f,
                ),
            ),
        )
    }

    companion object {
        val FRAME = RectF(60f, 252f, 1020f, 1100f)
        val PHOTO = RectF(172f, 252f, 908f, 1100f)
        val PLACARD = RectF(214f, 932f, 866f, 1084f)

        /** Chart lines and numbers stay clear of the IN/CM headings. */
        private val LINE_TOP = FRAME.top + 48f
        private val LABEL_TOP = FRAME.top + 76f
        private const val PX_PER_INCH = 26f
        private const val PX_PER_CM = PX_PER_INCH / 2.54f
        private const val CHART_WALL = 0xFFD9DAD3.toInt()
        private const val CHART_INK = 0xFF34383B.toInt()
        private const val PLACARD_BLACK = 0xFF161616.toInt()
        private const val PLACARD_WHITE = 0xFFF2F0EA.toInt()

        /** Centre-crops the source to [dst]'s aspect ratio, biased a little upward toward faces. */
        fun cropToFill(width: Int, height: Int, dst: RectF): Rect {
            val dstAspect = dst.width() / dst.height()
            return if (width.toFloat() / height > dstAspect) {
                val cropWidth = (height * dstAspect).toInt()
                val left = (width - cropWidth) / 2
                Rect(left, 0, left + cropWidth, height)
            } else {
                val cropHeight = (width / dstAspect).toInt()
                val top = ((height - cropHeight) * 0.4f).toInt()
                Rect(0, top, width, top + cropHeight)
            }
        }
    }
}
