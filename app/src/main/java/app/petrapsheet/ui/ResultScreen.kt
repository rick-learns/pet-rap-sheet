package app.petrapsheet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.petrapsheet.R
import app.petrapsheet.document.DocumentSize
import app.petrapsheet.share.MediaStoreSaver

enum class DocumentType {
    MUGSHOT, CHARGE_SHEET, SUMMONS
}

@Composable
fun ResultScreen(
    bookingNumber: String,
    document: ImageBitmap?,
    precinctCamera: Boolean,
    onPrecinctCameraChange: (Boolean) -> Unit,
    onShare: () -> Unit,
    onNewBooking: () -> Unit,
    modifier: Modifier = Modifier,
    currentType: DocumentType = DocumentType.MUGSHOT,
    onTypeChange: (DocumentType) -> Unit = {},
    onShareAll: () -> Unit = {},
    canShareAll: Boolean = true,
    onSaveToGallery: (() -> Unit)? = null,
    onSaveAllToGallery: (() -> Unit)? = null,
    canSaveAll: Boolean = true,
    onOpenHistory: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row {
            Text(
                stringResource(R.string.result_title).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f).alignByBaseline(),
            )
            Text(
                stringResource(R.string.result_booking_number, bookingNumber),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alignByBaseline(),
            )
        }

        // Document selector tabs
        val types = listOf(
            DocumentType.MUGSHOT to R.string.doc_mugshot,
            DocumentType.CHARGE_SHEET to R.string.doc_charge_sheet,
            DocumentType.SUMMONS to R.string.doc_summons,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            types.forEachIndexed { index, (type, labelRes) ->
                SegmentedButton(
                    selected = currentType == type,
                    onClick = { onTypeChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, types.size),
                    modifier = Modifier.testTag("tab_${type.name.lowercase()}"),
                ) {
                    Text(stringResource(labelRes))
                }
            }
        }

        val aspect = DocumentSize.PORTRAIT.width.toFloat() / DocumentSize.PORTRAIT.height
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
                .shadow(6.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            if (document != null) {
                Image(
                    bitmap = document,
                    contentDescription = stringResource(R.string.result_document_description),
                    modifier = Modifier.fillMaxSize().testTag(TAG_DOCUMENT),
                )
            } else {
                Text(stringResource(R.string.result_rendering), style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (currentType == DocumentType.MUGSHOT) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = precinctCamera, role = Role.Switch, onValueChange = onPrecinctCameraChange)
                    .testTag(TAG_PRECINCT_CAMERA),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.result_precinct_camera), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.result_precinct_camera_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = precinctCamera, onCheckedChange = null)
            }
        }

        val singleShareText = when (currentType) {
            DocumentType.MUGSHOT -> stringResource(R.string.result_share)
            DocumentType.CHARGE_SHEET -> stringResource(R.string.result_share_charge_sheet)
            DocumentType.SUMMONS -> stringResource(R.string.result_share_summons)
        }

        Button(
            onClick = onShare,
            enabled = document != null,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag(TAG_SHARE_CURRENT),
        ) { Text(singleShareText.uppercase()) }

        OutlinedButton(
            onClick = onShareAll,
            enabled = canShareAll,
            modifier = Modifier.fillMaxWidth().testTag(TAG_SHARE_ALL),
        ) { Text(stringResource(R.string.result_share_all).uppercase()) }

        if (MediaStoreSaver.isSupported) {
            if (onSaveToGallery != null) {
                OutlinedButton(
                    onClick = onSaveToGallery,
                    enabled = document != null,
                    modifier = Modifier.fillMaxWidth().testTag(TAG_SAVE_TO_GALLERY),
                ) { Text(stringResource(R.string.result_save_to_gallery).uppercase()) }
            }

            if (onSaveAllToGallery != null) {
                OutlinedButton(
                    onClick = onSaveAllToGallery,
                    enabled = canSaveAll,
                    modifier = Modifier.fillMaxWidth().testTag(TAG_SAVE_ALL_TO_GALLERY),
                ) { Text(stringResource(R.string.result_save_all_to_gallery).uppercase()) }
            }
        }

        if (onOpenHistory != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onNewBooking,
                    modifier = Modifier.weight(1f).testTag(TAG_NEW_BOOKING),
                ) {
                    Text(stringResource(R.string.result_new_booking))
                }
                OutlinedButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.weight(1f).testTag(TAG_RESULT_HISTORY),
                ) {
                    Text(stringResource(R.string.form_history))
                }
            }
        } else {
            OutlinedButton(
                onClick = onNewBooking,
                modifier = Modifier.fillMaxWidth().testTag(TAG_NEW_BOOKING),
            ) {
                Text(stringResource(R.string.result_new_booking))
            }
        }
    }
}

const val TAG_DOCUMENT = "document"
const val TAG_PRECINCT_CAMERA = "precinct_camera"
const val TAG_SHARE_CURRENT = "share_current"
const val TAG_SHARE_ALL = "share_all"
const val TAG_SAVE_TO_GALLERY = "save_to_gallery"
const val TAG_SAVE_ALL_TO_GALLERY = "save_all_to_gallery"
const val TAG_NEW_BOOKING = "new_booking"
const val TAG_RESULT_HISTORY = "result_history"
