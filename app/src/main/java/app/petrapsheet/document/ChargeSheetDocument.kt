package app.petrapsheet.document

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import app.petrapsheet.booking.Booking
import java.util.Locale

/**
 * The formal charge sheet: suspect particulars, enumerated charges with domestic statutes,
 * bail determination in treats, arresting officer signature, and a CASE PENDING stamp.
 */
class ChargeSheetDocument(
    private val booking: Booking,
    fonts: DocumentFonts,
) : PaperDocument(fonts, paperSeed = booking.bookingNumber.hashCode().toLong() + 1L) {

    override val slug = "charge-sheet"

    override fun drawContent(canvas: Canvas) {
        drawHeader(canvas)
        drawSuspectBox(canvas)
        drawCharges(canvas)
        drawBailBox(canvas)
        drawSignaturesAndStamp(canvas)
    }

    private fun drawHeader(canvas: Canvas) {
        HouseholdSeal.draw(canvas, cx = 140f, cy = 132f, radius = 78f, fonts = fonts)
        val title = Ink.text(fonts.bold, 42f)
        canvas.drawText(HouseholdSeal.AGENCY, 248f, 118f, Ink.fitText(title, HouseholdSeal.AGENCY, 772f))
        val subtitle = Ink.text(fonts.regular, 26f, Ink.FADED)
        val subtext = "County of ${booking.county} · Formal Charge Sheet"
        canvas.drawText(subtext, 250f, 162f, Ink.fitText(subtitle, subtext, 770f))
        canvas.drawRect(60f, 222f, 1020f, 226f, Ink.fill(Ink.BLACK))
        canvas.drawRect(60f, 232f, 1020f, 233.5f, Ink.fill(Ink.BLACK))
    }

    private fun drawSuspectBox(canvas: Canvas) {
        val box = RectF(60f, 252f, 1020f, 442f)
        canvas.drawRoundRect(box, 8f, 8f, Ink.stroke(2.5f))

        val keyPaint = Ink.text(fonts.regular, 22f, Ink.FADED)
        val valPaint = Ink.text(fonts.bold, 24f)

        // Row 1
        canvas.drawText("SUSPECT:", 84f, 296f, keyPaint)
        canvas.drawText(booking.name.uppercase(Locale.US), 220f, 296f, Ink.fitText(valPaint, booking.name.uppercase(Locale.US), 320f))

        canvas.drawText("SPECIES:", 580f, 296f, keyPaint)
        canvas.drawText(booking.species.name, 720f, 296f, valPaint)

        // Row 2
        canvas.drawText("A.K.A.:", 84f, 352f, keyPaint)
        val aliasText = booking.alias?.uppercase(Locale.US) ?: "NONE ON FILE"
        canvas.drawText(aliasText, 220f, 352f, Ink.fitText(valPaint, aliasText, 320f))

        canvas.drawText("BOOKING NO.:", 580f, 352f, keyPaint)
        canvas.drawText(booking.bookingNumber, 740f, 352f, valPaint)

        // Row 3
        canvas.drawText("BOOKED:", 84f, 408f, keyPaint)
        canvas.drawText(booking.bookedOnText, 220f, 408f, valPaint)

        canvas.drawText("OFFICER:", 580f, 408f, keyPaint)
        val officerText = booking.officer.uppercase(Locale.US)
        canvas.drawText(officerText, 720f, 408f, Ink.fitText(valPaint, officerText, 270f))
    }

    private fun drawCharges(canvas: Canvas) {
        val sectionTitle = Ink.text(fonts.bold, 30f)
        canvas.drawText("STATEMENT OF CHARGES", 60f, 485f, sectionTitle)

        val subtext = "DEFENDANT IS FORMALLY CHARGED WITH THE FOLLOWING CODE VIOLATIONS:"
        canvas.drawText(subtext, 60f, 514f, Ink.text(fonts.regular, 19f, Ink.FADED))
        canvas.drawLine(60f, 528f, 1020f, 528f, Ink.stroke(2f))

        val charges = booking.charges
        val count = charges.size.coerceAtLeast(1)
        val availableHeight = 400f
        val step = (availableHeight / count).coerceIn(84f, 130f)

        val countPaint = Ink.text(fonts.bold, 23f)
        val descPaint = Ink.text(fonts.bold, 25f)
        val statutePaint = Ink.text(fonts.regular, 22f, Ink.FADED)

        var y = 562f
        for ((index, charge) in charges.withIndex()) {
            val countLabel = "COUNT ${index + 1}:"
            canvas.drawText(countLabel, 64f, y, countPaint)

            val statuteText = charge.statute
            canvas.drawText(statuteText, 200f, y, Ink.fitText(statutePaint, statuteText, 800f))

            val desc = "“${charge.description}”"
            canvas.drawText(desc, 90f, y + 36f, Ink.fitText(descPaint, desc, 900f))

            if (index < charges.size - 1) {
                canvas.drawLine(64f, y + 54f, 1016f, y + 54f, Ink.stroke(1f, 0x33000000))
            }
            y += step
        }
    }

    private fun drawBailBox(canvas: Canvas) {
        val box = RectF(60f, 960f, 1020f, 1085f)
        canvas.drawRoundRect(box, 8f, 8f, Ink.stroke(2.5f))
        val inner = RectF(box).apply { inset(5f, 5f) }
        canvas.drawRoundRect(inner, 4f, 4f, Ink.stroke(1f, Ink.FADED))

        val headingPaint = Ink.text(fonts.bold, 20f, Ink.FADED).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText("OFFICIAL BAIL DETERMINATION", box.centerX(), 994f, headingPaint)

        val bailLine = "BAIL SET AT: ${booking.bail.uppercase(Locale.US)}"
        val bailPaint = Ink.fitText(Ink.text(fonts.bold, 30f), bailLine, box.width() - 40f)
        Ink.drawCentered(canvas, bailLine, box.centerX(), 1034f, bailPaint)

        val remarkPaint = Ink.text(fonts.regular, 18f, Ink.FADED).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText("REMITTANCE PAYABLE IN TREATS UPON VERIFIED GOOD BEHAVIOUR", box.centerX(), 1068f, remarkPaint)
    }

    private fun drawSignaturesAndStamp(canvas: Canvas) {
        val linePaint = Ink.stroke(1.5f)
        canvas.drawLine(80f, 1200f, 480f, 1200f, linePaint)

        val labelPaint = Ink.text(fonts.regular, 18f, Ink.FADED)
        canvas.drawText("ARRESTING OFFICER SIGNATURE", 80f, 1160f, labelPaint)

        val officerName = booking.officer.uppercase(Locale.US)
        val namePaint = Ink.fitText(Ink.text(fonts.bold, 22f), officerName, 380f)
        canvas.drawText(officerName, 80f, 1230f, namePaint)

        canvas.drawText(HouseholdSeal.AGENCY, 80f, 1256f, Ink.text(fonts.regular, 16f, Ink.FADED))

        RubberStamp.draw(
            canvas = canvas,
            text = "CASE PENDING",
            cx = 780f,
            cy = 1180f,
            angle = -9f,
            textSize = 68f,
            fonts = fonts,
        )
    }
}
