package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.DagEntry
import com.example.model.OwnerCategory
import com.example.model.OwnerEntry
import com.example.model.SessionSettings
import com.example.ui.components.KhatianTextField
import com.example.util.AppStrings
import com.example.util.BengaliNumberUtils
import com.example.util.LandCalculationEngine
import com.example.util.LandUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnerEditorDialog(
    initial: OwnerEntry?,
    settings: SessionSettings,
    onSave: (OwnerEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI
    val useBnNums = settings.useBengaliNumerals

    var category by remember(initial) { mutableStateOf(initial?.category ?: OwnerCategory.MUL_MALIK) }
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var fatherOrHusband by remember(initial) { mutableStateOf(initial?.fatherOrHusbandName ?: "") }
    var address by remember(initial) { mutableStateOf(initial?.address ?: "") }
    var ownershipDetails by remember(initial) { mutableStateOf(initial?.ownershipDetails ?: "") }
    var hissa by remember(initial) { mutableStateOf(initial?.hissa ?: "") }
    var landAmount by remember(initial) { mutableStateOf(initial?.landAmount ?: "") }
    var landUnit by remember(initial) { mutableStateOf(initial?.landUnit ?: LandUnit.SHOTOK) }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) strings.addOwner else strings.editOwner) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(strings.ownerCategory, style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OwnerCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(if (isBn) cat.labelBn else cat.labelEn) },
                        )
                    }
                }

                KhatianTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = strings.ownerName,
                    errorText = nameError,
                    testTag = "owner_name_input",
                )
                KhatianTextField(
                    value = fatherOrHusband,
                    onValueChange = { fatherOrHusband = it },
                    label = strings.fatherOrHusband,
                    testTag = "owner_father_input",
                )
                KhatianTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = strings.address,
                    singleLine = false,
                    minLines = 2,
                    testTag = "owner_address_input",
                )
                KhatianTextField(
                    value = ownershipDetails,
                    onValueChange = { ownershipDetails = it },
                    label = strings.ownershipDetails,
                    placeholder = if (isBn) "যেমন: পৈতৃক / ক্রয়সূত্রে" else "e.g., Inheritance / Deed",
                    testTag = "owner_details_input",
                )
                KhatianTextField(
                    value = hissa,
                    onValueChange = { hissa = it },
                    label = strings.hissaLabel,
                    testTag = "owner_hissa_input",
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val presets = if (useBnNums) listOf("১.০০", "০.৫০", "০.২৫", "১/২", "১/৪", "৪ আনা")
                    else listOf("1.00", "0.50", "0.25", "1/2", "1/4", "4 আনা")
                    presets.forEach { preset ->
                        SuggestionChip(
                            onClick = { hissa = preset },
                            label = { Text(preset) },
                        )
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LandUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = landUnit == unit,
                            onClick = { landUnit = unit },
                            label = { Text(if (isBn) unit.labelBn else unit.labelEn) },
                        )
                    }
                }
                KhatianTextField(
                    value = landAmount,
                    onValueChange = { landAmount = it },
                    label = "${strings.landAmount} (${if (isBn) landUnit.labelBn else landUnit.labelEn})",
                    testTag = "owner_land_amount_input",
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = if (isBn) "মালিকের নাম লিখুন" else "Owner name is required"
                        return@Button
                    }
                    onSave(
                        OwnerEntry(
                            id = initial?.id ?: java.util.UUID.randomUUID().toString(),
                            category = category,
                            name = name.trim(),
                            fatherOrHusbandName = fatherOrHusband.trim(),
                            address = address.trim(),
                            ownershipDetails = ownershipDetails.trim(),
                            hissa = hissa.trim(),
                            landAmount = landAmount.trim(),
                            landUnit = landUnit,
                        )
                    )
                },
                modifier = Modifier.testTag("save_owner_button"),
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DagEditorDialog(
    initial: DagEntry?,
    commonClasses: List<String>,
    settings: SessionSettings,
    globalAutoCalculate: Boolean,
    onSave: (DagEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI
    val useBnNums = settings.useBengaliNumerals

    var dagNumber by remember(initial) { mutableStateOf(initial?.dagNumber ?: "") }
    var landClass by remember(initial) { mutableStateOf(initial?.landClass ?: "") }
    var isNonAgri by remember(initial) { mutableStateOf(initial?.isNonAgricultural ?: false) }
    var totalDagArea by remember(initial) { mutableStateOf(initial?.totalDagArea ?: "") }
    var ownerShare by remember(initial) { mutableStateOf(initial?.ownerShare ?: if (useBnNums) "১.০০" else "1.00") }
    var manualMode by remember(initial) { mutableStateOf(initial?.manualCalculationDisabled ?: !globalAutoCalculate) }
    var entitledArea by remember(initial) { mutableStateOf(initial?.entitledArea ?: "") }
    var landUnit by remember(initial) { mutableStateOf(initial?.landUnit ?: LandUnit.SHOTOK) }
    var remarks by remember(initial) { mutableStateOf(initial?.remarks ?: "") }
    var dagError by remember { mutableStateOf<String?>(null) }
    var areaError by remember { mutableStateOf<String?>(null) }

    fun recomputeEntitled(totStr: String, shareStr: String) {
        if (manualMode) return
        val tot = BengaliNumberUtils.parseDoubleOrNull(totStr)
        val frac = LandCalculationEngine.parseHissaFraction(shareStr)
        if (tot != null && frac != null) {
            val result = LandCalculationEngine.calculateEntitledArea(tot, frac)
            entitledArea = BengaliNumberUtils.formatDecimal(result, 4, useBnNums)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) strings.addDag else strings.editDag) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KhatianTextField(
                    value = dagNumber,
                    onValueChange = {
                        dagNumber = it
                        dagError = null
                    },
                    label = strings.dagNumber,
                    errorText = dagError,
                    testTag = "dag_number_input",
                )

                KhatianTextField(
                    value = landClass,
                    onValueChange = { landClass = it },
                    label = strings.landClass,
                    testTag = "dag_class_input",
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    commonClasses.forEach { cls ->
                        SuggestionChip(
                            onClick = {
                                landClass = cls
                                isNonAgri = cls in listOf("বাড়ী", "বাড়ি", "দালান", "ভিটা", "বাস্তু", "বাণিজ্যিক", "আবাসিক প্লট")
                            },
                            label = { Text(cls) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isNonAgri,
                        onClick = { isNonAgri = false },
                        label = { Text(if (isBn) "কৃষি ৫(ক)" else "Agri 5(a)") },
                    )
                    FilterChip(
                        selected = isNonAgri,
                        onClick = { isNonAgri = true },
                        label = { Text(if (isBn) "অকৃষি ৫(খ)" else "Non-Agri 5(b)") },
                    )
                }

                Text(strings.unitConverterTitle, style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    LandUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = landUnit == unit,
                            onClick = {
                                val oldTot = BengaliNumberUtils.parseDoubleOrNull(totalDagArea)
                                val oldEnt = BengaliNumberUtils.parseDoubleOrNull(entitledArea)
                                if (oldTot != null) {
                                    totalDagArea = BengaliNumberUtils.formatDecimal(
                                        LandCalculationEngine.convert(oldTot, landUnit, unit), 4, useBnNums
                                    )
                                }
                                if (oldEnt != null) {
                                    entitledArea = BengaliNumberUtils.formatDecimal(
                                        LandCalculationEngine.convert(oldEnt, landUnit, unit), 4, useBnNums
                                    )
                                }
                                landUnit = unit
                            },
                            label = { Text(if (isBn) unit.labelBn else unit.labelEn) },
                        )
                    }
                }

                KhatianTextField(
                    value = totalDagArea,
                    onValueChange = {
                        totalDagArea = it
                        areaError = null
                        recomputeEntitled(it, ownerShare)
                    },
                    label = "${strings.totalDagArea} (${if (isBn) landUnit.labelBn else landUnit.labelEn})",
                    errorText = areaError,
                    testTag = "dag_total_area_input",
                )

                KhatianTextField(
                    value = ownerShare,
                    onValueChange = {
                        ownerShare = it
                        recomputeEntitled(totalDagArea, it)
                    },
                    label = strings.ownerShare,
                    placeholder = if (isBn) "১.০০ বা ১/২ বা ৪ আনা" else "1.00 or 1/2",
                    testTag = "dag_share_input",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = strings.autoCalculateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = !manualMode,
                        onCheckedChange = { autoOn ->
                            manualMode = !autoOn
                            if (autoOn) recomputeEntitled(totalDagArea, ownerShare)
                        },
                    )
                }

                KhatianTextField(
                    value = entitledArea,
                    onValueChange = {
                        entitledArea = it
                        if (!manualMode) manualMode = true
                    },
                    label = "${strings.entitledArea} (${if (isBn) landUnit.labelBn else landUnit.labelEn})",
                    testTag = "dag_entitled_area_input",
                )

                KhatianTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = strings.remarks,
                    singleLine = false,
                    minLines = 2,
                    testTag = "dag_remarks_input",
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dagNumber.isBlank()) {
                        dagError = if (isBn) "দাগ নম্বর লিখুন" else "Dag number is required"
                        return@Button
                    }
                    val totParsed = BengaliNumberUtils.parseDoubleOrNull(totalDagArea)
                    if (totalDagArea.isNotBlank() && (totParsed == null || totParsed < 0.0)) {
                        areaError = if (isBn) "সঠিক জমির পরিমাণ লিখুন" else "Enter valid non-negative area"
                        return@Button
                    }
                    onSave(
                        DagEntry(
                            id = initial?.id ?: java.util.UUID.randomUUID().toString(),
                            dagNumber = dagNumber.trim(),
                            landClass = landClass.trim(),
                            isNonAgricultural = isNonAgri,
                            totalDagArea = totalDagArea.trim(),
                            ownerShare = ownerShare.trim(),
                            entitledArea = entitledArea.trim(),
                            landUnit = landUnit,
                            manualCalculationDisabled = manualMode,
                            remarks = remarks.trim(),
                        )
                    )
                },
                modifier = Modifier.testTag("save_dag_button"),
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
    )
}
