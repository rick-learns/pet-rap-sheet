package app.petrapsheet.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.content.IntentCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.Fixtures
import app.petrapsheet.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowToast

/** The core loop through the real activity: pick a photo, fill the form, render, share. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class BookingFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() = Fixtures.resetFileProviderCache()

    @Test
    fun pickBookAndShare() {
        val activity = composeRule.activity
        val book = composeRule.onNodeWithText("BOOK 'EM")
        book.performScrollTo().assertIsNotEnabled()

        composeRule.onNodeWithTag(TAG_PICK_PHOTO).performClick()
        composeRule.waitForIdle()
        val pick = shadowOf(activity).nextStartedActivityForResult
        assertEquals(MediaStore.ACTION_PICK_IMAGES, pick.intent.action)

        // What the Photo Picker hands back: a content:// URI the app may read once.
        val photo = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/1000")
        val photoFile = Fixtures.suspectPhotoFile(activity.cacheDir)
        shadowOf(activity.contentResolver).registerInputStreamSupplier(photo) { photoFile.inputStream() }
        shadowOf(activity).receiveResult(pick.intent, Activity.RESULT_OK, Intent().setData(photo))
        composeRule.onNodeWithTag(TAG_NAME).performTextInput("Biscuit")
        composeRule.waitUntil(5_000) {
            runCatching { book.assertIsEnabled() }.isSuccess
        }
        book.performScrollTo().performClick()

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTestTag(TAG_DOCUMENT)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("SHARE MUGSHOT").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            shadowOf(activity).peekNextStartedActivity()?.action == Intent.ACTION_CHOOSER
        }

        val chooser = shadowOf(activity).nextStartedActivity
        val send = IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("image/png", send.type)
        val shared = IntentCompat.getParcelableExtra(send, Intent.EXTRA_STREAM, Uri::class.java)!!
        assertEquals("content", shared.scheme)
    }

    @Test
    fun shareAllSendsActionSendMultipleWithThreeUris() {
        val activity = composeRule.activity
        completeBooking()

        composeRule.onNodeWithText("SHARE ALL").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            shadowOf(activity).peekNextStartedActivity()?.action == Intent.ACTION_CHOOSER
        }

        val chooser = shadowOf(activity).nextStartedActivity
        val send = IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals(Intent.ACTION_SEND_MULTIPLE, send.action)
        assertEquals("image/png", send.type)
        val uris = IntentCompat.getParcelableArrayListExtra(send, Intent.EXTRA_STREAM, Uri::class.java)!!
        assertEquals(3, uris.size)
        for (uri in uris) {
            assertEquals("content", uri.scheme)
            assertEquals("app.petrapsheet.fileprovider", uri.authority)
        }
    }

    @Test
    fun canSwitchDocumentsAndShareSingle() {
        val activity = composeRule.activity
        completeBooking()

        composeRule.onNodeWithTag("tab_charge_sheet").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("SHARE CHARGE SHEET").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            shadowOf(activity).peekNextStartedActivity()?.action == Intent.ACTION_CHOOSER
        }
        val chooser = shadowOf(activity).nextStartedActivity
        val send = IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals(Intent.ACTION_SEND, send.action)

        composeRule.onNodeWithTag("tab_summons").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("SHARE COURT SUMMONS").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            shadowOf(activity).peekNextStartedActivity()?.action == Intent.ACTION_CHOOSER
        }
        val chooserSummons = shadowOf(activity).nextStartedActivity
        val sendSummons = IntentCompat.getParcelableExtra(chooserSummons, Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals(Intent.ACTION_SEND, sendSummons.action)
    }

    @Test
    fun customChargeAndRandomCharges() {
        composeRule.onNodeWithTag(TAG_CUSTOM_CHARGE).performTextInput("Chewing on TV Remote")
        composeRule.onNodeWithTag(TAG_ADD_CUSTOM_CHARGE).performClick()
        composeRule.onNodeWithText("Chewing on TV Remote").performScrollTo().assertExists()

        composeRule.onNodeWithTag(TAG_RANDOM_CHARGES).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun saveToGalleryShowsSuccessToast() {
        completeBooking()
        composeRule.onNodeWithText("SAVE TO GALLERY").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            ShadowToast.getTextOfLatestToast() != null
        }
        assertEquals("Saved to Pictures/Pet Rap Sheet", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun saveAllToGalleryShowsSuccessToast() {
        completeBooking()
        composeRule.onNodeWithText("SAVE ALL TO GALLERY").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            ShadowToast.getTextOfLatestToast() == "Saved 3 documents to Pictures/Pet Rap Sheet"
        }
        assertEquals("Saved 3 documents to Pictures/Pet Rap Sheet", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun saveToGalleryFailureShowsErrorToast() {
        val activity = composeRule.activity
        completeBooking()

        val failingProvider = object : android.content.ContentProvider() {
            init {
                val info = android.content.pm.ProviderInfo().apply { authority = MediaStore.AUTHORITY }
                attachInfo(activity, info)
            }
            override fun onCreate(): Boolean = true
            override fun insert(uri: Uri, values: android.content.ContentValues?): Uri? = null
            override fun query(uri: Uri, p: Array<out String>?, s: String?, sa: Array<out String>?, o: String?): android.database.Cursor? = null
            override fun update(uri: Uri, v: android.content.ContentValues?, s: String?, sa: Array<out String>?): Int = 0
            override fun delete(uri: Uri, s: String?, sa: Array<out String>?): Int = 0
            override fun getType(uri: Uri): String? = null
        }
        org.robolectric.shadows.ShadowContentResolver.registerProviderInternal(MediaStore.AUTHORITY, failingProvider)

        composeRule.onNodeWithText("SAVE TO GALLERY").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            ShadowToast.getTextOfLatestToast() == "Could not save to gallery. Check device storage."
        }
        assertEquals("Could not save to gallery. Check device storage.", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun saveAllToGalleryFailureShowsErrorToast() {
        val activity = composeRule.activity
        completeBooking()

        val failingProvider = object : android.content.ContentProvider() {
            init {
                val info = android.content.pm.ProviderInfo().apply { authority = MediaStore.AUTHORITY }
                attachInfo(activity, info)
            }
            override fun onCreate(): Boolean = true
            override fun insert(uri: Uri, values: android.content.ContentValues?): Uri? = null
            override fun query(uri: Uri, p: Array<out String>?, s: String?, sa: Array<out String>?, o: String?): android.database.Cursor? = null
            override fun update(uri: Uri, v: android.content.ContentValues?, s: String?, sa: Array<out String>?): Int = 0
            override fun delete(uri: Uri, s: String?, sa: Array<out String>?): Int = 0
            override fun getType(uri: Uri): String? = null
        }
        org.robolectric.shadows.ShadowContentResolver.registerProviderInternal(MediaStore.AUTHORITY, failingProvider)

        composeRule.onNodeWithText("SAVE ALL TO GALLERY").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            ShadowToast.getTextOfLatestToast() == "Could not save pack to gallery. Check device storage."
        }
        assertEquals("Could not save pack to gallery. Check device storage.", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun navigateToAboutAndBack() {
        composeRule.onNodeWithTag(TAG_OPEN_ABOUT).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Nothing leaves your phone.").assertExists()

        composeRule.onNodeWithTag(TAG_ABOUT_BACK).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("PET RAP SHEET").assertExists()
    }

    @Test
    fun navigateToHistoryAndBack() {
        composeRule.onNodeWithTag(TAG_OPEN_HISTORY).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("BOOKING HISTORY").assertExists()

        composeRule.onNodeWithTag(TAG_HISTORY_BACK).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("PET RAP SHEET").assertExists()
    }

    @Test
    fun bookingAppearsInHistoryAndCanBeReopened() {
        completeBooking(name = "Sir Winston")

        // From Result screen, navigate to History
        composeRule.onNodeWithTag(TAG_RESULT_HISTORY).performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("BOOKING HISTORY").assertExists()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Sir Winston")).fetchSemanticsNodes().isNotEmpty()
        }

        // Reopen from History
        composeRule.onNodeWithText("Open").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTestTag(TAG_DOCUMENT)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun togglePrecinctCameraOnResultScreenPersistsToHistoryAndReopenMaintainsFilter() {
        completeBooking(name = "Barnaby")

        // Initially precinct camera switch is off
        composeRule.onNodeWithTag(TAG_PRECINCT_CAMERA).performScrollTo().assertIsOff()

        // Toggle precinct camera switch on
        composeRule.onNodeWithTag(TAG_PRECINCT_CAMERA).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TAG_PRECINCT_CAMERA).assertIsOn()

        // Navigate to History from Result screen
        composeRule.onNodeWithTag(TAG_RESULT_HISTORY).performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("BOOKING HISTORY").assertExists()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Barnaby")).fetchSemanticsNodes().isNotEmpty()
        }

        // Reopen booking from History
        composeRule.onNodeWithText("Open").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTestTag(TAG_DOCUMENT)).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify the reopened screen has precinct camera switch on
        composeRule.onNodeWithTag(TAG_PRECINCT_CAMERA).performScrollTo().assertIsOn()
    }

    private fun completeBooking(name: String = "Biscuit", officer: String? = null) {
        val activity = composeRule.activity
        composeRule.onNodeWithTag(TAG_PICK_PHOTO).performClick()
        composeRule.waitForIdle()
        val pick = shadowOf(activity).nextStartedActivityForResult
        val photo = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/1000")
        val photoFile = Fixtures.suspectPhotoFile(activity.cacheDir)
        shadowOf(activity.contentResolver).registerInputStreamSupplier(photo) { photoFile.inputStream() }
        shadowOf(activity).receiveResult(pick.intent, Activity.RESULT_OK, Intent().setData(photo))
        composeRule.onNodeWithTag(TAG_NAME).performTextInput(name)
        if (officer != null) {
            composeRule.onNodeWithTag(TAG_OFFICER).performTextInput(officer)
        }
        val book = composeRule.onNodeWithText("BOOK 'EM")
        composeRule.waitUntil(5_000) { runCatching { book.assertIsEnabled() }.isSuccess }
        book.performScrollTo().performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTestTag(TAG_DOCUMENT)).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
