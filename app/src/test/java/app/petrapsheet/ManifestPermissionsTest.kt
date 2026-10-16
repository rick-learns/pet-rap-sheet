package app.petrapsheet

import android.Manifest
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory

/** Privacy rules from AGENTS.md: no network access, no storage or camera permissions. */
@RunWith(AndroidJUnit4::class)
class ManifestPermissionsTest {
    private val forbidden = listOf(Manifest.permission.INTERNET)

    @Test
    fun mergedManifestFileHasNoInternetPermission() {
        val requested = mergedManifestPermissions()
        for (permission in forbidden) {
            assertFalse("merged manifest requests $permission: $requested", permission in requested)
        }
    }

    /** Photos come from the Photo Picker and the camera would come from an intent: no permissions. */
    @Test
    fun mergedManifestFileHasNoStorageOrCameraPermission() {
        val requested = mergedManifestPermissions()
        val storage = requested.filter {
            it.startsWith("android.permission.READ_MEDIA_") ||
                it == Manifest.permission.READ_EXTERNAL_STORAGE ||
                it == Manifest.permission.WRITE_EXTERNAL_STORAGE ||
                it == Manifest.permission.MANAGE_EXTERNAL_STORAGE ||
                it == Manifest.permission.CAMERA
        }
        assertTrue("merged manifest requests $storage", storage.isEmpty())
    }

    private fun mergedManifestPermissions(): List<String> {
        val manifest = mergedManifestFile()
        assertTrue("merged manifest not found at $manifest", manifest.isFile)
        val doc = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(manifest)
        return listOf("uses-permission", "uses-permission-sdk-23").flatMap { tag ->
            val nodes = doc.getElementsByTagName(tag)
            (0 until nodes.length).map { i ->
                nodes.item(i).attributes.getNamedItemNS(ANDROID_NS, "name").nodeValue
            }
        }
    }

    @Test
    fun installedPackageHasNoInternetPermission() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        val requested = info.requestedPermissions?.toList().orEmpty()
        for (permission in forbidden) {
            assertFalse("package requests $permission: $requested", permission in requested)
        }
    }

    /** AGP hands Robolectric the merged manifest path through this generated resource. */
    private fun mergedManifestFile(): File {
        val props = Properties()
        val stream = javaClass.classLoader!!.getResourceAsStream("com/android/tools/test_config.properties")
            ?: error("com/android/tools/test_config.properties missing; is isIncludeAndroidResources enabled?")
        stream.use(props::load)
        return File(props.getProperty("android_merged_manifest") ?: error("android_merged_manifest not set"))
    }

    private companion object {
        const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    }
}
