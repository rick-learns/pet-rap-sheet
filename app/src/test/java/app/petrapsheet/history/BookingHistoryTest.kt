package app.petrapsheet.history

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.Fixtures
import app.petrapsheet.booking.Booking
import app.petrapsheet.booking.Species
import app.petrapsheet.document.ChargeSheetDocument
import app.petrapsheet.document.CourtSummonsDocument
import app.petrapsheet.document.DocumentFonts
import app.petrapsheet.document.MugshotDocument
import app.petrapsheet.document.renderToBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class BookingHistoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var storageDir: File
    private lateinit var repository: BookingRepository
    private lateinit var fonts: DocumentFonts

    @Before
    fun setUp() {
        storageDir = tempFolder.newFolder("bookings")
        repository = BookingRepository(storageDir)
        val context = ApplicationProvider.getApplicationContext<Context>()
        fonts = DocumentFonts.from(context.assets)
    }

    /**
     * Acceptance criterion:
     * save -> list -> reopen -> identical render output (compare bitmap hashes or pixels) -> delete removes the files.
     */
    @Test
    fun saveListReopenIdenticalRenderAndDeleteRemovesFiles() {
        val originalPhoto = Fixtures.suspectPhoto()
        val seed = 42L
        val bookedOn = LocalDate.of(2026, 10, 16)
        val originalBooking = Booking.create(
            name = "Biscuit",
            species = Species.DOG,
            alias = "Sir Floofington",
            town = "Pawsbury",
            seed = seed,
            bookedOn = bookedOn,
            charges = emptyList(),
            arrestingOfficer = "Officer Friendly",
        )

        // 1. Initial renders from original booking inputs and photo
        val originalMugshot = MugshotDocument(originalBooking, originalPhoto, precinctCamera = false, fonts).renderToBitmap()
        val originalChargeSheet = ChargeSheetDocument(originalBooking, fonts).renderToBitmap()
        val originalSummons = CourtSummonsDocument(originalBooking, fonts).renderToBitmap()

        val mugshotHash = bitmapSha256(originalMugshot)
        val chargeSheetHash = bitmapSha256(originalChargeSheet)
        val summonsHash = bitmapSha256(originalSummons)

        // 2. Save to repository
        val record = BookingRecord(
            id = "booking-42",
            name = originalBooking.name,
            species = originalBooking.species,
            alias = originalBooking.alias,
            town = originalBooking.town,
            officer = originalBooking.arrestingOfficer,
            charges = originalBooking.charges,
            seed = seed,
            bookedOnEpochDay = bookedOn.toEpochDay(),
            precinctCamera = false,
            createdAt = 1000L,
        )
        repository.save(record, originalPhoto)

        // Verify files were saved to disk
        val bookingDir = File(storageDir, "booking-42")
        val jsonFile = File(bookingDir, "booking.json")
        val photoFile = File(bookingDir, "photo.png")
        assertTrue("booking.json should exist", jsonFile.exists())
        assertTrue("photo.png should exist", photoFile.exists())

        // 3. List
        val list = repository.list()
        assertEquals(1, list.size)
        val item = list[0]
        assertEquals("booking-42", item.record.id)
        assertEquals("Biscuit", item.record.name)
        assertEquals(bookedOn, item.record.bookedOn)
        assertNotNull("Thumbnail should load", item.loadThumbnail())

        // 4. Reopen
        val reopened = repository.get("booking-42")
        assertNotNull("Reopened entry should not be null", reopened)
        val (reopenedRecord, reopenedPhoto) = reopened!!
        val reopenedBooking = reopenedRecord.toBooking()

        // 5. Verify identical render output across all three documents
        val reopenedMugshot = MugshotDocument(reopenedBooking, reopenedPhoto, precinctCamera = false, fonts).renderToBitmap()
        val reopenedChargeSheet = ChargeSheetDocument(reopenedBooking, fonts).renderToBitmap()
        val reopenedSummons = CourtSummonsDocument(reopenedBooking, fonts).renderToBitmap()

        // Compare using both Bitmap.sameAs (pixel equality) and SHA-256 pixel hashes
        assertTrue("Mugshot pixels should be identical", originalMugshot.sameAs(reopenedMugshot))
        assertTrue("Charge sheet pixels should be identical", originalChargeSheet.sameAs(reopenedChargeSheet))
        assertTrue("Court summons pixels should be identical", originalSummons.sameAs(reopenedSummons))

        assertEquals("Mugshot SHA-256 hash must match", mugshotHash, bitmapSha256(reopenedMugshot))
        assertEquals("Charge sheet SHA-256 hash must match", chargeSheetHash, bitmapSha256(reopenedChargeSheet))
        assertEquals("Court summons SHA-256 hash must match", summonsHash, bitmapSha256(reopenedSummons))

        // 6. Delete removes the files
        val deleted = repository.delete("booking-42")
        assertTrue("delete should return true", deleted)
        assertFalse("photo.png should be removed", photoFile.exists())
        assertFalse("booking.json should be removed", jsonFile.exists())
        assertFalse("booking dir should be removed", bookingDir.exists())
        assertTrue("list should be empty after deletion", repository.list().isEmpty())
    }

    @Test
    fun listReturnsMostRecent20SortedDescending() {
        val photo = Fixtures.suspectPhoto()
        for (i in 1..25) {
            val record = BookingRecord(
                id = "booking-$i",
                name = "Pet $i",
                species = Species.DOG,
                alias = null,
                town = null,
                officer = null,
                charges = emptyList(),
                seed = i.toLong(),
                bookedOnEpochDay = LocalDate.of(2026, 1, 1).plusDays(i.toLong()).toEpochDay(),
                precinctCamera = false,
                createdAt = i * 1000L,
            )
            repository.save(record, photo)
        }

        val list = repository.list()
        assertEquals(20, list.size)
        // Most recent should be Pet 25, followed by Pet 24, ..., Pet 6
        assertEquals("Pet 25", list[0].record.name)
        assertEquals("Pet 6", list[19].record.name)
    }

    @Test
    fun togglePrecinctCameraPersistsAndReopenProducesFilteredMugshot() {
        val originalPhoto = Fixtures.suspectPhoto()
        val seed = 42L
        val bookedOn = LocalDate.of(2026, 10, 16)
        val originalBooking = Booking.create(
            name = "Biscuit",
            species = Species.DOG,
            alias = "Sir Floofington",
            town = "Pawsbury",
            seed = seed,
            bookedOn = bookedOn,
            charges = emptyList(),
            arrestingOfficer = "Officer Friendly",
        )

        // Initial save with precinctCamera = false
        val record = BookingRecord(
            id = "booking-precinct-toggle",
            name = originalBooking.name,
            species = originalBooking.species,
            alias = originalBooking.alias,
            town = originalBooking.town,
            officer = originalBooking.arrestingOfficer,
            charges = originalBooking.charges,
            seed = seed,
            bookedOnEpochDay = bookedOn.toEpochDay(),
            precinctCamera = false,
            createdAt = 1000L,
        )
        repository.save(record, originalPhoto)

        // Baseline renders
        val unfilteredMugshot = MugshotDocument(originalBooking, originalPhoto, precinctCamera = false, fonts).renderToBitmap()
        val expectedFilteredMugshot = MugshotDocument(originalBooking, originalPhoto, precinctCamera = true, fonts).renderToBitmap()
        val expectedFilteredHash = bitmapSha256(expectedFilteredMugshot)
        assertFalse("Filtered and unfiltered mugshots must differ", unfilteredMugshot.sameAs(expectedFilteredMugshot))

        // Toggle precinct-camera to true
        val updated = repository.updatePrecinctCamera("booking-precinct-toggle", true)
        assertTrue("updatePrecinctCamera should succeed", updated)

        // Reopen from repository
        val reopened = repository.get("booking-precinct-toggle")
        assertNotNull("Reopened entry should not be null", reopened)
        val (reopenedRecord, reopenedPhoto) = reopened!!
        assertTrue("reopenedRecord.precinctCamera must be true", reopenedRecord.precinctCamera)

        // Render mugshot with the reopened record's precinctCamera setting
        val reopenedBooking = reopenedRecord.toBooking()
        val reopenedMugshot = MugshotDocument(reopenedBooking, reopenedPhoto, reopenedRecord.precinctCamera, fonts).renderToBitmap()

        // Verify it matches the filtered render identically (pixel equality and SHA-256 hash)
        assertTrue("Reopened mugshot pixels must match expected filtered mugshot", expectedFilteredMugshot.sameAs(reopenedMugshot))
        assertEquals("Reopened mugshot hash must match expected filtered mugshot", expectedFilteredHash, bitmapSha256(reopenedMugshot))
        assertFalse("Reopened mugshot must not match unfiltered mugshot", unfilteredMugshot.sameAs(reopenedMugshot))

        // Toggle back to false and verify it persists and produces unfiltered
        val updatedBack = repository.updatePrecinctCamera("booking-precinct-toggle", false)
        assertTrue("updatePrecinctCamera should succeed", updatedBack)
        val reopenedBack = repository.get("booking-precinct-toggle")!!
        assertFalse("reopenedBack.precinctCamera must be false", reopenedBack.first.precinctCamera)
        val reopenedBackMugshot = MugshotDocument(reopenedBack.first.toBooking(), reopenedBack.second, reopenedBack.first.precinctCamera, fonts).renderToBitmap()
        assertTrue("Reopened back mugshot must match unfiltered mugshot", unfilteredMugshot.sameAs(reopenedBackMugshot))
    }

    private fun bitmapSha256(bitmap: Bitmap): String {
        val buffer = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(buffer)
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(buffer.array()).joinToString("") { "%02x".format(it) }
    }
}
