package app.petrapsheet.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Writes rendered documents to app cache and hands them to the system share sheet. */
object DocumentShare {
    const val MIME_PNG = "image/png"
    private const val SHARED_DIR = "shared"

    fun authority(context: Context) = "${context.packageName}.fileprovider"

    /** Saves [bitmap] as `<name>.png` in the FileProvider's cache directory, replacing older exports. */
    fun exportPng(context: Context, bitmap: Bitmap, name: String): File {
        val dir = File(context.cacheDir, SHARED_DIR).apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    /** Saves multiple [items] (pairs of name to bitmap) into the FileProvider's cache directory. */
    fun exportPngs(context: Context, items: List<Pair<String, Bitmap>>): List<File> {
        val dir = File(context.cacheDir, SHARED_DIR).apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        return items.map { (name, bitmap) ->
            val file = File(dir, "$name.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            file
        }
    }

    /** An ACTION_SEND intent for [file] with a content:// URI that the receiver may read. */
    fun sendIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, authority(context), file)
        return Intent(Intent.ACTION_SEND).apply {
            type = MIME_PNG
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /** An ACTION_SEND_MULTIPLE intent for [files] with content:// URIs for all documents in the pack. */
    fun sendMultipleIntent(context: Context, files: List<File>): Intent {
        val fileProviderAuthority = authority(context)
        val uris = ArrayList(files.map { FileProvider.getUriForFile(context, fileProviderAuthority, it) })
        val firstUri = uris.firstOrNull() ?: Uri.EMPTY
        val clipData = ClipData.newRawUri(files.firstOrNull()?.name ?: "pack", firstUri)
        for (i in 1 until uris.size) {
            clipData.addItem(ClipData.Item(uris[i]))
        }
        return Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = MIME_PNG
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            this.clipData = clipData
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun chooser(context: Context, file: File, title: CharSequence): Intent =
        Intent.createChooser(sendIntent(context, file), title)

    fun chooserMultiple(context: Context, files: List<File>, title: CharSequence): Intent =
        Intent.createChooser(sendMultipleIntent(context, files), title)
}
