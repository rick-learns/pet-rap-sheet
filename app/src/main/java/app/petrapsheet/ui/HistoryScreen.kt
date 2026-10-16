package app.petrapsheet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.petrapsheet.R
import app.petrapsheet.history.BookingEntry
import app.petrapsheet.history.BookingRecord
import java.time.format.DateTimeFormatter
import java.util.Locale

const val TAG_HISTORY_BACK = "history_back"
const val TAG_HISTORY_EMPTY = "history_empty"
const val TAG_HISTORY_OPEN_PREFIX = "history_open_"
const val TAG_HISTORY_SHARE_PREFIX = "history_share_"
const val TAG_HISTORY_DELETE_PREFIX = "history_delete_"

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

@Composable
fun HistoryScreen(
    entries: List<BookingEntry>,
    onOpen: (BookingRecord) -> Unit,
    onShare: (BookingRecord) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.history_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    stringResource(R.string.history_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.testTag(TAG_HISTORY_BACK),
            ) {
                Text(stringResource(R.string.history_back))
            }
        }

        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface)

        if (entries.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp)
                    .testTag(TAG_HISTORY_EMPTY),
            ) {
                Text(
                    stringResource(R.string.history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            entries.forEach { entry ->
                HistoryItemCard(
                    entry = entry,
                    onOpen = { onOpen(entry.record) },
                    onShare = { onShare(entry.record) },
                    onDelete = { onDelete(entry.record.id) },
                )
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    entry: BookingEntry,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    val record = entry.record
    val thumbBitmap = remember(entry.thumbnailFile.lastModified()) {
        entry.loadThumbnail()?.asImageBitmap()
    }
    val dateText = remember(record.bookedOn) {
        DATE_FORMAT.format(record.bookedOn).uppercase(Locale.US)
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("history_item_${record.id}"),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 54.dp, height = 68.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    if (thumbBitmap != null) {
                        Image(
                            bitmap = thumbBitmap,
                            contentDescription = stringResource(R.string.history_item_description),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        record.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val aliasText = record.alias?.let { " · a.k.a. $it" } ?: ""
                    Text(
                        "${record.species.name}$aliasText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("${TAG_HISTORY_DELETE_PREFIX}${record.id}"),
                ) {
                    Text(
                        "✕",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("${TAG_HISTORY_SHARE_PREFIX}${record.id}"),
                ) {
                    Text(stringResource(R.string.history_share))
                }
                OutlinedButton(
                    onClick = onOpen,
                    modifier = Modifier.testTag("${TAG_HISTORY_OPEN_PREFIX}${record.id}"),
                ) {
                    Text(stringResource(R.string.history_open))
                }
            }
        }
    }
}
