package app.petrapsheet.history

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Metadata and file references for a booking entry in the history list.
 */
data class BookingEntry(
    val record: BookingRecord,
    val photoFile: File,
    val thumbnailFile: File,
) {
    fun loadThumbnail(): Bitmap? = BitmapFactory.decodeFile(thumbnailFile.absolutePath)
    fun loadPhoto(): Bitmap? = BitmapFactory.decodeFile(photoFile.absolutePath)
}

/**
 * Manages persisted bookings in app-private storage.
 * Each booking is stored in its own folder:
 * - booking.json: full record inputs
 * - photo.png: lossless downsampled photo
 * - thumb.png: fast-loading thumbnail for history list
 */
class BookingRepository(private val baseDir: File) {

    private val lock = Any()

    constructor(context: Context) : this(context.filesDir.resolve("bookings"))

    init {
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
    }

    fun save(record: BookingRecord, photo: Bitmap): BookingRecord = synchronized(lock) {
        val dir = File(baseDir, record.id)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        File(dir, "booking.json").writeText(record.toJson())
        File(dir, "photo.png").outputStream().use { out ->
            photo.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val thumb = createThumbnail(photo, 160)
        File(dir, "thumb.png").outputStream().use { out ->
            thumb.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        record
    }

    fun updatePrecinctCamera(id: String, enabled: Boolean): Boolean = synchronized(lock) {
        val dir = File(baseDir, id)
        val jsonFile = File(dir, "booking.json")
        if (!jsonFile.exists()) return false
        runCatching {
            val record = BookingRecord.fromJson(jsonFile.readText())
            val updated = record.copy(precinctCamera = enabled)
            jsonFile.writeText(updated.toJson())
            true
        }.getOrDefault(false)
    }

    fun list(): List<BookingEntry> = synchronized(lock) {
        if (!baseDir.exists()) return emptyList()
        val entries = baseDir.listFiles()?.mapNotNull { dir ->
            if (!dir.isDirectory) return@mapNotNull null
            val jsonFile = File(dir, "booking.json")
            val photoFile = File(dir, "photo.png")
            if (!jsonFile.exists() || !photoFile.exists()) return@mapNotNull null
            runCatching {
                val record = BookingRecord.fromJson(jsonFile.readText())
                val thumbFile = File(dir, "thumb.png").takeIf { it.exists() } ?: photoFile
                BookingEntry(record = record, photoFile = photoFile, thumbnailFile = thumbFile)
            }.getOrNull()
        } ?: emptyList()

        entries.sortedByDescending { it.record.createdAt }.take(20)
    }

    fun get(id: String): Pair<BookingRecord, Bitmap>? = synchronized(lock) {
        val dir = File(baseDir, id)
        val jsonFile = File(dir, "booking.json")
        val photoFile = File(dir, "photo.png")
        if (!jsonFile.exists() || !photoFile.exists()) return null
        runCatching {
            val record = BookingRecord.fromJson(jsonFile.readText())
            val photo = BitmapFactory.decodeFile(photoFile.absolutePath) ?: return null
            record to photo
        }.getOrNull()
    }

    fun delete(id: String): Boolean = synchronized(lock) {
        val dir = File(baseDir, id)
        dir.deleteRecursively()
    }

    private fun createThumbnail(source: Bitmap, maxDimension: Int): Bitmap {
        val width = source.width
        val height = source.height
        val maxSide = maxOf(width, height)
        if (maxSide <= maxDimension) return source
        val scale = maxDimension.toFloat() / maxSide
        val dstWidth = (width * scale).toInt().coerceAtLeast(1)
        val dstHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, dstWidth, dstHeight, true)
    }
}
