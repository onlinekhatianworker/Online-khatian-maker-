package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.KhatianDraftState
import com.example.model.SessionSettings
import com.example.ui.components.DraftDisclaimerBanner
import com.example.util.AppStrings
import com.example.util.BengaliNumberUtils
import com.example.viewmodel.KhatianViewModel
import com.example.viewmodel.PreviewUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    draft: KhatianDraftState,
    settings: SessionSettings,
    previewState: PreviewUiState,
    viewModel: KhatianViewModel,
    onBackToEdit: () -> Unit,
) {
    val context = LocalContext.current
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI
    val useBnNums = settings.useBengaliNumerals

    var currentPageIndex by remember { mutableIntStateOf(0) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.exportPdfToUri(context, uri) { success, errMsg ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (success) strings.pdfSavedSuccess
                        else "${strings.pdfSaveError}: ${errMsg.orEmpty()}"
                    )
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBn) "খতিয়ান প্রিভিউ" else "Khatian Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (isBn) draft.surveyType.titleBn else draft.surveyType.titleEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToEdit,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.backToEdit)
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = onBackToEdit,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("preview_edit_button"),
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.backToEdit)
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (previewState is PreviewUiState.Ready && previewState.pages.isNotEmpty()) {
                        val total = previewState.pages.size
                        val clampedIdx = currentPageIndex.coerceIn(0, total - 1)
                        if (clampedIdx != currentPageIndex) currentPageIndex = clampedIdx

                        // Zoom + Pagination Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilledTonalIconButton(
                                    onClick = {
                                        zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.75f)
                                    },
                                    modifier = Modifier.testTag("zoom_out_button"),
                                ) {
                                    Icon(Icons.Outlined.ZoomOut, contentDescription = strings.zoomOut)
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        zoomScale = 1f
                                        panOffset = Offset.Zero
                                    },
                                    modifier = Modifier.testTag("fit_screen_button"),
                                ) {
                                    Icon(Icons.Outlined.FitScreen, contentDescription = strings.fitToScreen)
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        zoomScale = (zoomScale + 0.25f).coerceAtMost(3.5f)
                                    },
                                    modifier = Modifier.testTag("zoom_in_button"),
                                ) {
                                    Icon(Icons.Outlined.ZoomIn, contentDescription = strings.zoomIn)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        if (currentPageIndex > 0) {
                                            currentPageIndex--
                                            zoomScale = 1f
                                            panOffset = Offset.Zero
                                        }
                                    },
                                    enabled = currentPageIndex > 0,
                                    modifier = Modifier.testTag("prev_page_button"),
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = strings.prevPage)
                                }
                                Text(
                                    text = "${BengaliNumberUtils.formatScript("${currentPageIndex + 1}", useBnNums)} / ${BengaliNumberUtils.formatScript("$total", useBnNums)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                FilledTonalIconButton(
                                    onClick = {
                                        if (currentPageIndex < total - 1) {
                                            currentPageIndex++
                                            zoomScale = 1f
                                            panOffset = Offset.Zero
                                        }
                                    },
                                    enabled = currentPageIndex < total - 1,
                                    modifier = Modifier.testTag("next_page_button"),
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = strings.nextPage)
                                }
                            }
                        }
                    }

                    // Primary Download PDF & Direct Print Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = {
                                val kNo = BengaliNumberUtils.toEnglishDigits(
                                    draft.basicInfo.khatianNumber.ifBlank { "draft" }
                                ).replace(Regex("[^A-Za-z0-9_-]"), "_")
                                val suggested = "Khatian_${draft.surveyType.code}_$kNo.pdf"
                                savePdfLauncher.launch(suggested)
                            },
                            enabled = previewState is PreviewUiState.Ready,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("download_pdf_button"),
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.downloadPdf, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.launchPrintJob(
                                    context = context,
                                    onStarted = {
                                        scope.launch { snackbarHostState.showSnackbar(strings.printJobStarted) }
                                    },
                                    onError = { err ->
                                        scope.launch { snackbarHostState.showSnackbar(err) }
                                    },
                                )
                            },
                            enabled = previewState is PreviewUiState.Ready,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("direct_print_button"),
                        ) {
                            Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.directPrint, maxLines = 1)
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE6E6E6)),
        ) {
            DraftDisclaimerBanner(
                text = strings.draftDisclaimerShort,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (zoomScale * zoom).coerceIn(0.75f, 3.5f)
                            zoomScale = newScale
                            panOffset = if (newScale > 1.01f) {
                                panOffset + pan
                            } else {
                                Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                when (previewState) {
                    PreviewUiState.Idle, PreviewUiState.Loading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isBn) "খতিয়ান প্রিভিউ প্রস্তুত হচ্ছে..." else "Rendering Khatian preview...",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    is PreviewUiState.Error -> {
                        Surface(
                            modifier = Modifier.padding(24.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {
                            Text(
                                text = "${strings.pdfSaveError}: ${previewState.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                    is PreviewUiState.Ready -> {
                        val bmp = previewState.pages.getOrNull(currentPageIndex)
                        if (bmp != null) {
                            Surface(
                                shadowElevation = 6.dp,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .graphicsLayer(
                                        scaleX = zoomScale,
                                        scaleY = zoomScale,
                                        translationX = panOffset.x,
                                        translationY = panOffset.y,
                                    ),
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Khatian Page Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
