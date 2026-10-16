package app.petrapsheet.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PhotoLoaderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val uri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/7")

    private fun serve(file: File) {
        shadowOf(context.contentResolver).registerInputStreamSupplier(uri) { file.inputStream() }
    }

    @Test
    fun loadsThePickedPhoto() {
        serve(Fixtures.suspectPhotoFile(context.cacheDir))
        val photo = PhotoLoader.load(context.contentResolver, uri)!!
        assertEquals(480, photo.width)
        assertEquals(595, photo.height)
    }

    @Test
    fun turnsSidewaysPhotosUpright() {
        val file = Fixtures.suspectPhotoFile(context.cacheDir)
        ExifInterface(file).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
            saveAttributes()
        }
        serve(file)
        val photo = PhotoLoader.load(context.contentResolver, uri)!!
        assertEquals(595, photo.width)
        assertEquals(480, photo.height)
    }

    /**
     * A 40x20 photo stored as four solid quadrants (red, green / blue, yellow), tagged with each
     * EXIF orientation in turn. The loaded bitmap must show the corners where a viewer would.
     */
    @Test
    fun appliesEveryExifOrientationIncludingMirrors() {
        // Expected upright corners: top-left, top-right, bottom-left.
        val expected = mapOf(
            ExifInterface.ORIENTATION_NORMAL to Triple(RED, GREEN, BLUE),
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL to Triple(GREEN, RED, YELLOW),
            ExifInterface.ORIENTATION_ROTATE_180 to Triple(YELLOW, BLUE, GREEN),
            ExifInterface.ORIENTATION_FLIP_VERTICAL to Triple(BLUE, YELLOW, RED),
            ExifInterface.ORIENTATION_TRANSPOSE to Triple(RED, BLUE, GREEN),
            ExifInterface.ORIENTATION_ROTATE_90 to Triple(BLUE, RED, YELLOW),
            ExifInterface.ORIENTATION_TRANSVERSE to Triple(YELLOW, GREEN, BLUE),
            ExifInterface.ORIENTATION_ROTATE_270 to Triple(GREEN, YELLOW, RED),
        )
        for ((orientation, corners) in expected) {
            val file = quadrantJpeg(orientation)
            serve(file)
            val photo = PhotoLoader.load(context.contentResolver, uri)!!
            val sideways = orientation >= ExifInterface.ORIENTATION_TRANSPOSE
            assertEquals("width for orientation $orientation", if (sideways) 20 else 40, photo.width)
            assertEquals("height for orientation $orientation", if (sideways) 40 else 20, photo.height)
            val (w, h) = photo.width to photo.height
            assertEquals("top-left for orientation $orientation", corners.first, nearest(photo.getPixel(w / 4, h / 4)))
            assertEquals("top-right for orientation $orientation", corners.second, nearest(photo.getPixel(3 * w / 4, h / 4)))
            assertEquals("bottom-left for orientation $orientation", corners.third, nearest(photo.getPixel(w / 4, 3 * h / 4)))
        }
    }

    @Test
    fun returnsNullForSomethingThatIsNotAnImage() {
        val file = File(context.cacheDir, "notes.txt").apply { writeText("definitely not a dog") }
        serve(file)
        assertNull(PhotoLoader.load(context.contentResolver, uri))
    }

    private fun quadrantJpeg(orientation: Int): File {
        val bitmap = createBitmap(40, 20)
        Canvas(bitmap).apply {
            val paint = Paint()
            for ((colour, rect) in listOf(
                RED to Rect(0, 0, 20, 10), GREEN to Rect(20, 0, 40, 10),
                BLUE to Rect(0, 10, 20, 20), YELLOW to Rect(20, 10, 40, 20),
            )) {
                paint.color = colour
                drawRect(rect, paint)
            }
        }
        val file = File(context.cacheDir, "quadrants-$orientation.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        ExifInterface(file).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            saveAttributes()
        }
        return file
    }

    /** Snaps a decoded (JPEG-blurred) pixel to the closest quadrant colour. */
    private fun nearest(pixel: Int) = listOf(RED, GREEN, BLUE, YELLOW).minBy { colour ->
        listOf(Color::red, Color::green, Color::blue).sumOf { channel -> abs(channel(pixel) - channel(colour)) }
    }

    private companion object {
        val RED = Color.rgb(220, 30, 30)
        val GREEN = Color.rgb(30, 200, 30)
        val BLUE = Color.rgb(30, 30, 220)
        val YELLOW = Color.rgb(230, 220, 30)
    }
}
