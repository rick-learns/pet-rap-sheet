package app.petrapsheet.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.Fixtures
import app.petrapsheet.booking.Charge
import app.petrapsheet.history.BookingEntry
import app.petrapsheet.history.BookingRecord
import java.io.File
import java.time.LocalDate
import app.petrapsheet.booking.Species
import app.petrapsheet.document.DocumentFonts
import app.petrapsheet.document.MugshotDocument
import app.petrapsheet.document.renderToBitmap
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziOptions.RecordOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class ScreensScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyForm() {
        composeRule.setContent {
            PetRapSheetTheme {
                Surface {
                    BookingFormScreen(
                        form = BookingForm(),
                        onFormChange = {},
                        photo = null,
                        photoUnreadable = false,
                        onPickPhoto = {},
                        onBook = {},
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun filledForm() {
        val photo = Fixtures.suspectPhoto().asImageBitmap()
        composeRule.setContent {
            PetRapSheetTheme {
                Surface {
                    BookingFormScreen(
                        form = BookingForm(name = "Biscuit", species = Species.DOG, alias = "Sir Floofington"),
                        onFormChange = {},
                        photo = photo,
                        photoUnreadable = false,
                        onPickPhoto = {},
                        onBook = {},
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun result() {
        val fonts = DocumentFonts.from(ApplicationProvider.getApplicationContext<android.content.Context>().assets)
        val booking = Fixtures.booking()
        val document = MugshotDocument(booking, Fixtures.suspectPhoto(), precinctCamera = false, fonts)
            .renderToBitmap()
            .asImageBitmap()
        composeRule.setContent {
            PetRapSheetTheme {
                Surface {
                    ResultScreen(
                        bookingNumber = booking.bookingNumber,
                        document = document,
                        precinctCamera = false,
                        onPrecinctCameraChange = {},
                        onShare = {},
                        onShareAll = {},
                        onSaveToGallery = {},
                        onSaveAllToGallery = {},
                        onNewBooking = {},
                    )
                }
            }
        }
        // Half scale keeps this photo-heavy reference PNG well under 1 MB.
        composeRule.onRoot().captureRoboImage(
            roborazziOptions = RoborazziOptions(recordOptions = RecordOptions(resizeScale = 0.5)),
        )
    }

    @Test
    fun about() {
        composeRule.setContent {
            PetRapSheetTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AboutScreen(onBack = {})
                }
            }
        }
        // Same guard as history(): wait for real content so the capture can't come out blank.
        composeRule.onNodeWithText("Nothing leaves your phone.").assertExists()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun history() {
        val cacheDir = ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir
        val photoFile = Fixtures.suspectPhotoFile(cacheDir)
        val record = BookingRecord(
            id = "test-screenshot-booking",
            name = "Biscuit",
            species = Species.DOG,
            alias = "Sir Floofington",
            town = null,
            officer = "Officer Friendly",
            charges = listOf(
                Charge("§ 14.7(b) Domestic Code", "Grand Theft Sock"),
                Charge("§ 9.2 Domestic Code", "Loitering on the Keyboard"),
            ),
            seed = 42L,
            bookedOnEpochDay = LocalDate.of(2026, 10, 16).toEpochDay(),
            precinctCamera = false,
        )
        val entry = BookingEntry(record = record, photoFile = photoFile, thumbnailFile = photoFile)

        composeRule.setContent {
            PetRapSheetTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HistoryScreen(
                        entries = listOf(entry),
                        onOpen = {},
                        onShare = {},
                        onDelete = {},
                        onBack = {},
                    )
                }
            }
        }
        // Late in the full suite the capture could beat the first frame and come out blank.
        composeRule.onNodeWithTag("history_item_${record.id}").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }
}
