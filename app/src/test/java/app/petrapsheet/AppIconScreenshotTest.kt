package app.petrapsheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.ui.PetRapSheetTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AppIconScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun launcherIcon() {
        composeRule.setContent {
            PetRapSheetTheme {
                Surface {
                    Box(modifier = Modifier.size(108.dp).clip(CircleShape)) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_background),
                            contentDescription = "Background",
                            modifier = Modifier.size(108.dp),
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = "Foreground",
                            modifier = Modifier.size(108.dp),
                        )
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
