package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.PageSizeOption
import com.example.model.PdfQuality
import com.example.model.PrintOrientationPref
import com.example.model.SessionSettings
import com.example.ui.components.ClearAllConfirmationDialog
import com.example.ui.components.SectionCard
import com.example.util.AppStrings
import com.example.viewmodel.KhatianViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: SessionSettings,
    viewModel: KhatianViewModel,
    onBack: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI
    var showClearDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showClearDialog) {
        ClearAllConfirmationDialog(
            strings = strings,
            onConfirm = {
                viewModel.clearAllData()
                showClearDialog = false
                scope.launch { snackbarHostState.showSnackbar(strings.clearedSnackbar) }
            },
            onDismiss = { showClearDialog = false },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(strings.settings, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SectionCard(
                        title = if (isBn) "ভাষা ও সংখ্যা পদ্ধতি" else "Language & Numerals",
                    ) {
                        Text(
                            if (isBn) "অ্যাপের ভাষা" else "App Language",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = settings.language == lang,
                                    onClick = { viewModel.updateLanguage(lang) },
                                    label = { Text(if (isBn) lang.labelBn else lang.labelEn) },
                                    modifier = Modifier.testTag("lang_chip_${lang.name.lowercase()}"),
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (isBn) "বাংলা সংখ্যা (০–৯) ব্যবহার করুন" else "Use Bengali Numerals (০–৯)",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    if (isBn) "বন্ধ করলে ইংরেজি সংখ্যা (0–9) ব্যবহৃত হবে" else "Switch off for English digits (0–9)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = settings.useBengaliNumerals,
                                onCheckedChange = { viewModel.updateUseBengaliNumerals(it) },
                            )
                        }
                    }
                }

                item {
                    SectionCard(
                        title = if (isBn) "পৃষ্ঠার সাইজ ও PDF মান" else "Default Page Size & PDF Quality",
                    ) {
                        Text(
                            if (isBn) "ডিফল্ট পৃষ্ঠা সাইজ" else "Default Page Size",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PageSizeOption.entries.forEach { size ->
                                FilterChip(
                                    selected = settings.defaultPageSize == size,
                                    onClick = { viewModel.updateDefaultPageSize(size) },
                                    label = { Text(if (isBn) size.labelBn else size.labelEn) },
                                )
                            }
                        }

                        Text(
                            if (isBn) "PDF Quality" else "PDF Render Quality",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PdfQuality.entries.forEach { q ->
                                FilterChip(
                                    selected = settings.pdfQuality == q,
                                    onClick = { viewModel.updatePdfQuality(q) },
                                    label = { Text(if (isBn) q.labelBn else q.labelEn) },
                                )
                            }
                        }
                    }
                }

                item {
                    SectionCard(
                        title = if (isBn) "প্রিন্ট সেটিংস (Print Settings)" else "Print Settings",
                    ) {
                        Text(
                            if (isBn) "প্রিন্ট ওরিয়েন্টেশন" else "Print Orientation",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PrintOrientationPref.entries.forEach { orient ->
                                FilterChip(
                                    selected = settings.printOrientationPref == orient,
                                    onClick = { viewModel.updatePrintOrientationPref(orient) },
                                    label = { Text(if (isBn) orient.labelBn else orient.labelEn) },
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (isBn) "সাদা-কালো প্রিন্ট মোড (Ink Saver)" else "Monochrome / Ink-Saver Print",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    if (isBn) "প্রিন্ট ও PDF-এ কালি সাশ্রয়ী সাদা-কালো বর্ডার ব্যবহার করে" else "Uses crisp black-and-white lines for economical printing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = settings.printInkSaverGrayscale,
                                onCheckedChange = { viewModel.updatePrintInkSaver(it) },
                            )
                        }
                    }
                }

                item {
                    SectionCard(
                        title = if (isBn) "ডাটা ও গোপনীয়তা নিয়ন্ত্রণ" else "Session Data & Privacy",
                        subtitle = if (isBn) {
                            "এই অ্যাপ কোনো তথ্য স্থায়ীভাবে সংরক্ষণ করে না। বর্তমান সেশনের সব অস্থায়ী তথ্য মুছতে নিচের বাটনে চাপুন।"
                        } else {
                            "Zero data is stored permanently. Clear all temporary inputs from RAM below."
                        },
                    ) {
                        Button(
                            onClick = { showClearDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_clear_all_button"),
                        ) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBn) "Clear All Data (সব তথ্য মুছুন)" else "Clear All Data")
                        }
                    }
                }

                item {
                    SectionCard(title = strings.homeQuickLinksTitle) {
                        OutlinedButton(
                            onClick = onOpenGuide,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.usageGuide)
                        }
                        OutlinedButton(
                            onClick = onOpenPrivacy,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Icon(Icons.Outlined.PrivacyTip, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.privacyPolicy)
                        }
                        OutlinedButton(
                            onClick = onOpenAbout,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.aboutApp)
                        }
                    }
                }
            }
        }
    }
}
