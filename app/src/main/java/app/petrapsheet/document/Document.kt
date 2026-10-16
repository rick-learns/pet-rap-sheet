package app.petrapsheet.document

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap

/** Pixel size of a rendered document. */
data class DocumentSize(val width: Int, val height: Int) {
    companion object {
        /** Every document in the pack is a 1080x1350 portrait PNG. */
        val PORTRAIT = DocumentSize(1080, 1350)
    }
}

/**
 * A printable document. Implementations draw with android.graphics only, so a new document type
 * plugs into rendering, preview and sharing without touching the UI.
 */
interface Document {
    /** Short name used for exported files, e.g. "mugshot". */
    val slug: String

    /** Draws the whole document onto [canvas], filling [size]. */
    fun render(canvas: Canvas, size: DocumentSize)
}

fun Document.renderToBitmap(size: DocumentSize = DocumentSize.PORTRAIT): Bitmap {
    val bitmap = createBitmap(size.width, size.height)
    render(Canvas(bitmap), size)
    return bitmap
}
