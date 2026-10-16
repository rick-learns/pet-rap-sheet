package app.petrapsheet.document

import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.Fixtures
import app.petrapsheet.booking.Booking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CourtSummonsDocumentTest {
    private val fonts = DocumentFonts.from(ApplicationProvider.getApplicationContext<android.content.Context>().assets)

    private fun render(booking: Booking = Fixtures.booking(), size: DocumentSize = DocumentSize.PORTRAIT) =
        CourtSummonsDocument(booking, fonts).renderToBitmap(size)

    @Test
    fun rendersAPortrait1080x1350Bitmap() {
        val bitmap = render()
        assertEquals(1080, bitmap.width)
        assertEquals(1350, bitmap.height)
    }

    @Test
    fun renderingIsDeterministic() {
        assertTrue(render().sameAs(render()))
    }

    @Test
    fun scalesToTheRequestedSize() {
        val small = render(size = DocumentSize(540, 675))
        assertEquals(540, small.width)
        assertEquals(675, small.height)
        assertEquals(255, Color.alpha(small.getPixel(539, 674)))
    }

    @Test
    fun slugIsSummons() {
        val doc = CourtSummonsDocument(Fixtures.booking(), fonts)
        assertEquals("summons", doc.slug)
    }
}
