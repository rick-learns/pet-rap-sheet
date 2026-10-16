package app.petrapsheet.document

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.Fixtures
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class ChargeSheetScreenshotTest {
    private val fonts = DocumentFonts.from(ApplicationProvider.getApplicationContext<android.content.Context>().assets)

    @Test
    fun chargeSheet() {
        ChargeSheetDocument(Fixtures.booking(), fonts)
            .renderToBitmap()
            .captureRoboImage()
    }
}
