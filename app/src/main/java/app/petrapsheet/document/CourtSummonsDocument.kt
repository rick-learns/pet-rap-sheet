package app.petrapsheet.document

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import app.petrapsheet.booking.Booking
import java.util.Locale

/**
 * The court summons: formal notice to appear before the presiding parody judge on a specified
 * hearing date (7–30 days post-booking), with the mandatory warning and a CASE PENDING stamp.
 */
class CourtSummonsDocument(
    private val booking: Booking,
    fonts: DocumentFonts,
) : PaperDocument(fonts, paperSeed = booking.bookingNumber.hashCode().toLong() + 2L) {

    override val slug = "summons"

    override fun drawContent(canvas: Canvas) {
        drawHeader(canvas)
        drawCaseBox(canvas)
        drawSummonsPreamble(canvas)
        drawHearingBox(canvas)
        drawChargesSummary(canvas)
        drawAdmonitionBox(canvas)
        drawEndorsementAndStamp(canvas)
    }

    private fun drawHeader(canvas: Canvas) {
        HouseholdSeal.draw(canvas, cx = 140f, cy = 132f, radius = 78f, fonts = fonts)
        val title = Ink.text(fonts.bold, 40f)
        val courtName = "MUNICIPAL COURT OF DOMESTIC DISPUTES"
        canvas.drawText(courtName, 248f, 118f, Ink.fitText(title, courtName, 772f))
        val subtitle = Ink.text(fonts.regular, 26f, Ink.FADED)
        val subtext = "District of ${booking.county} · Official Summons to Appear"
        canvas.drawText(subtext, 250f, 162f, Ink.fitText(subtitle, subtext, 770f))
        canvas.drawRect(60f, 222f, 1020f, 226f, Ink.fill(Ink.BLACK))
        canvas.drawRect(60f, 232f, 1020f, 233.5f, Ink.fill(Ink.BLACK))
    }

    private fun drawCaseBox(canvas: Canvas) {
        val box = RectF(60f, 252f, 1020f, 442f)
        canvas.drawRoundRect(box, 8f, 8f, Ink.stroke(2.5f))
        canvas.drawLine(540f, 252f, 540f, 442f, Ink.stroke(1.5f))

        val keyPaint = Ink.text(fonts.regular, 20f, Ink.FADED)
        val valPaint = Ink.text(fonts.bold, 24f)

        // Left column: Defendant particulars
        canvas.drawText("DEFENDANT:", 80f, 292f, keyPaint)
        val nameText = booking.name.uppercase(Locale.US)
        canvas.drawText(nameText, 80f, 326f, Ink.fitText(valPaint, nameText, 420f))

        val aliasText = "a.k.a. " + (booking.alias?.uppercase(Locale.US) ?: "NONE ON FILE")
        canvas.drawText(aliasText, 80f, 362f, Ink.fitText(Ink.text(fonts.regular, 21f), aliasText, 420f))

        val metaText = "SPECIES: ${booking.species.name} · ${booking.county}"
        canvas.drawText(metaText, 80f, 402f, Ink.fitText(keyPaint, metaText, 420f))

        // Right column: Case and court particulars
        canvas.drawText("CASE NUMBER:", 564f, 292f, keyPaint)
        canvas.drawText(booking.caseNumber, 564f, 324f, valPaint)

        canvas.drawText("PRESIDING JUDGE:", 564f, 362f, keyPaint)
        canvas.drawText(booking.judge, 564f, 394f, Ink.fitText(valPaint, booking.judge, 420f))

        val courtroomText = "VENUE: Courtroom 1-A (Main Rug)"
        canvas.drawText(courtroomText, 564f, 424f, Ink.text(fonts.regular, 19f, Ink.FADED))
    }

    private fun drawSummonsPreamble(canvas: Canvas) {
        val titlePaint = Ink.text(fonts.bold, 30f)
        Ink.drawCentered(canvas, "SUMMONS FOR MANDATORY APPEARANCE", 540f, 484f, titlePaint)
        canvas.drawLine(60f, 504f, 1020f, 504f, Ink.stroke(2f))

        val commandPaint = Ink.text(fonts.bold, 22f)
        canvas.drawText("TO THE DEFENDANT ABOVE NAMED:", 64f, 538f, commandPaint)

        val bodyPaint = Ink.text(fonts.regular, 21f)
        val body1 = "You are hereby commanded to appear in person before the Honorable Court"
        val body2 = "at the scheduled date and time to respond to the formal charges set forth below."
        canvas.drawText(body1, 64f, 570f, Ink.fitText(bodyPaint, body1, 950f))
        canvas.drawText(body2, 64f, 598f, Ink.fitText(bodyPaint, body2, 950f))
    }

    private fun drawHearingBox(canvas: Canvas) {
        val box = RectF(60f, 624f, 1020f, 752f)
        canvas.drawRoundRect(box, 8f, 8f, Ink.stroke(2.5f))

        val keyPaint = Ink.text(fonts.bold, 24f)
        val dateValPaint = Ink.text(fonts.bold, 30f)
        val timeValPaint = Ink.text(fonts.regular, 23f)

        canvas.drawText("HEARING DATE:", 88f, 672f, keyPaint)
        canvas.drawText(booking.hearingDateText, 320f, 672f, dateValPaint)

        canvas.drawText("HEARING TIME:", 88f, 720f, keyPaint)
        canvas.drawText("10:00 AM (IMMEDIATELY POST-NAP)", 320f, 720f, timeValPaint)
    }

    private fun drawChargesSummary(canvas: Canvas) {
        canvas.drawText("SCHEDULE OF ALLEGED OFFENSES:", 64f, 792f, Ink.text(fonts.bold, 21f))

        var y = 824f
        val itemPaint = Ink.text(fonts.regular, 21f)
        for ((index, charge) in booking.charges.take(4).withIndex()) {
            val line = "• Count ${index + 1}: ${charge.description} (${charge.statute})"
            canvas.drawText(line, 80f, y, Ink.fitText(itemPaint, line, 920f))
            y += 32f
        }
    }

    private fun drawAdmonitionBox(canvas: Canvas) {
        val box = RectF(60f, 960f, 1020f, 1085f)
        canvas.drawRoundRect(box, 8f, 8f, Ink.stroke(3f))
        val inner = RectF(box).apply { inset(5f, 5f) }
        canvas.drawRoundRect(inner, 4f, 4f, Ink.stroke(1f, Ink.FADED))

        val headingPaint = Ink.text(fonts.bold, 20f, Ink.FADED).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText("JUDICIAL WARNING", box.centerX(), 994f, headingPaint)

        val warningLine = "Failure to appear will result in a stern look."
        val warningPaint = Ink.text(fonts.bold, 28f).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(warningLine, box.centerX(), 1034f, warningPaint)

        val consequencePaint = Ink.text(fonts.regular, 18f, Ink.FADED).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText("Continued absence may prompt an extended sigh or delayed distribution of treats.", box.centerX(), 1068f, consequencePaint)
    }

    private fun drawEndorsementAndStamp(canvas: Canvas) {
        val linePaint = Ink.stroke(1.5f)
        canvas.drawLine(80f, 1200f, 480f, 1200f, linePaint)

        val labelPaint = Ink.text(fonts.regular, 18f, Ink.FADED)
        canvas.drawText("ISSUED UNDER THE SEAL OF THE CLERK", 80f, 1160f, labelPaint)

        val clerkName = "CLERK OF DOMESTIC DISPUTES"
        val namePaint = Ink.fitText(Ink.text(fonts.bold, 22f), clerkName, 380f)
        canvas.drawText(clerkName, 80f, 1230f, namePaint)

        canvas.drawText("HOUSEHOLD DIVISION", 80f, 1256f, Ink.text(fonts.regular, 16f, Ink.FADED))

        RubberStamp.draw(
            canvas = canvas,
            text = "CASE PENDING",
            cx = 780f,
            cy = 1180f,
            angle = 9f,
            textSize = 68f,
            fonts = fonts,
        )
    }
}
