package app.petrapsheet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.booking.Charge
import app.petrapsheet.booking.Species
import app.petrapsheet.history.BookingEntry
import app.petrapsheet.history.BookingRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AboutAndHistoryFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun aboutScreenDisplaysRequiredStatements() {
        var backClicked = false
        composeRule.setContent {
            PetRapSheetTheme {
                AboutScreen(onBack = { backClicked = true })
            }
        }

        // 1. "Nothing leaves your phone."
        composeRule.onNodeWithText("Nothing leaves your phone.").assertExists()

        // 2. Parody statement and agency disclaimer
        composeRule.onNodeWithText("Parody & Disclaimer", substring = true).assertExists()

        // 3. Bundled fonts and licences
        composeRule.onNodeWithText("Courier Prime").assertExists()
        composeRule.onNodeWithText("Licensed under the SIL Open Font License, Version 1.1 (OFL).").assertExists()

        // 4. Back button
        composeRule.onNodeWithTag(TAG_ABOUT_BACK).performClick()
        assertTrue("onBack should have been clicked", backClicked)
    }

    @Test
    fun historyScreenDisplaysEntriesAndSupportsOpenShareDelete() {
        val dummyFile = File.createTempFile("photo", ".png").apply { deleteOnExit() }
        val record = BookingRecord(
            id = "test-123",
            name = "Barnaby",
            species = Species.CAT,
            alias = "The Claw",
            town = "Meowtown",
            officer = "Officer Purr",
            charges = listOf(Charge("§ 1.1", "Knocking over water glass")),
            seed = 999L,
            bookedOnEpochDay = LocalDate.of(2026, 10, 16).toEpochDay(),
            precinctCamera = false,
            createdAt = 5000L,
        )
        val entry = BookingEntry(record = record, photoFile = dummyFile, thumbnailFile = dummyFile)

        var openedRecord: BookingRecord? = null
        var sharedRecord: BookingRecord? = null
        var deletedId: String? = null
        var backClicked = false

        composeRule.setContent {
            PetRapSheetTheme {
                HistoryScreen(
                    entries = listOf(entry),
                    onOpen = { openedRecord = it },
                    onShare = { sharedRecord = it },
                    onDelete = { deletedId = it },
                    onBack = { backClicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Barnaby").assertIsDisplayed()
        composeRule.onNodeWithText("16 OCT 2026", substring = true).assertIsDisplayed()

        // Open
        composeRule.onNodeWithTag("${TAG_HISTORY_OPEN_PREFIX}test-123").performClick()
        assertEquals("test-123", openedRecord?.id)

        // Share
        composeRule.onNodeWithTag("${TAG_HISTORY_SHARE_PREFIX}test-123").performClick()
        assertEquals("test-123", sharedRecord?.id)

        // Delete
        composeRule.onNodeWithTag("${TAG_HISTORY_DELETE_PREFIX}test-123").performClick()
        assertEquals("test-123", deletedId)

        // Back
        composeRule.onNodeWithTag(TAG_HISTORY_BACK).performClick()
        assertTrue(backClicked)
    }
}
