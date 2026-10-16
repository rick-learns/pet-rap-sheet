package app.petrapsheet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.petrapsheet.R
import app.petrapsheet.booking.Charge
import app.petrapsheet.booking.ChargeBank
import app.petrapsheet.booking.Species
import kotlin.random.Random

/** What the user has typed into the booking form so far. */
data class BookingForm(
    val name: String = "",
    val species: Species = Species.DOG,
    val alias: String = "",
    val town: String = "",
    val officer: String = "",
    val charges: List<Charge> = ChargeBank.defaultCharges(Species.DOG),
)

const val MAX_CUSTOM_CHARGE_LENGTH = 50
const val TAG_OPEN_HISTORY = "booking_open_history"
const val TAG_OPEN_ABOUT = "booking_open_about"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    form: BookingForm,
    onFormChange: (BookingForm) -> Unit,
    photo: ImageBitmap?,
    photoUnreadable: Boolean,
    onPickPhoto: () -> Unit,
    onBook: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenHistory: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
) {
    var customChargeText by rememberSaveable { mutableStateOf("") }
    var bankExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.form_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.form_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.testTag(TAG_OPEN_HISTORY),
                ) {
                    Text(stringResource(R.string.form_history))
                }
                OutlinedButton(
                    onClick = onOpenAbout,
                    modifier = Modifier.testTag(TAG_OPEN_ABOUT),
                ) {
                    Text(stringResource(R.string.form_about))
                }
            }
        }
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface)

        PhotoSlot(photo = photo, unreadable = photoUnreadable, onPickPhoto = onPickPhoto)

        OutlinedTextField(
            value = form.name,
            onValueChange = { onFormChange(form.copy(name = it)) },
            label = { Text(stringResource(R.string.form_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth().testTag(TAG_NAME),
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.form_species), style = MaterialTheme.typography.labelMedium)
            val options = listOf(
                Species.DOG to R.string.species_dog,
                Species.CAT to R.string.species_cat,
                Species.OTHER to R.string.species_other,
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, (species, label) ->
                    SegmentedButton(
                        selected = form.species == species,
                        onClick = { onFormChange(form.copy(species = species)) },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    ) { Text(stringResource(label)) }
                }
            }
        }

        OutlinedTextField(
            value = form.alias,
            onValueChange = { onFormChange(form.copy(alias = it)) },
            label = { Text(stringResource(R.string.form_alias)) },
            placeholder = { Text(stringResource(R.string.form_alias_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = form.town,
            onValueChange = { onFormChange(form.copy(town = it)) },
            label = { Text(stringResource(R.string.form_town)) },
            supportingText = { Text(stringResource(R.string.form_town_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = form.officer,
            onValueChange = { onFormChange(form.copy(officer = it)) },
            label = { Text(stringResource(R.string.form_officer)) },
            placeholder = { Text(stringResource(R.string.form_officer_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth().testTag(TAG_OFFICER),
        )

        // Charges section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.form_charges_heading),
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedButton(
                    onClick = {
                        val fresh = ChargeBank.randomCharges(form.species, Random.nextLong(), 3)
                        onFormChange(form.copy(charges = fresh))
                    },
                    modifier = Modifier.testTag(TAG_RANDOM_CHARGES),
                ) {
                    Text(stringResource(R.string.form_random_charges))
                }
            }

            // Bank selection dropdown
            val bankChoices = remember(form.species, form.charges) {
                ChargeBank.chargesFor(form.species).filter { it !in form.charges }
            }
            ExposedDropdownMenuBox(
                expanded = bankExpanded,
                onExpandedChange = { if (form.charges.size < 4) bankExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            if (form.charges.size < 4) stringResource(R.string.form_bank_charge)
                            else "Max 4 charges selected",
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                    enabled = form.charges.size < 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .testTag(TAG_BANK_CHARGE),
                )
                ExposedDropdownMenu(
                    expanded = bankExpanded,
                    onDismissRequest = { bankExpanded = false },
                ) {
                    bankChoices.forEach { charge ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(charge.description, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        charge.statute,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            onClick = {
                                if (form.charges.size < 4) {
                                    onFormChange(form.copy(charges = form.charges + charge))
                                }
                                bankExpanded = false
                            },
                        )
                    }
                }
            }

            // Custom charge input row
            val canAddCustom = customChargeText.isNotBlank() &&
                customChargeText.length <= MAX_CUSTOM_CHARGE_LENGTH &&
                form.charges.size < 4

            fun addCustom() {
                if (canAddCustom) {
                    val statute = ChargeBank.customStatute(form.charges.size + 1)
                    val charge = Charge(statute = statute, description = customChargeText.trim())
                    onFormChange(form.copy(charges = form.charges + charge))
                    customChargeText = ""
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = customChargeText,
                    onValueChange = { if (it.length <= MAX_CUSTOM_CHARGE_LENGTH) customChargeText = it },
                    label = { Text(stringResource(R.string.form_custom_charge)) },
                    placeholder = { Text("e.g. Grand Theft Sock") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { addCustom() }),
                    modifier = Modifier.weight(1f).testTag(TAG_CUSTOM_CHARGE),
                )

                Button(
                    onClick = { addCustom() },
                    enabled = canAddCustom,
                    modifier = Modifier.testTag(TAG_ADD_CUSTOM_CHARGE),
                ) {
                    Text(stringResource(R.string.form_add_charge))
                }
            }

            // Selected charges list
            form.charges.forEachIndexed { index, charge ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().testTag("charge_card_$index"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                charge.statute,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                charge.description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(
                            onClick = {
                                onFormChange(form.copy(charges = form.charges.filterIndexed { i, _ -> i != index }))
                            },
                            modifier = Modifier.size(36.dp).testTag("remove_charge_$index"),
                        ) {
                            Text(
                                "✕",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBook,
            enabled = photo != null && form.name.isNotBlank() && form.charges.size in 2..4,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(stringResource(R.string.form_book).uppercase()) }

        Text(
            stringResource(R.string.privacy_line),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAbout() },
        )
    }
}

@Composable
private fun PhotoSlot(photo: ImageBitmap?, unreadable: Boolean, onPickPhoto: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 100.dp, height = 125.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.onSurface),
        ) {
            if (photo != null) {
                Image(
                    bitmap = photo,
                    contentDescription = stringResource(R.string.form_photo_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().padding(2.dp),
                )
            } else {
                Text(
                    stringResource(R.string.form_photo_missing).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.form_photo_heading), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(if (unreadable) R.string.form_photo_unreadable else R.string.form_photo_hint),
                style = MaterialTheme.typography.bodySmall,
                color = if (unreadable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onPickPhoto, modifier = Modifier.testTag(TAG_PICK_PHOTO)) {
                Text(stringResource(if (photo == null) R.string.form_photo_choose else R.string.form_photo_change))
            }
        }
    }
}

const val TAG_NAME = "name"
const val TAG_PICK_PHOTO = "pick_photo"
const val TAG_OFFICER = "officer"
const val TAG_RANDOM_CHARGES = "random_charges"
const val TAG_CUSTOM_CHARGE = "custom_charge"
const val TAG_ADD_CUSTOM_CHARGE = "add_custom_charge"
const val TAG_BANK_CHARGE = "bank_charge"
