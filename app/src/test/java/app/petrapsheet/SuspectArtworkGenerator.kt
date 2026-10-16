package app.petrapsheet

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Rewrites the committed suspect JPEG from [Fixtures.drawSuspect]. Skipped unless asked for:
 *
 * ```sh
 * source scripts/env.sh
 * WRITE_SUSPECT_JPEG=1 ./gradlew testDebugUnitTest --tests '*SuspectArtworkGenerator*' --rerun
 * bash scripts/record-screenshots.sh
 * ```
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SuspectArtworkGenerator {
    @Test
    fun writeSuspectJpeg() {
        assumeTrue(System.getenv("WRITE_SUSPECT_JPEG") == "1")
        // Gradle runs unit tests from the module directory.
        val file = File("src/test/resources/${Fixtures.SUSPECT_RESOURCE}")
        file.parentFile!!.mkdirs()
        file.outputStream().use { out ->
            check(Fixtures.drawSuspect().compress(Bitmap.CompressFormat.JPEG, Fixtures.SUSPECT_JPEG_QUALITY, out)) {
                "JPEG encode failed"
            }
        }
    }
}
