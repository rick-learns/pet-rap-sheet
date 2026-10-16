package app.petrapsheet.share

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.IntentCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.petrapsheet.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DocumentShareTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)

    @Before
    fun setUp() = Fixtures.resetFileProviderCache()

    @Test
    fun exportsAPngIntoTheSharedCacheDir() {
        val file = DocumentShare.exportPng(context, bitmap, "mugshot-2026-12345")
        assertEquals("mugshot-2026-12345.png", file.name)
        assertEquals("shared", file.parentFile!!.name)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        assertEquals("image/png", bounds.outMimeType)
    }

    @Test
    fun sendIntentSharesAPngThroughTheFileProvider() {
        val file = DocumentShare.exportPng(context, bitmap, "mugshot-2026-12345")
        val intent = DocumentShare.sendIntent(context, file)

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("image/png", intent.type)
        val uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        assertNotNull(uri)
        assertEquals(ContentResolver.SCHEME_CONTENT, uri!!.scheme)
        assertEquals("app.petrapsheet.fileprovider", uri.authority)
        assertEquals(uri, intent.clipData!!.getItemAt(0).uri)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun chooserWrapsTheSendIntent() {
        val file = DocumentShare.exportPng(context, bitmap, "mugshot-2026-12345")
        val chooser = DocumentShare.chooser(context, file, "Share mugshot")
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val target = IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java)
        assertEquals(Intent.ACTION_SEND, target!!.action)
    }

    @Test
    fun sendMultipleIntentSharesAllThreePngsThroughFileProvider() {
        val files = DocumentShare.exportPngs(
            context,
            listOf(
                "mugshot-2026-12345" to bitmap,
                "charge-sheet-2026-12345" to bitmap,
                "summons-2026-12345" to bitmap,
            ),
        )
        assertEquals(3, files.size)

        val intent = DocumentShare.sendMultipleIntent(context, files)
        assertEquals(Intent.ACTION_SEND_MULTIPLE, intent.action)
        assertEquals("image/png", intent.type)

        val uris = IntentCompat.getParcelableArrayListExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        assertNotNull(uris)
        assertEquals(3, uris!!.size)

        for (uri in uris) {
            assertEquals(ContentResolver.SCHEME_CONTENT, uri.scheme)
            assertEquals("app.petrapsheet.fileprovider", uri.authority)
        }

        val clipData = intent.clipData
        assertNotNull(clipData)
        assertEquals(3, clipData!!.itemCount)
        for (i in 0 until 3) {
            assertEquals(uris[i], clipData.getItemAt(i).uri)
        }
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun chooserMultipleWrapsMultipleIntent() {
        val files = DocumentShare.exportPngs(
            context,
            listOf("doc1" to bitmap, "doc2" to bitmap, "doc3" to bitmap),
        )
        val chooser = DocumentShare.chooserMultiple(context, files, "Share all")
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val target = IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java)
        assertEquals(Intent.ACTION_SEND_MULTIPLE, target!!.action)
    }

    @Test
    fun mediaStoreSaverWritesPngToPicturesPetRapSheet() {
        assertTrue("MediaStore saver should be supported on test runner (API 29+)", MediaStoreSaver.isSupported)
        val uri = MediaStoreSaver.saveToGallery(context, "test-doc-2026", bitmap)
        assertEquals(ContentResolver.SCHEME_CONTENT, uri.scheme)

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)!!.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        assertEquals("image/png", bounds.outMimeType)
        assertEquals(1080, bounds.outWidth)
        assertEquals(1350, bounds.outHeight)
    }

    @Test
    fun saveAllToGalleryRollsBackOnFailure() {
        val validBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val badBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply { recycle() }

        var thrown = false
        try {
            MediaStoreSaver.saveAllToGallery(
                context,
                listOf(
                    "doc1-rollback" to validBitmap,
                    "doc2-bad" to badBitmap,
                ),
            )
        } catch (e: Exception) {
            thrown = true
        }
        assertTrue("Expected saveAllToGallery to throw when an item fails", thrown)

        // Verify that doc1-rollback was cleaned up from MediaStore so no partial pack remains
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("doc1-rollback.png")
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            assertEquals("Rolled-back image should not remain in MediaStore", 0, cursor.count)
        }
    }

    @Test
    fun saveToGalleryFailsOnRecycledBitmapAndCleansUp() {
        val badBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply { recycle() }
        var thrown = false
        try {
            MediaStoreSaver.saveToGallery(context, "test-fail-cleanup", badBitmap)
        } catch (e: Exception) {
            thrown = true
        }
        assertTrue("Expected saveToGallery to throw on bad bitmap", thrown)

        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("test-fail-cleanup.png")
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            assertEquals("Failed image should be cleaned up from MediaStore", 0, cursor.count)
        }
    }
}
