package app.petrapsheet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import app.petrapsheet.booking.Booking
import app.petrapsheet.booking.Species
import java.io.File
import java.time.LocalDate

/**
 * Shared test inputs. The suspect photo, `fixtures/suspect.jpg`, is original artwork drawn in code
 * by [drawSuspect] (Apache-2.0 like the rest of the repo; no third-party image). Tests decode the
 * committed JPEG rather than encoding one at test time, so every machine sees identical pixels.
 * Regenerate it with `SuspectArtworkGenerator`.
 */
object Fixtures {
    const val SUSPECT_RESOURCE = "fixtures/suspect.jpg"
    const val SUSPECT_JPEG_QUALITY = 95

    private const val WIDTH = 480
    private const val HEIGHT = 595

    private val BACKDROP = Color.rgb(196, 199, 203)
    private val SPOTLIGHT = Color.rgb(214, 216, 219)
    private val FUR = Color.rgb(205, 150, 88)
    private val FUR_SHADE = Color.rgb(176, 120, 64)
    private val CREAM = Color.rgb(244, 226, 196)
    private val PATCH = Color.rgb(92, 58, 36)
    private val INK = Color.rgb(28, 24, 22)
    private val TONGUE = Color.rgb(226, 110, 120)
    private val COLLAR = Color.rgb(184, 44, 46)
    private val TAG = Color.rgb(226, 182, 58)
    private val TAG_RIM = Color.rgb(160, 120, 30)

    /** A fresh 480x595 bitmap decoded from the committed suspect JPEG. */
    fun suspectPhoto(): Bitmap = javaClass.classLoader!!.getResourceAsStream(SUSPECT_RESOURCE)!!.use {
        BitmapFactory.decodeStream(it)
    }

    /** Copies the committed suspect JPEG to [dir] so it can be handed over as a file:// URI. */
    fun suspectPhotoFile(dir: File): File = File(dir, "suspect.jpg").also { file ->
        javaClass.classLoader!!.getResourceAsStream(SUSPECT_RESOURCE)!!.use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
    }

    /**
     * The suspect artwork: a cartoon dog on an opaque grey studio backdrop, with fixed colours and
     * no fonts or randomness. Its features are deliberately lopsided (one floppy ear, an eye patch,
     * an off-centre collar tag) so a rotated or mirrored photo is easy to spot in a screenshot.
     */
    fun drawSuspect(): Bitmap = createBitmap(WIDTH, HEIGHT).also { drawSuspect(Canvas(it)) }

    private fun drawSuspect(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        fun fill(colour: Int) = paint.apply { style = Paint.Style.FILL; color = colour }
        fun stroke(colour: Int, width: Float) = paint.apply {
            style = Paint.Style.STROKE; color = colour; strokeWidth = width; strokeCap = Paint.Cap.ROUND
        }

        canvas.drawColor(BACKDROP)
        canvas.drawOval(RectF(70f, 60f, 410f, 470f), fill(SPOTLIGHT))

        // Shoulders and chest, with a cream bib and two spots on the suspect's right side only.
        canvas.drawOval(RectF(60f, 360f, 420f, 820f), fill(FUR))
        canvas.drawRect(180f, 300f, 300f, 420f, fill(FUR))
        canvas.drawOval(RectF(170f, 400f, 310f, 640f), fill(CREAM))
        canvas.drawCircle(350f, 430f, 20f, fill(PATCH))
        canvas.drawCircle(375f, 462f, 12f, fill(PATCH))

        // Collar, with the tag hanging well off-centre.
        canvas.drawArc(RectF(160f, 320f, 320f, 400f), 20f, 140f, false, stroke(COLLAR, 22f))
        canvas.drawLine(284f, 394f, 290f, 410f, stroke(TAG_RIM, 4f))
        canvas.drawCircle(292f, 426f, 19f, fill(TAG))
        canvas.drawCircle(292f, 426f, 19f, stroke(TAG_RIM, 3f))

        // Left ear (viewer's left) pricked up.
        canvas.drawPath(Path().apply {
            moveTo(128f, 200f); lineTo(118f, 60f); quadTo(170f, 90f, 205f, 140f); close()
        }, fill(FUR_SHADE))
        canvas.drawPath(Path().apply {
            moveTo(138f, 180f); lineTo(132f, 92f); quadTo(162f, 112f, 182f, 146f); close()
        }, fill(CREAM))

        // Head.
        canvas.drawOval(RectF(120f, 120f, 360f, 350f), fill(FUR))

        // Right ear flopped down over the side of the head.
        canvas.drawPath(Path().apply {
            moveTo(300f, 140f); quadTo(400f, 120f, 405f, 250f); quadTo(400f, 320f, 360f, 310f)
            quadTo(345f, 230f, 300f, 140f); close()
        }, fill(PATCH))

        // Patch over the suspect's left eye (viewer's left) and a cream muzzle.
        canvas.drawOval(RectF(155f, 185f, 235f, 265f), fill(PATCH))
        canvas.drawOval(RectF(172f, 255f, 308f, 350f), fill(CREAM))

        // Eyes, both glinting top-right; one eyebrow raised in suspicion.
        for (x in listOf(196f, 284f)) {
            canvas.drawCircle(x, 226f, 14f, fill(INK))
            canvas.drawCircle(x + 5f, 221f, 4.5f, fill(Color.WHITE))
        }
        canvas.drawLine(176f, 196f, 212f, 200f, stroke(INK, 5f))
        canvas.drawArc(RectF(262f, 178f, 306f, 210f), 200f, 130f, false, stroke(INK, 5f))

        // Nose, a lopsided smirk and a tongue poking out of one corner.
        canvas.drawPath(Path().apply {
            moveTo(218f, 272f); quadTo(240f, 262f, 262f, 272f); quadTo(258f, 294f, 240f, 298f)
            quadTo(222f, 294f, 218f, 272f); close()
        }, fill(INK))
        canvas.drawLine(240f, 298f, 240f, 314f, stroke(INK, 4f))
        canvas.drawOval(RectF(256f, 314f, 282f, 346f), fill(TONGUE))
        canvas.drawArc(RectF(204f, 296f, 240f, 326f), 30f, 120f, false, stroke(INK, 4f))
        canvas.drawArc(RectF(240f, 292f, 292f, 324f), 10f, 140f, false, stroke(INK, 4f))
    }

    /**
     * FileProvider caches its path roots in a static map keyed by authority, but Robolectric gives
     * each test a fresh cache dir. Clear the map so a stale root from an earlier test can't leak in.
     */
    fun resetFileProviderCache() {
        val field = FileProvider::class.java.getDeclaredField("sCache").apply { isAccessible = true }
        (field.get(null) as MutableMap<*, *>).clear()
    }

    fun booking(town: String? = null) = Booking.create(
        name = "Biscuit",
        species = Species.DOG,
        alias = "Sir Floofington",
        town = town,
        seed = 42L,
        bookedOn = LocalDate.of(2026, 10, 16),
    )
}
