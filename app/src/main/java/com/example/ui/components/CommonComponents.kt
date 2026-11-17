package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SurveyType
import com.example.util.BangladeshGeoData
import com.example.util.MouzaJlEntry
import com.example.util.UiStrings

@Composable
fun DraftDisclaimerBanner(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
fun PrivacyRamBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
fun KhatianTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    testTag: String = "",
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotBlank()) {
                { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)) }
            } else null,
            isError = errorText != null,
            supportingText = if (errorText != null) {
                { Text(errorText, color = MaterialTheme.colorScheme.error) }
            } else null,
            singleLine = singleLine,
            minLines = minLines,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        )
    }
}

@Composable
fun ClearAllConfirmationDialog(
    strings: UiStrings,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text(strings.clearAllConfirmTitle) },
        text = { Text(strings.clearAllConfirmBody) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_clear_all_button"),
            ) {
                Text(strings.confirmClear, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_clear_all_button"),
            ) {
                Text(strings.cancel)
            }
        },
    )
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun TappableLocationField(
    value: String,
    label: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    testTag: String = "",
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                placeholder = {
                    Text(
                        placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = label,
                    )
                },
                isError = errorText != null,
                supportingText = if (errorText != null) {
                    { Text(errorText, color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onClick() },
            )
        }
    }
}

@Composable
fun SearchableLocationPickerDialog(
    title: String,
    items: List<String>,
    allItemsFallback: List<String>? = null,
    selectedValue: String,
    isBengali: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var showAll by remember { mutableStateOf(false) }

    val sourceList = if (showAll && allItemsFallback != null) allItemsFallback else items
    val filtered = remember(query, sourceList) {
        val q = query.trim()
        if (q.isEmpty()) sourceList else sourceList.filter { it.contains(q, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(if (isBengali) "খুঁজুন বা নিজের নাম লিখুন" else "Search or type custom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (allItemsFallback != null && allItemsFallback.size > items.size) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        FilterChip(
                            selected = showAll,
                            onClick = { showAll = !showAll },
                            label = {
                                Text(
                                    if (isBengali) {
                                        if (showAll) "শুধু নির্বাচিত অঞ্চল" else "সকল (${allItemsFallback.size}) দেখান"
                                    } else {
                                        if (showAll) "Filtered Only" else "Show All (${allItemsFallback.size})"
                                    }
                                )
                            },
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (query.isNotBlank() && filtered.none { it.equals(query.trim(), ignoreCase = true) }) {
                        item {
                            Surface(
                                onClick = { onSelect(query.trim()) },
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = if (isBengali) "\"${query.trim()}\" ব্যবহার করুন" else "Use \"${query.trim()}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                )
                            }
                        }
                    }
                    items(filtered.size) { idx ->
                        val item = filtered[idx]
                        val isSelected = item.equals(selectedValue.trim(), ignoreCase = true)
                        Surface(
                            onClick = { onSelect(item) },
                            shape = MaterialTheme.shapes.small,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বন্ধ করুন" else "Close")
            }
        },
    )
}

@Composable
fun MouzaJlPickerDialog(
    title: String,
    surveyType: SurveyType,
    filteredEntries: List<MouzaJlEntry>,
    allEntries: List<MouzaJlEntry>,
    selectedMouza: String,
    selectedJl: String,
    isBengali: Boolean,
    onSelectEntry: (MouzaJlEntry) -> Unit,
    onSelectCustom: (jl: String, mouza: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var showAll by remember { mutableStateOf(false) }
    var customJl by remember(selectedJl) { mutableStateOf(selectedJl) }
    var customMouza by remember(selectedMouza) { mutableStateOf(selectedMouza) }

    val sourceList = if (showAll) allEntries else filteredEntries
    val filtered = remember(query, sourceList, surveyType) {
        val q = query.trim()
        if (q.isEmpty()) sourceList
        else sourceList.filter {
            it.displayLabelForSurvey(surveyType).contains(q, ignoreCase = true) ||
                it.mouza.contains(q, ignoreCase = true) ||
                it.csJl.contains(q, ignoreCase = true) ||
                it.saJl.contains(q, ignoreCase = true) ||
                it.rsJl.contains(q, ignoreCase = true) ||
                it.bsJl.contains(q, ignoreCase = true) ||
                it.district.contains(q, ignoreCase = true) ||
                it.upazila.contains(q, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // ২-ঘরে সরাসরি সঠিক জে.এল. নং ও মৌজার নাম লেখার কার্ড
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = if (isBengali) "খতিয়ান অনুযায়ী সঠিক জে.এল. ও মৌজা লিখুন (${surveyType.code})" else "Enter exact J.L. No. & Mouza (${surveyType.code})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = customJl,
                                onValueChange = { customJl = it },
                                label = { Text(if (isBengali) "জে.এল. নং" else "J.L.") },
                                singleLine = true,
                                modifier = Modifier.weight(0.38f),
                            )
                            OutlinedTextField(
                                value = customMouza,
                                onValueChange = { customMouza = it },
                                label = { Text(if (isBengali) "মৌজার নাম" else "Mouza") },
                                singleLine = true,
                                modifier = Modifier.weight(0.62f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(
                                enabled = customMouza.isNotBlank() || customJl.isNotBlank(),
                                onClick = { onSelectCustom(customJl.trim(), customMouza.trim()) },
                            ) {
                                Text(if (isBengali) "তথ্য বসান" else "Apply", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (allEntries.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (isBengali) "যাচাইকৃত মৌজা তালিকা (${surveyType.code})" else "Verified Records (${surveyType.code})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (allEntries.size > filteredEntries.size) {
                            FilterChip(
                                selected = showAll,
                                onClick = { showAll = !showAll },
                                label = {
                                    Text(
                                        if (isBengali) {
                                            if (showAll) "শুধু এই এলাকা" else "সকল যাচাইকৃত (${allEntries.size})"
                                        } else {
                                            if (showAll) "This Area Only" else "All Verified (${allEntries.size})"
                                        }
                                    )
                                },
                            )
                        }
                    }

                    if (sourceList.isNotEmpty()) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text(if (isBengali) "যাচাইকৃত তালিকায় খুঁজুন" else "Filter verified list") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                if (filtered.isEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (isBengali) {
                                "এই জেলা/উপজেলার যাচাইকৃত মৌজা ও জে.এল. তালিকা অফলাইনে যুক্ত নেই (বাংলাদেশে ~৬০,০০০ মৌজায় CS/SA/RS/BS জরিপভেদে জে.এল. নম্বর আলাদা হয়)। মূল খতিয়ান বা eporcha.gov.bd দেখে উপরে সঠিক জে.এল. নং ও মৌজার নাম লিখুন।"
                            } else {
                                "Verified offline J.L. numbers for this Upazila are not bundled (J.L. numbers vary by CS/SA/RS/BS survey across ~60,000 mouzas). Enter the exact J.L. No. & Mouza from your Khatian or eporcha.gov.bd above."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 210.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        items(filtered.size) { idx ->
                            val entry = filtered[idx]
                            val surveyJl = entry.jlForSurvey(surveyType)
                            val isSelected = entry.mouza.equals(selectedMouza.trim(), ignoreCase = true) &&
                                surveyJl.equals(selectedJl.trim(), ignoreCase = true)
                            Surface(
                                onClick = { onSelectEntry(entry) },
                                shape = MaterialTheme.shapes.small,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                                    Text(
                                        text = entry.displayLabelForSurvey(surveyType),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    )
                                    val sub = listOf(entry.locationSubtitle, entry.surveyJlSummary)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" • ")
                                    if (sub.isNotBlank()) {
                                        Text(
                                            text = sub,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বন্ধ করুন" else "Close")
            }
        },
    )
}

