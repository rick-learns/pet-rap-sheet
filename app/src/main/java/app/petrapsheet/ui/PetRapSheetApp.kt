package app.petrapsheet.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import app.petrapsheet.R
import app.petrapsheet.booking.Booking
import app.petrapsheet.booking.Charge
import app.petrapsheet.booking.ChargeBank
import app.petrapsheet.booking.Species
import app.petrapsheet.document.ChargeSheetDocument
import app.petrapsheet.document.CourtSummonsDocument
import app.petrapsheet.document.DocumentFonts
import app.petrapsheet.document.MugshotDocument
import app.petrapsheet.document.renderToBitmap
import app.petrapsheet.history.BookingEntry
import app.petrapsheet.history.BookingRecord
import app.petrapsheet.history.BookingRepository
import app.petrapsheet.photo.PhotoLoader
import app.petrapsheet.share.DocumentShare
import app.petrapsheet.share.MediaStoreSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

/** Everything the booking desk needs to survive a configuration change. */
data class BookingDesk(
    val form: BookingForm = BookingForm(),
    val photoUri: Uri? = null,
    val bookingSeed: Long? = null,
    val bookedOnEpochDay: Long = 0,
    val precinctCamera: Boolean = false,
    val selectedDocumentType: DocumentType = DocumentType.MUGSHOT,
) {
    val booking: Booking?
        get() = bookingSeed?.let { seed ->
            Booking.create(
                name = form.name,
                species = form.species,
                alias = form.alias,
                town = form.town,
                seed = seed,
                bookedOn = LocalDate.ofEpochDay(bookedOnEpochDay),
                charges = form.charges,
                arrestingOfficer = form.officer,
            )
        }

    fun book(seed: Long, today: LocalDate) = copy(bookingSeed = seed, bookedOnEpochDay = today.toEpochDay())

    companion object {
        val Saver = listSaver<BookingDesk, Any?>(
            save = {
                listOf(
                    it.form.name,
                    it.form.species.name,
                    it.form.alias,
                    it.form.town,
                    it.form.officer,
                    it.form.charges.flatMap { c -> listOf(c.statute, c.description) },
                    it.photoUri,
                    it.bookingSeed,
                    it.bookedOnEpochDay,
                    it.precinctCamera,
                    it.selectedDocumentType.name,
                )
            },
            restore = {
                val species = Species.valueOf(it[1] as String)
                val rawCharges = (it[5] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val charges = rawCharges.chunked(2).mapNotNull { pair ->
                    if (pair.size == 2) Charge(pair[0], pair[1]) else null
                }.ifEmpty { ChargeBank.defaultCharges(species) }

                BookingDesk(
                    form = BookingForm(
                        name = it[0] as String,
                        species = species,
                        alias = it[2] as String,
                        town = it[3] as String,
                        officer = (it[4] as? String) ?: "",
                        charges = charges,
                    ),
                    photoUri = it[6] as Uri?,
                    bookingSeed = it[7] as Long?,
                    bookedOnEpochDay = it[8] as Long,
                    precinctCamera = it[9] as Boolean,
                    selectedDocumentType = DocumentType.valueOf((it[10] as? String) ?: DocumentType.MUGSHOT.name),
                )
            },
        )
    }
}

private sealed interface PhotoState {
    data object Empty : PhotoState
    data class Loaded(val bitmap: Bitmap) : PhotoState
    data object Unreadable : PhotoState
}

@Composable
fun PetRapSheetApp() {
    PetRapSheetTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BookingDeskFlow()
        }
    }
}

@Composable
private fun BookingDeskFlow() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { BookingRepository(context) }
    var activeScreen by rememberSaveable { mutableStateOf("DESK") }
    var activeBookingId by rememberSaveable { mutableStateOf<String?>(null) }
    var desk by rememberSaveable(stateSaver = BookingDesk.Saver) { mutableStateOf(BookingDesk()) }

    if (activeScreen == "ABOUT") {
        BackHandler { activeScreen = "DESK" }
        AboutScreen(onBack = { activeScreen = "DESK" })
        return
    }

    if (activeScreen == "HISTORY") {
        BackHandler { activeScreen = "DESK" }
        var historyEntries by remember { mutableStateOf(emptyList<BookingEntry>()) }
        var historyVersion by remember { mutableIntStateOf(0) }
        LaunchedEffect(historyVersion) {
            historyEntries = withContext(Dispatchers.IO) { repository.list() }
        }
        val fonts = remember { DocumentFonts.from(context.assets) }

        HistoryScreen(
            entries = historyEntries,
            onOpen = { record ->
                val reopened = repository.get(record.id)
                if (reopened != null) {
                    val (rec, _) = reopened
                    activeBookingId = rec.id
                    val photoFile = File(context.filesDir.resolve("bookings"), "${rec.id}/photo.png")
                    desk = BookingDesk(
                        form = BookingForm(
                            name = rec.name,
                            species = rec.species,
                            alias = rec.alias ?: "",
                            town = rec.town ?: "",
                            officer = rec.officer ?: "",
                            charges = rec.charges,
                        ),
                        photoUri = Uri.fromFile(photoFile),
                        bookingSeed = rec.seed,
                        bookedOnEpochDay = rec.bookedOnEpochDay,
                        precinctCamera = rec.precinctCamera,
                        selectedDocumentType = DocumentType.MUGSHOT,
                    )
                    activeScreen = "DESK"
                }
            },
            onShare = { record ->
                val reopened = repository.get(record.id)
                if (reopened != null) {
                    val (rec, photo) = reopened
                    scope.launch {
                        val b = rec.toBooking()
                        val mugshot = withContext(Dispatchers.Default) {
                            MugshotDocument(b, photo, rec.precinctCamera, fonts).renderToBitmap()
                        }
                        val name = "mugshot-${b.bookingNumber}"
                        share(context, mugshot, name)
                    }
                }
            },
            onDelete = { id ->
                if (activeBookingId == id) {
                    activeBookingId = null
                }
                scope.launch(Dispatchers.IO) {
                    repository.delete(id)
                    historyVersion++
                }
            },
            onBack = { activeScreen = "DESK" },
        )
        return
    }

    val pickPhoto = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) desk = desk.copy(photoUri = uri)
    }

    val photoUri = desk.photoUri
    val photo by produceState<PhotoState>(PhotoState.Empty, photoUri) {
        value = PhotoState.Empty
        if (photoUri == null) return@produceState
        val bitmap = withContext(Dispatchers.IO) {
            runCatching { PhotoLoader.load(context.contentResolver, photoUri) }.getOrNull()
        }
        value = bitmap?.let { PhotoState.Loaded(it) } ?: PhotoState.Unreadable
    }
    val photoBitmap = (photo as? PhotoState.Loaded)?.bitmap
    val photoImage = remember(photoBitmap) { photoBitmap?.asImageBitmap() }

    val booking = desk.booking
    if (booking == null || photoUri == null || photo == PhotoState.Unreadable) {
        BookingFormScreen(
            form = desk.form,
            onFormChange = { desk = desk.copy(form = it) },
            photo = photoImage,
            photoUnreadable = photo == PhotoState.Unreadable,
            onPickPhoto = { pickPhoto.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
            onBook = {
                val seed = Random.nextLong()
                val today = LocalDate.now()
                val newDesk = desk.book(seed = seed, today = today)
                desk = newDesk
                val b = newDesk.booking
                if (photoBitmap != null && b != null) {
                    val newId = UUID.randomUUID().toString()
                    activeBookingId = newId
                    val record = BookingRecord(
                        id = newId,
                        name = b.name,
                        species = b.species,
                        alias = b.alias,
                        town = b.town,
                        officer = b.arrestingOfficer,
                        charges = b.charges,
                        seed = seed,
                        bookedOnEpochDay = today.toEpochDay(),
                        precinctCamera = desk.precinctCamera,
                        createdAt = System.currentTimeMillis(),
                    )
                    scope.launch(Dispatchers.IO) {
                        repository.save(record, photoBitmap)
                    }
                }
            },
            onOpenHistory = { activeScreen = "HISTORY" },
            onOpenAbout = { activeScreen = "ABOUT" },
        )
        return
    }

    BackHandler {
        activeBookingId = null
        desk = desk.copy(bookingSeed = null)
    }
    val precinctCamera = desk.precinctCamera
    val fonts = remember { DocumentFonts.from(context.assets) }

    val renderedMugshot by produceState<Bitmap?>(null, booking, photoBitmap, precinctCamera) {
        if (photoBitmap == null) return@produceState
        value = withContext(Dispatchers.Default) {
            MugshotDocument(booking, photoBitmap, precinctCamera, fonts).renderToBitmap()
        }
    }

    val renderedChargeSheet by produceState<Bitmap?>(null, booking) {
        value = withContext(Dispatchers.Default) {
            ChargeSheetDocument(booking, fonts).renderToBitmap()
        }
    }

    val renderedSummons by produceState<Bitmap?>(null, booking) {
        value = withContext(Dispatchers.Default) {
            CourtSummonsDocument(booking, fonts).renderToBitmap()
        }
    }

    val currentBitmap = when (desk.selectedDocumentType) {
        DocumentType.MUGSHOT -> renderedMugshot
        DocumentType.CHARGE_SHEET -> renderedChargeSheet
        DocumentType.SUMMONS -> renderedSummons
    }
    val renderedImage = remember(currentBitmap) { currentBitmap?.asImageBitmap() }

    val allRendered = renderedMugshot != null && renderedChargeSheet != null && renderedSummons != null

    ResultScreen(
        bookingNumber = booking.bookingNumber,
        currentType = desk.selectedDocumentType,
        onTypeChange = { desk = desk.copy(selectedDocumentType = it) },
        document = renderedImage,
        precinctCamera = precinctCamera,
        onPrecinctCameraChange = { enabled ->
            desk = desk.copy(precinctCamera = enabled)
            activeBookingId?.let { id ->
                scope.launch(Dispatchers.IO) {
                    repository.updatePrecinctCamera(id, enabled)
                }
            }
        },
        onShare = {
            val bitmap = currentBitmap ?: return@ResultScreen
            val name = "${desk.selectedDocumentType.name.lowercase().replace('_', '-')}-${booking.bookingNumber}"
            scope.launch { share(context, bitmap, name) }
        },
        onShareAll = {
            if (!allRendered) return@ResultScreen
            val items = listOf(
                "mugshot-${booking.bookingNumber}" to renderedMugshot!!,
                "charge-sheet-${booking.bookingNumber}" to renderedChargeSheet!!,
                "summons-${booking.bookingNumber}" to renderedSummons!!,
            )
            scope.launch { shareAll(context, items) }
        },
        canShareAll = allRendered,
        onSaveToGallery = if (MediaStoreSaver.isSupported) {
            {
                val bitmap = currentBitmap ?: return@ResultScreen
                val name = "${desk.selectedDocumentType.name.lowercase().replace('_', '-')}-${booking.bookingNumber}"
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching { MediaStoreSaver.saveToGallery(context, name, bitmap) }
                    }
                    if (result.isSuccess) {
                        Toast.makeText(context, R.string.result_saved_single, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.result_save_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else null,
        onSaveAllToGallery = if (MediaStoreSaver.isSupported) {
            {
                if (!allRendered) return@ResultScreen
                val items = listOf(
                    "mugshot-${booking.bookingNumber}" to renderedMugshot!!,
                    "charge-sheet-${booking.bookingNumber}" to renderedChargeSheet!!,
                    "summons-${booking.bookingNumber}" to renderedSummons!!,
                )
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching { MediaStoreSaver.saveAllToGallery(context, items) }
                    }
                    if (result.isSuccess) {
                        Toast.makeText(context, R.string.result_saved_all, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.result_save_all_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else null,
        canSaveAll = allRendered,
        onNewBooking = {
            activeBookingId = null
            desk = BookingDesk()
        },
        onOpenHistory = { activeScreen = "HISTORY" },
    )
}

private suspend fun share(context: Context, bitmap: Bitmap, name: String) {
    val file = withContext(Dispatchers.IO) { DocumentShare.exportPng(context, bitmap, name) }
    context.startActivity(DocumentShare.chooser(context, file, context.getString(R.string.share_title)))
}

private suspend fun shareAll(context: Context, items: List<Pair<String, Bitmap>>) {
    val files = withContext(Dispatchers.IO) { DocumentShare.exportPngs(context, items) }
    context.startActivity(DocumentShare.chooserMultiple(context, files, context.getString(R.string.share_title_all)))
}
