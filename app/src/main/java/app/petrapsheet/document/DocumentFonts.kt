package app.petrapsheet.document

import android.content.res.AssetManager
import android.graphics.Typeface

/** Typefaces for documents. Courier Prime is bundled under the OFL (assets/fonts). */
class DocumentFonts(val regular: Typeface, val bold: Typeface) {
    companion object {
        @Volatile
        private var cached: DocumentFonts? = null

        fun from(assets: AssetManager): DocumentFonts = cached ?: DocumentFonts(
            regular = Typeface.createFromAsset(assets, "fonts/CourierPrime-Regular.ttf"),
            bold = Typeface.createFromAsset(assets, "fonts/CourierPrime-Bold.ttf"),
        ).also { cached = it }
    }
}
