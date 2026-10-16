package app.petrapsheet.photo

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

/** Decodes a picked photo, downsampled for a 1080px document and turned upright. */
object PhotoLoader {
    private const val MAX_SIDE = 1600

    fun load(resolver: ContentResolver, uri: Uri): Bitmap? {
        if (uri.scheme == ContentResolver.SCHEME_FILE || uri.scheme == "file") {
            val path = uri.path ?: return null
            val file = java.io.File(path)
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.absolutePath)
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        // With inJustDecodeBounds, decodeStream always returns null; only the options are filled in.
        val stream = resolver.openInputStream(uri) ?: return null
        stream.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: return null

        val orientation = resolver.openInputStream(uri)?.use { stream ->
            runCatching {
                ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        val matrix = orientationMatrix(orientation) ?: return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * The transform that displays pixels stored with EXIF [orientation] upright, including the
     * mirrored orientations (2, 4, 5, 7). Null when the pixels are already upright.
     */
    private fun orientationMatrix(orientation: Int): Matrix? = when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> Matrix().apply { setScale(-1f, 1f) }
        ExifInterface.ORIENTATION_ROTATE_180 -> Matrix().apply { setRotate(180f) }
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> Matrix().apply { setScale(1f, -1f) }
        ExifInterface.ORIENTATION_TRANSPOSE -> Matrix().apply { setRotate(90f); postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_ROTATE_90 -> Matrix().apply { setRotate(90f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> Matrix().apply { setRotate(-90f); postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_ROTATE_270 -> Matrix().apply { setRotate(-90f) }
        else -> null
    }
}
