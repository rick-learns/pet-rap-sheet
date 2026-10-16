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
class MugshotScreenshotTest {
    private val fonts = DocumentFonts.from(ApplicationProvider.getApplicationContext<android.content.Context>().assets)

    @Test
    fun mugshot() {
        MugshotDocument(Fixtures.booking(), Fixtures.suspectPhoto(), precinctCamera = false, fonts)
            .renderToBitmap()
            .captureRoboImage()
    }

    @Test
    fun mugshotPrecinctCameraWithTown() {
        MugshotDocument(Fixtures.booking(town = "Little Snoring"), Fixtures.suspectPhoto(), precinctCamera = true, fonts)
            .renderToBitmap()
            .captureRoboImage()
    }
}
