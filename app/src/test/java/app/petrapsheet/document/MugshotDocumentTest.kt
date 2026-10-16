package app.petrapsheet.document

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MugshotDocumentTest {
    private val fonts = DocumentFonts.from(ApplicationProvider.getApplicationContext<android.content.Context>().assets)
    private val photo = Fixtures.suspectPhoto()

    private fun render(precinctCamera: Boolean, size: DocumentSize = DocumentSize.PORTRAIT) =
        MugshotDocument(Fixtures.booking(), photo, precinctCamera, fonts).renderToBitmap(size)

    @Test
    fun rendersAPortrait1080x1350Bitmap() {
        val bitmap = render(precinctCamera = false)
        assertEquals(1080, bitmap.width)
        assertEquals(1350, bitmap.height)
    }

    @Test
    fun renderingIsDeterministic() {
        assertTrue(render(precinctCamera = false).sameAs(render(precinctCamera = false)))
    }

    @Test
    fun precinctCameraTurnsThePhotoGrayscale() {
        val colour = render(precinctCamera = false)
        val precinct = render(precinctCamera = true)
        assertFalse("precinct camera did not change the output", colour.sameAs(precinct))

        val region = MugshotDocument.PHOTO
        // Sample the photo below the camera caption and above the placard.
        val samples = (0 until 400).map { i ->
            val x = region.left.toInt() + 20 + (i % 20) * ((region.width().toInt() - 40) / 20)
            val y = region.top.toInt() + 80 + (i / 20) * ((MugshotDocument.PLACARD.top - region.top - 100).toInt() / 20)
            x to y
        }
        assertTrue("colour photo should have saturated pixels", samples.any { (x, y) -> saturation(colour, x, y) > 30 })
        for ((x, y) in samples) {
            assertTrue("pixel ($x, $y) is not gray", saturation(precinct, x, y) <= 2)
        }
    }

    @Test
    fun scalesToTheRequestedSize() {
        val small = render(precinctCamera = false, size = DocumentSize(540, 675))
        assertEquals(540, small.width)
        assertEquals(675, small.height)
        // The paper fills the corner, so nothing is left transparent.
        assertEquals(255, Color.alpha(small.getPixel(539, 674)))
    }

    @Test
    fun documentLayerHasNoComposeDependency() {
        val dir = File("src/main/java/app/petrapsheet/document")
        assertTrue("run from the app module: ${dir.absolutePath}", dir.isDirectory)
        for (file in dir.listFiles()!!.filter { it.extension == "kt" }) {
            assertFalse("${file.name} imports Compose", file.readText().contains("import androidx.compose"))
        }
    }

    private fun saturation(bitmap: Bitmap, x: Int, y: Int): Int {
        val pixel = bitmap.getPixel(x, y)
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        return maxOf(abs(r - g), abs(g - b), abs(r - b))
    }
}
