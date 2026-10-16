package app.petrapsheet.share

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.IOException

/**
 * Saves rendered booking documents into the shared pictures gallery using MediaStore.
 *
 * Privacy & Scoped Storage:
 * On API 29+ (Android 10+), this uses [MediaStore.Images.Media.RELATIVE_PATH] pointed at
 * "Pictures/Pet Rap Sheet" and sets IS_PENDING during write. No storage permissions are needed
 * or requested.
 *
 * On API 26–28, saving to shared storage requires WRITE_EXTERNAL_STORAGE runtime permission.
 * In accordance with privacy rules (no storage permission requested), saving is disabled and
 * the button is hidden on API 26–28.
 */
object MediaStoreSaver {
    const val RELATIVE_PATH = "Pictures/Pet Rap Sheet"

    /** True if MediaStore scoped write is supported without storage permissions (API 29+). */
    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /**
     * Saves [bitmap] as [name].png to MediaStore into Pictures/Pet Rap Sheet.
     */
    fun saveToGallery(context: Context, name: String, bitmap: Bitmap): Uri {
        check(isSupported) { "Saving to MediaStore without storage permission requires API 29+" }
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, RELATIVE_PATH)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Failed to create MediaStore entry for $name")

        try {
            resolver.openOutputStream(uri)?.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    throw IOException("Failed to compress PNG for $name")
                }
            } ?: throw IOException("Failed to open output stream for $uri")

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    /**
     * Saves all [items] to MediaStore into Pictures/Pet Rap Sheet.
     * If any item fails, all previously saved documents in the batch are deleted to avoid
     * leaving a partially saved pack.
     */
    fun saveAllToGallery(context: Context, items: List<Pair<String, Bitmap>>): List<Uri> {
        val savedUris = mutableListOf<Uri>()
        try {
            for ((name, bitmap) in items) {
                savedUris.add(saveToGallery(context, name, bitmap))
            }
            return savedUris
        } catch (e: Exception) {
            val resolver = context.contentResolver
            for (uri in savedUris) {
                runCatching { resolver.delete(uri, null, null) }
            }
            throw e
        }
    }
}
