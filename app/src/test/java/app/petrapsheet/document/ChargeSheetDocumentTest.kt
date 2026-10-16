package app.petrapsheet.document

import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.Fixtures
import app.petrapsheet.booking.Booking
import app.petrapsheet.booking.Charge
import app.petrapsheet.booking.Species
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChargeSheetDocumentTest {
    private val fonts = DocumentFonts.from(ApplicationProvider.getApplicationContext<android.content.Context>().assets)

    private fun render(booking: Booking = Fixtures.booking(), size: DocumentSize = DocumentSize.PORTRAIT) =
        ChargeSheetDocument(booking, fonts).renderToBitmap(size)

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
    fun slugIsChargeSheet() {
        val doc = ChargeSheetDocument(Fixtures.booking(), fonts)
        assertEquals("charge-sheet", doc.slug)
    }

    @Test
    fun rendersWithVaryingChargeCounts() {
        for (count in 2..4) {
            val charges = (1..count).map {
                Charge("§ $it.0 Domestic Code", "Violation Number $it of House Rules")
            }
            val booking = Booking.create(
                name = "Sir Paws",
                species = Species.CAT,
                alias = "The Menace",
                town = "Household",
                seed = count.toLong(),
                bookedOn = LocalDate.of(2026, 10, 16),
                charges = charges,
                arrestingOfficer = "Officer Friendly",
            )
            val bitmap = render(booking)
            assertEquals(1080, bitmap.width)
            assertEquals(1350, bitmap.height)
        }
    }
}
