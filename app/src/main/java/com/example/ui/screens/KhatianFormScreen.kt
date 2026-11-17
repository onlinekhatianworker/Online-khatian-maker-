package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.DagEntry
import com.example.model.KhatianDraftState
import com.example.model.OwnerEntry
import com.example.model.SessionSettings
import com.example.model.SurveySchemas
import com.example.model.SurveyType
import com.example.ui.components.ClearAllConfirmationDialog
import com.example.ui.components.DraftDisclaimerBanner
import com.example.ui.components.KhatianTextField
import com.example.ui.components.MouzaJlPickerDialog
import com.example.ui.components.SearchableLocationPickerDialog
import com.example.ui.components.SectionCard
import com.example.ui.components.TappableLocationField
import com.example.util.AppStrings
import com.example.util.BangladeshGeoData
import com.example.util.BengaliNumberUtils
import com.example.util.LandCalculationEngine
import com.example.util.LandUnit
import com.example.viewmodel.KhatianViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhatianFormScreen(
    draft: KhatianDraftState,
    settings: SessionSettings,
    viewModel: KhatianViewModel,
    onBackHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPreview: () -> Unit,
) {
    val context = LocalContext.current
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI
    val schema = remember(draft.surveyType) { SurveySchemas.schemaFor(draft.surveyType) }

    var activeTab by remember { mutableIntStateOf(0) }
    var showClearDialog by remember { mutableStateOf(false) }
    var editingOwner by remember { mutableStateOf<OwnerEntry?>(null) }
    var showOwnerModal by remember { mutableStateOf(false) }
    var editingDag by remember { mutableStateOf<DagEntry?>(null) }
    var showDagModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.loadReferenceScanFromUri(context, uri) { err ->
                scope.launch { snackbarHostState.showSnackbar(err) }
            }
        }
    }

    if (showClearDialog) {
        ClearAllConfirmationDialog(
            strings = strings,
            onConfirm = {
                viewModel.clearAllData()
                showClearDialog = false
                activeTab = 0
                scope.launch { snackbarHostState.showSnackbar(strings.clearedSnackbar) }
            },
            onDismiss = { showClearDialog = false },
        )
    }

    if (showOwnerModal) {
        OwnerEditorDialog(
            initial = editingOwner,
            settings = settings,
            onSave = { entry ->
                viewModel.upsertOwner(entry)
                showOwnerModal = false
                editingOwner = null
            },
            onDismiss = {
                showOwnerModal = false
                editingOwner = null
            },
        )
    }

    if (showDagModal) {
        DagEditorDialog(
            initial = editingDag,
            commonClasses = schema.commonLandClassesBn,
            settings = settings,
            globalAutoCalculate = draft.autoCalculateLand,
            onSave = { entry ->
                viewModel.upsertDag(entry)
                showDagModal = false
                editingDag = null
            },
            onDismiss = {
                showDagModal = false
                editingDag = null
            },
        )
    }

    fun triggerPreviewOrPdf() {
        val isValid = viewModel.validateDraft()
        if (!isValid) {
            activeTab = 0
            scope.launch { snackbarHostState.showSnackbar(strings.validationFixPrompt) }
            return
        }
        viewModel.generatePreview(context)
        onOpenPreview()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBn) draft.surveyType.titleBn else draft.surveyType.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (isBn) draft.surveyType.eraLabelBn else draft.surveyType.eraLabelEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackHome,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("clear_all_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = strings.clearAll,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = strings.settings,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = { triggerPreviewOrPdf() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("preview_button"),
                    ) {
                        Icon(Icons.Outlined.Preview, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview", maxLines = 1)
                    }
                    Button(
                        onClick = { triggerPreviewOrPdf() },
                        modifier = Modifier
                            .weight(1.25f)
                            .height(48.dp)
                            .testTag("generate_pdf_button"),
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.generatePdf, maxLines = 1)
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val tabLabels = listOf(
                strings.tabBasic,
                "${strings.tabOwners} (${BengaliNumberUtils.formatScript("${draft.owners.size}", settings.useBengaliNumerals)})",
                "${strings.tabDags} (${BengaliNumberUtils.formatScript("${draft.dags.size}", settings.useBengaliNumerals)})",
                strings.tabOther,
            )

            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                tabLabels.forEachIndexed { index, label ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        modifier = Modifier.testTag("form_tab_$index"),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 820.dp),
            ) {
                when (activeTab) {
                    0 -> BasicInfoTab(draft, settings, schema.exclusiveBasicFields, viewModel)
                    1 -> OwnersTab(
                        draft = draft,
                        settings = settings,
                        onAddOwner = {
                            editingOwner = null
                            showOwnerModal = true
                        },
                        onEditOwner = { owner ->
                            editingOwner = owner
                            showOwnerModal = true
                        },
                        onDeleteOwner = { id -> viewModel.deleteOwner(id) },
                    )
                    2 -> DagsTab(
                        draft = draft,
                        settings = settings,
                        viewModel = viewModel,
                        onAddDag = {
                            editingDag = null
                            showDagModal = true
                        },
                        onEditDag = { dag ->
                            editingDag = dag
                            showDagModal = true
                        },
                        onDeleteDag = { id -> viewModel.deleteDag(id) },
                    )
                    3 -> OtherAndTemplateTab(
                        draft = draft,
                        settings = settings,
                        viewModel = viewModel,
                        onPickImage = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    )
                }
            }
        }
    }
}

private enum class ActiveLocationPicker {
    DIVISION, DISTRICT, THANA_UPAZILA, MOUZA_JL, PARGANA
}

@Composable
private fun BasicInfoTab(
    draft: KhatianDraftState,
    settings: SessionSettings,
    exclusiveFields: List<com.example.model.SurveyExtraFieldDef>,
    viewModel: KhatianViewModel,
) {
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI
    val b = draft.basicInfo
    val errs = draft.validation.fieldErrors
    var activePicker by remember { mutableStateOf<ActiveLocationPicker?>(null) }

    when (activePicker) {
        ActiveLocationPicker.DIVISION -> {
            SearchableLocationPickerDialog(
                title = if (isBn) "বিভাগ নির্বাচন করুন (সকল বিভাগ)" else "Select Division",
                items = BangladeshGeoData.allDivisions,
                selectedValue = b.division,
                isBengali = isBn,
                onSelect = { selected ->
                    viewModel.updateBasicInfo { cur ->
                        val sameDiv = cur.division.equals(selected, ignoreCase = true)
                        cur.copy(
                            division = selected,
                            district = if (sameDiv) cur.district else "",
                            upazila = if (sameDiv) cur.upazila else "",
                            thana = if (sameDiv) cur.thana else "",
                        )
                    }
                    activePicker = null
                },
                onDismiss = { activePicker = null },
            )
        }
        ActiveLocationPicker.DISTRICT -> {
            val filteredDistricts = BangladeshGeoData.districtsForDivision(b.division)
            val allDistricts = BangladeshGeoData.allDistrictNames()
            SearchableLocationPickerDialog(
                title = if (isBn) "জেলা নির্বাচন করুন" else "Select District",
                items = filteredDistricts,
                allItemsFallback = allDistricts,
                selectedValue = b.district,
                isBengali = isBn,
                onSelect = { selected ->
                    val autoDiv = BangladeshGeoData.findDivisionForDistrict(selected)
                    viewModel.updateBasicInfo { cur ->
                        val sameDist = cur.district.equals(selected, ignoreCase = true)
                        cur.copy(
                            division = autoDiv ?: cur.division,
                            district = selected,
                            upazila = if (sameDist) cur.upazila else "",
                            thana = if (sameDist) cur.thana else "",
                        )
                    }
                    activePicker = null
                },
                onDismiss = { activePicker = null },
            )
        }
        ActiveLocationPicker.THANA_UPAZILA -> {
            val filteredUpazilas = BangladeshGeoData.upazilasForDistrict(b.district, b.division)
            val allUpazilas = BangladeshGeoData.allUpazilaNames()
            SearchableLocationPickerDialog(
                title = if (isBn) "থানা / উপজেলা নির্বাচন করুন" else "Select Thana / Upazila",
                items = filteredUpazilas,
                allItemsFallback = allUpazilas,
                selectedValue = b.thana.ifBlank { b.upazila },
                isBengali = isBn,
                onSelect = { selected ->
                    val loc = BangladeshGeoData.findLocationForUpazila(selected)
                    viewModel.updateBasicInfo { cur ->
                        cur.copy(
                            division = cur.division.ifBlank { loc?.first.orEmpty() },
                            district = cur.district.ifBlank { loc?.second.orEmpty() },
                            thana = selected,
                            upazila = selected,
                        )
                    }
                    activePicker = null
                },
                onDismiss = { activePicker = null },
            )
        }
        ActiveLocationPicker.MOUZA_JL -> {
            val filteredMouzas = BangladeshGeoData.mouzasForLocation(b.district, b.upazila.ifBlank { b.thana })
            val allMouzas = BangladeshGeoData.allMouzas()
            MouzaJlPickerDialog(
                title = if (isBn) "মৌজা ও জে.এল. নং নির্বাচন করুন" else "Select Mouza & J.L. No.",
                surveyType = draft.surveyType,
                filteredEntries = filteredMouzas,
                allEntries = allMouzas,
                selectedMouza = b.mouzaName,
                selectedJl = b.jlNumber,
                isBengali = isBn,
                onSelectEntry = { entry ->
                    val autoDiv = BangladeshGeoData.findDivisionForDistrict(entry.district)
                    val matchedJl = entry.jlForSurvey(draft.surveyType)
                    viewModel.updateBasicInfo { cur ->
                        cur.copy(
                            division = cur.division.ifBlank { autoDiv.orEmpty() },
                            district = cur.district.ifBlank { entry.district },
                            upazila = cur.upazila.ifBlank { entry.upazila },
                            thana = cur.thana.ifBlank { entry.upazila },
                            mouzaName = entry.mouza,
                            jlNumber = matchedJl,
                        )
                    }
                    activePicker = null
                },
                onSelectCustom = { jl, mouza ->
                    viewModel.updateBasicInfo { cur ->
                        cur.copy(
                            jlNumber = jl,
                            mouzaName = mouza,
                        )
                    }
                    activePicker = null
                },
                onDismiss = { activePicker = null },
            )
        }
        ActiveLocationPicker.PARGANA -> {
            val parganas = BangladeshGeoData.parganasForLocation(
                divisionName = b.division,
                districtName = b.district,
                upazilaName = b.upazila.ifBlank { b.thana },
                mouzaName = b.mouzaName,
            )
            val allParganas = BangladeshGeoData.allParganaNames()
            SearchableLocationPickerDialog(
                title = if (isBn) "পরগণা নির্বাচন করুন" else "Select Pargana",
                items = parganas,
                allItemsFallback = allParganas,
                selectedValue = b.surveySpecificValues["pargana"].orEmpty(),
                isBengali = isBn,
                onSelect = { selected ->
                    viewModel.updateSurveySpecificField("pargana", selected)
                    activePicker = null
                },
                onDismiss = { activePicker = null },
            )
        }
        null -> Unit
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { DraftDisclaimerBanner(text = strings.draftDisclaimerShort) }

        if (draft.validation.generalWarnings.isNotEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        draft.validation.generalWarnings.forEach { warn ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(warn, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionCard(
                title = if (isBn) "প্রশাসনিক ও মৌলিক তথ্য (${draft.surveyType.code})" else "Administrative & Core Details (${draft.surveyType.code})",
                subtitle = if (isBn) draft.surveyType.formRefBn else draft.surveyType.legalProvisionEn,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TappableLocationField(
                        value = b.division,
                        label = strings.division,
                        placeholder = if (isBn) "বিভাগ বাছুন" else "Tap to select",
                        onClick = { activePicker = ActiveLocationPicker.DIVISION },
                        modifier = Modifier.weight(1f),
                        testTag = "input_division",
                    )
                    TappableLocationField(
                        value = b.district,
                        label = strings.district,
                        placeholder = if (isBn) "জেলা বাছুন" else "Tap to select",
                        errorText = errs["district"],
                        onClick = { activePicker = ActiveLocationPicker.DISTRICT },
                        modifier = Modifier.weight(1f),
                        testTag = "input_district",
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TappableLocationField(
                        value = b.thana.ifBlank { b.upazila },
                        label = strings.thanaUpazila,
                        placeholder = if (isBn) "থানা / উপজেলা বাছুন" else "Select Thana / Upazila",
                        onClick = { activePicker = ActiveLocationPicker.THANA_UPAZILA },
                        modifier = Modifier.weight(1f),
                        testTag = "input_thana_upazila",
                    )
                    KhatianTextField(
                        value = b.unionName,
                        onValueChange = { v -> viewModel.updateBasicInfo { it.copy(unionName = v) } },
                        label = strings.unionName,
                        modifier = Modifier.weight(1f),
                        testTag = "input_union",
                    )
                }
                val combinedMouzaJl = listOf(b.jlNumber.trim(), b.mouzaName.trim())
                    .filter { it.isNotEmpty() }
                    .joinToString(" - ")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TappableLocationField(
                        value = combinedMouzaJl,
                        label = strings.mouzaAndJl,
                        placeholder = if (isBn) "৭০ - সতিপুরী" else "e.g. 70 - Satipuri",
                        errorText = errs["mouzaName"] ?: errs["jlNumber"],
                        onClick = { activePicker = ActiveLocationPicker.MOUZA_JL },
                        modifier = Modifier.weight(1.25f),
                        testTag = "input_mouza_jl",
                    )
                    KhatianTextField(
                        value = b.khatianNumber,
                        onValueChange = { v -> viewModel.updateBasicInfo { it.copy(khatianNumber = v) } },
                        label = strings.khatianNumber,
                        errorText = errs["khatianNumber"],
                        modifier = Modifier.weight(0.75f),
                        testTag = "input_khatian_no",
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KhatianTextField(
                        value = b.surveyName,
                        onValueChange = { v -> viewModel.updateBasicInfo { it.copy(surveyName = v) } },
                        label = strings.surveyName,
                        modifier = Modifier.weight(1.3f),
                        testTag = "input_survey_name",
                    )
                    KhatianTextField(
                        value = b.pageNumber,
                        onValueChange = { v -> viewModel.updateBasicInfo { it.copy(pageNumber = v) } },
                        label = strings.pageNumber,
                        modifier = Modifier.weight(0.7f),
                        testTag = "input_page_no",
                    )
                }
            }
        }

        when (draft.surveyType) {
            SurveyType.CS -> {
                val ex = b.surveySpecificValues
                item {
                    SectionCard(
                        title = if (isBn) "সি.এস. ফরম — পরগণা, রে.সা. ও তৌজি" else "CS Form — Pargana, Re-Sa & Touzi",
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TappableLocationField(
                                value = ex["pargana"].orEmpty(),
                                label = if (isBn) "পরগণা" else "Pargana",
                                placeholder = if (isBn) "পরগণা বাছুন" else "Select Pargana",
                                onClick = { activePicker = ActiveLocationPicker.PARGANA },
                                modifier = Modifier.weight(1f),
                                testTag = "input_pargana",
                            )
                            KhatianTextField(
                                value = ex["resa_no"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("resa_no", it) },
                                label = "রেঃ সাঃ নং",
                                modifier = Modifier.weight(1f),
                            )
                            KhatianTextField(
                                value = ex["touzi_no"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("touzi_no", it) },
                                label = "তৌজি নং",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                item {
                    SectionCard(
                        title = "উপরিস্থ স্বত্বের ও অত্র স্বত্বের দেয় (সি.এস. ছক)",
                    ) {
                        KhatianTextField(
                            value = ex["superior_khatian_occupant"].orEmpty(),
                            onValueChange = { viewModel.updateSurveySpecificField("superior_khatian_occupant", it) },
                            label = "উপরিস্থ স্বত্বের — দখলকার সংক্ষিপ্ত",
                            placeholder = "যেমন: ২৭৫ চন্দ্রহরি বেপারী গং",
                            singleLine = false,
                            minLines = 2,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KhatianTextField(
                                value = ex["superior_mutual_share"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("superior_mutual_share", it) },
                                label = "পরস্পর অংশ",
                                placeholder = "১১",
                                modifier = Modifier.weight(1f),
                            )
                            KhatianTextField(
                                value = ex["own_dey_khajana"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("own_dey_khajana", it) },
                                label = "অত্র স্বত্বের দেয়: খাজানা",
                                modifier = Modifier.weight(1f),
                            )
                            KhatianTextField(
                                value = ex["own_dey_cess"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("own_dey_cess", it) },
                                label = "সেস্",
                                modifier = Modifier.weight(0.8f),
                            )
                        }
                        KhatianTextField(
                            value = ex["upper_remarks"].orEmpty(),
                            onValueChange = { viewModel.updateSurveySpecificField("upper_remarks", it) },
                            label = "মন্তব্য (উপরিস্থ ছক)",
                            placeholder = "যেমন: সামিল খাজানা ২৭০ নং খতিয়ান, মোট খাজানা ১৪৬/৬",
                            singleLine = false,
                            minLines = 2,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KhatianTextField(
                                value = ex["dharamote_son_khajana"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("dharamote_son_khajana", it) },
                                label = "ধারামতে ও কোন সন হইতে: খাজানা",
                                modifier = Modifier.weight(1f),
                            )
                            KhatianTextField(
                                value = ex["dharamote_son_cess"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("dharamote_son_cess", it) },
                                label = "সেস্",
                                modifier = Modifier.weight(0.8f),
                            )
                        }
                    }
                }
                item {
                    SectionCard(
                        title = "অত্র স্বত্বের বিবরণ, শ্রেণী ও পরিবর্তন",
                        subtitle = "মালিক ট্যাবে মূল দখলকার যুক্ত করুন এবং নিচে ২য় কলাম ও শ্রেণী লিখুন",
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KhatianTextField(
                                value = ex["second_occupant_details"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("second_occupant_details", it) },
                                label = "২য় কলাম: অত্র স্বত্বের বিবরণ ও দখলকার",
                                singleLine = false,
                                minLines = 2,
                                modifier = Modifier.weight(1.4f),
                            )
                            KhatianTextField(
                                value = ex["second_occupant_share"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("second_occupant_share", it) },
                                label = "অংশ",
                                modifier = Modifier.weight(0.6f),
                            )
                        }
                        KhatianTextField(
                            value = ex["tenure_class_rules"].orEmpty(),
                            onValueChange = { viewModel.updateSurveySpecificField("tenure_class_rules", it) },
                            label = "অত্র স্বত্বের শ্রেণী এবং বিশেষ নিয়ম ও অনুসঙ্গ",
                            placeholder = "যেমন: রায়ত / স্থিতিবান",
                            singleLine = false,
                            minLines = 2,
                        )
                        KhatianTextField(
                            value = ex["case_change_note"].orEmpty(),
                            onValueChange = { viewModel.updateSurveySpecificField("case_change_note", it) },
                            label = "ধারা মতে পরিবর্তন ও মোকদ্দমা নম্বর",
                            singleLine = false,
                            minLines = 2,
                        )
                    }
                }
            }

            SurveyType.SA -> {
                val ex = b.surveySpecificValues
                item {
                    SectionCard(
                        title = if (isBn) "এস.এ. ফরম — পরগণা, রে.সা. ও তৌজি" else "SA Form — Pargana, Re-Sa & Touzi",
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TappableLocationField(
                                value = ex["pargana"].orEmpty(),
                                label = if (isBn) "পরগণা" else "Pargana",
                                placeholder = if (isBn) "পরগণা বাছুন" else "Select Pargana",
                                onClick = { activePicker = ActiveLocationPicker.PARGANA },
                                modifier = Modifier.weight(1f),
                                testTag = "input_pargana",
                            )
                            KhatianTextField(
                                value = ex["resa_no"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("resa_no", it) },
                                label = "রেঃ সাঃ নং",
                                modifier = Modifier.weight(1f),
                            )
                            KhatianTextField(
                                value = ex["touzi_no"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("touzi_no", it) },
                                label = "তৌজি নং",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                item {
                    SectionCard(
                        title = "উপরিস্থ স্বত্বের ও অত্র স্বত্বের দেয় (এস.এ. ছক)",
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KhatianTextField(
                                value = ex["superior_khatian_batta"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("superior_khatian_batta", it) },
                                label = "খতিয়ান নং মায় বাটা",
                                placeholder = "৮৯",
                                modifier = Modifier.weight(0.8f),
                            )
                            KhatianTextField(
                                value = ex["superior_mutual_share"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("superior_mutual_share", it) },
                                label = "পরস্পর অংশ",
                                placeholder = "১১",
                                modifier = Modifier.weight(0.8f),
                            )
                        }
                        KhatianTextField(
                            value = ex["superior_khatian_occupant"].orEmpty(),
                            onValueChange = { viewModel.updateSurveySpecificField("superior_khatian_occupant", it) },
                            label = "বিবরণ ও দখলকার [সংক্ষিপ্ত]",
                            placeholder = "যেমন: সোনা ভান বেওয়া",
                            singleLine = false,
                            minLines = 2,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KhatianTextField(
                                value = ex["own_dey_khajana"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("own_dey_khajana", it) },
                                label = "খাজানা",
                                placeholder = "৬।।০",
                                modifier = Modifier.weight(1f),
                            )
                            KhatianTextField(
                                value = ex["own_dey_cess"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("own_dey_cess", it) },
                                label = "সেস",
                                placeholder = "১/০",
                                modifier = Modifier.weight(1f),
                            )
                            KhatianTextField(
                                value = ex["own_dey_edu_cess"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("own_dey_edu_cess", it) },
                                label = "শিক্ষা সেস",
                                placeholder = "১/৯",
                                modifier = Modifier.weight(1f),
                            )
                        }
                        KhatianTextField(
                            value = ex["upper_remarks"].orEmpty(),
                            onValueChange = { viewModel.updateSurveySpecificField("upper_remarks", it) },
                            label = "মন্তব্য (উপরিস্থ ছক)",
                            singleLine = false,
                            minLines = 2,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KhatianTextField(
                                value = ex["dharamote_date_khajana"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("dharamote_date_khajana", it) },
                                label = "ধারামতে ও কোন তারিখ হইতে: খাজানা",
                                modifier = Modifier.weight(1f),
                            )
                            KhatianTextField(
                                value = ex["dharamote_date_cess"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("dharamote_date_cess", it) },
                                label = "সেস্",
                                modifier = Modifier.weight(0.8f),
                            )
                        }
                    }
                }
                item {
                    SectionCard(
                        title = "অত্র স্বত্বের বিবরণ, শ্রেণী ও ধারামতে নোট",
                        subtitle = "মালিক ট্যাবে মূল দখলকার যুক্ত করুন এবং নিচে ২য় কলাম ও শ্রেণী লিখুন",
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KhatianTextField(
                                value = ex["second_occupant_details"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("second_occupant_details", it) },
                                label = "২য় কলাম: অত্র স্বত্বের বিবরণ ও দখলকার",
                                singleLine = false,
                                minLines = 2,
                                modifier = Modifier.weight(1.4f),
                            )
                            KhatianTextField(
                                value = ex["second_occupant_share"].orEmpty(),
                                onValueChange = { viewModel.updateSurveySpecificField("second_occupant_share", it) },
                                label = "অংশ",
                                modifier = Modifier.weight(0.6f),
                            )
                        }
                        KhatianTextField(
                            value = ex["tenure_class_rules"].orEmpty(),
                            onValueChange = { viewModel.updateSurveySpecificField("tenure_class_rules", it) },
                            label = "অত্র স্বত্বের শ্রেণী (এবং বিশেষ নিয়ম ও অনুসঙ্গ)",
                            placeholder = "যেমন: কোর্ফা",
                            singleLine = false,
                            minLines = 2,
                        )
                        KhatianTextField(
                            value = ex["case_change_note"].orEmpty(),
                            onValueChange = { viewModel.updateSurveySpecificField("case_change_note", it) },
                            label = "ধারামতে নোট বা পরিবর্তন [মায় মোকদ্দমা নং এবং সন]",
                            singleLine = false,
                            minLines = 2,
                        )
                    }
                }
            }

            else -> {
                if (exclusiveFields.isNotEmpty()) {
                    item {
                        SectionCard(
                            title = "${strings.surveyExclusiveSection} (${draft.surveyType.code})",
                            subtitle = if (isBn) draft.surveyType.legalProvisionBn else draft.surveyType.legalProvisionEn,
                        ) {
                            exclusiveFields.forEach { def ->
                                KhatianTextField(
                                    value = b.surveySpecificValues[def.key] ?: "",
                                    onValueChange = { v -> viewModel.updateSurveySpecificField(def.key, v) },
                                    label = if (isBn) def.labelBn else def.labelEn,
                                    placeholder = if (isBn) def.placeholderBn else def.placeholderEn,
                                    singleLine = !def.isMultiline,
                                    minLines = if (def.isMultiline) 2 else 1,
                                    testTag = "input_extra_${def.key}",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnersTab(
    draft: KhatianDraftState,
    settings: SessionSettings,
    onAddOwner: () -> Unit,
    onEditOwner: (OwnerEntry) -> Unit,
    onDeleteOwner: (String) -> Unit,
) {
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI
    val useBnNums = settings.useBengaliNumerals

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Button(
                onClick = onAddOwner,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_owner_button"),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.addOwner)
            }
        }

        if (draft.owners.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                ) {
                    Text(
                        text = strings.emptyOwnersMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
        } else {
            items(draft.owners, key = { it.id }) { owner ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = if (isBn) owner.category.labelBn else owner.category.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                            Text(
                                text = owner.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            if (owner.fatherOrHusbandName.isNotBlank()) {
                                Text(
                                    text = "${if (isBn) "পিতা/স্বামী" else "Father/Husband"}: ${owner.fatherOrHusbandName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (owner.address.isNotBlank()) {
                                Text(
                                    text = owner.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (owner.hissa.isNotBlank()) {
                                    Text(
                                        text = "${if (isBn) "হিস্যা" else "Hissa"}: ${BengaliNumberUtils.formatScript(owner.hissa, useBnNums)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                if (owner.landAmount.isNotBlank()) {
                                    Text(
                                        text = "${BengaliNumberUtils.formatScript(owner.landAmount, useBnNums)} ${if (isBn) owner.landUnit.labelBn else owner.landUnit.labelEn}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                        }
                        Row {
                            IconButton(
                                onClick = { onEditOwner(owner) },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit Owner")
                            }
                            IconButton(
                                onClick = { onDeleteOwner(owner.id) },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete Owner",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DagsTab(
    draft: KhatianDraftState,
    settings: SessionSettings,
    viewModel: KhatianViewModel,
    onAddDag: () -> Unit,
    onEditDag: (DagEntry) -> Unit,
    onDeleteDag: (String) -> Unit,
) {
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI
    val useBnNums = settings.useBengaliNumerals

    var calcAmount by remember { mutableStateOf(if (useBnNums) "১০" else "10") }
    var calcSourceUnit by remember { mutableStateOf(LandUnit.SHOTOK) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Quick Unit Converter & Auto-Calc Switch
        item {
            SectionCard(
                title = strings.unitConverterTitle,
                subtitle = strings.manualOverrideHint,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Outlined.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.autoCalculateLabel, style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = draft.autoCalculateLand,
                        onCheckedChange = { viewModel.setAutoCalculateLand(it) },
                    )
                }
                HorizontalDivider()
                KhatianTextField(
                    value = calcAmount,
                    onValueChange = { calcAmount = it },
                    label = if (isBn) "রূপান্তরের জন্য পরিমাণ লিখুন" else "Amount to convert",
                    testTag = "unit_converter_input",
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LandUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = calcSourceUnit == unit,
                            onClick = { calcSourceUnit = unit },
                            label = { Text(if (isBn) unit.labelBn else unit.labelEn) },
                        )
                    }
                }
                val parsed = BengaliNumberUtils.parseDoubleOrNull(calcAmount) ?: 0.0
                val shotokBase = LandCalculationEngine.toShotok(parsed, calcSourceUnit)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LandUnit.entries.forEach { target ->
                        val converted = LandCalculationEngine.fromShotok(shotokBase, target)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = if (isBn) target.labelBn else target.labelEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = BengaliNumberUtils.formatDecimal(converted, 4, useBnNums),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onAddDag,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_dag_button"),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.addDag)
            }
        }

        if (draft.dags.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                ) {
                    Text(
                        text = strings.emptyDagsMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
        } else {
            items(draft.dags, key = { it.id }) { dag ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "${if (isBn) "দাগ নং" else "Dag"}: ${BengaliNumberUtils.formatScript(dag.dagNumber, useBnNums)} (${dag.landClass.ifBlank { "-" }})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            val unitLbl = if (isBn) dag.landUnit.labelBn else dag.landUnit.labelEn
                            Text(
                                text = "${if (isBn) "মোট" else "Total"}: ${BengaliNumberUtils.formatScript(dag.totalDagArea.ifBlank { "-" }, useBnNums)} $unitLbl  |  ${if (isBn) "প্রাপ্য" else "Entitled"}: ${BengaliNumberUtils.formatScript(dag.entitledArea.ifBlank { "-" }, useBnNums)} $unitLbl",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (dag.remarks.isNotBlank()) {
                                Text(
                                    text = dag.remarks,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Row {
                            IconButton(
                                onClick = { onEditDag(dag) },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit Dag")
                            }
                            IconButton(
                                onClick = { onDeleteDag(dag.id) },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete Dag",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtherAndTemplateTab(
    draft: KhatianDraftState,
    settings: SessionSettings,
    viewModel: KhatianViewModel,
    onPickImage: () -> Unit,
) {
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI
    val extra = draft.extraInfo
    val cfg = draft.customConfig

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionCard(
                title = if (isBn) "রাজস্ব, চৌহদ্দি ও বিশেষ মন্তব্য" else "Revenue, Boundaries & Notes",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KhatianTextField(
                        value = extra.revenueAmount,
                        onValueChange = { v -> viewModel.updateExtraInfo { it.copy(revenueAmount = v) } },
                        label = strings.revenueLabel,
                        modifier = Modifier.weight(1f),
                        testTag = "input_revenue",
                    )
                    KhatianTextField(
                        value = extra.cessAmount,
                        onValueChange = { v -> viewModel.updateExtraInfo { it.copy(cessAmount = v) } },
                        label = strings.cessLabel,
                        modifier = Modifier.weight(1f),
                        testTag = "input_cess",
                    )
                }
                Text(strings.boundarySection, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KhatianTextField(
                        value = extra.boundaryNorth,
                        onValueChange = { v -> viewModel.updateExtraInfo { it.copy(boundaryNorth = v) } },
                        label = strings.north,
                        modifier = Modifier.weight(1f),
                    )
                    KhatianTextField(
                        value = extra.boundarySouth,
                        onValueChange = { v -> viewModel.updateExtraInfo { it.copy(boundarySouth = v) } },
                        label = strings.south,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KhatianTextField(
                        value = extra.boundaryEast,
                        onValueChange = { v -> viewModel.updateExtraInfo { it.copy(boundaryEast = v) } },
                        label = strings.east,
                        modifier = Modifier.weight(1f),
                    )
                    KhatianTextField(
                        value = extra.boundaryWest,
                        onValueChange = { v -> viewModel.updateExtraInfo { it.copy(boundaryWest = v) } },
                        label = strings.west,
                        modifier = Modifier.weight(1f),
                    )
                }
                KhatianTextField(
                    value = extra.specialNotes,
                    onValueChange = { v -> viewModel.updateExtraInfo { it.copy(specialNotes = v) } },
                    label = strings.specialNotes,
                    singleLine = false,
                    minLines = 3,
                    testTag = "input_special_notes",
                )
            }
        }

        // Reference Scan Uploader (RAM-Only via PickVisualMedia)
        item {
            SectionCard(
                title = strings.referenceScanTitle,
                subtitle = if (isBn) {
                    "মূল খতিয়ানের স্ক্যান ছবি আপলোড করে দেখে দেখে তথ্য বা কাস্টম টেমপ্লেট সাজাতে পারেন (স্থায়ীভাবে সংরক্ষিত হয় না)।"
                } else {
                    "Pick a reference Khatian scan to inspect while drafting (kept in temporary RAM only)."
                },
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onPickImage,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pick_reference_image_button"),
                    ) {
                        Icon(Icons.Outlined.ImageSearch, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.uploadSampleImage, maxLines = 1)
                    }
                    if (extra.referenceScanBitmap != null) {
                        OutlinedButton(
                            onClick = { viewModel.removeReferenceScan() },
                            modifier = Modifier.height(48.dp),
                        ) {
                            Text(strings.removeImage)
                        }
                    }
                }
                extra.referenceScanBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Reference Khatian Scan",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small),
                    )
                }
            }
        }

        // Custom Template Builder (shown when SurveyType.CUSTOM is active)
        if (draft.surveyType == SurveyType.CUSTOM) {
            item {
                SectionCard(
                    title = strings.customTemplateBuilderTitle,
                    subtitle = strings.customTemplateNotice,
                ) {
                    KhatianTextField(
                        value = cfg.customSurveyTitle,
                        onValueChange = { v -> viewModel.updateCustomConfig { it.copy(customSurveyTitle = v) } },
                        label = if (isBn) "কাস্টম খতিয়ান শিরোনাম" else "Custom Template Heading",
                        testTag = "custom_title_input",
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (isBn) "অনুভূমিক (Landscape) টেমপ্লেট" else "Landscape Orientation",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Switch(
                            checked = cfg.useLandscape,
                            onCheckedChange = { v -> viewModel.updateCustomConfig { it.copy(useLandscape = v) } },
                        )
                    }
                    ColumnToggleRow(
                        label = if (isBn) "স্বত্ব / রেফারেন্স কলাম দেখান" else "Show Tenure / Ref Column",
                        checked = cfg.showSuperiorTenureColumn,
                        onCheckedChange = { v -> viewModel.updateCustomConfig { it.copy(showSuperiorTenureColumn = v) } },
                    )
                    ColumnToggleRow(
                        label = if (isBn) "রাজস্ব / কর কলাম দেখান" else "Show Revenue Column",
                        checked = cfg.showRevenueColumn,
                        onCheckedChange = { v -> viewModel.updateCustomConfig { it.copy(showRevenueColumn = v) } },
                    )
                    ColumnToggleRow(
                        label = if (isBn) "জমির শ্রেণি কলাম দেখান" else "Show Land Class Column",
                        checked = cfg.showLandClassColumn,
                        onCheckedChange = { v -> viewModel.updateCustomConfig { it.copy(showLandClassColumn = v) } },
                    )
                    ColumnToggleRow(
                        label = if (isBn) "মন্তব্য কলাম দেখান" else "Show Remarks Column",
                        checked = cfg.showRemarksColumn,
                        onCheckedChange = { v -> viewModel.updateCustomConfig { it.copy(showRemarksColumn = v) } },
                    )
                    KhatianTextField(
                        value = cfg.ownerColumnHeader,
                        onValueChange = { v -> viewModel.updateCustomConfig { it.copy(ownerColumnHeader = v) } },
                        label = if (isBn) "মালিকের কলাম শিরোনাম" else "Owner Column Header",
                    )
                    KhatianTextField(
                        value = cfg.hissaColumnHeader,
                        onValueChange = { v -> viewModel.updateCustomConfig { it.copy(hissaColumnHeader = v) } },
                        label = if (isBn) "হিস্যার কলাম শিরোনাম" else "Hissa Column Header",
                    )
                    KhatianTextField(
                        value = cfg.dagColumnHeader,
                        onValueChange = { v -> viewModel.updateCustomConfig { it.copy(dagColumnHeader = v) } },
                        label = if (isBn) "দাগের কলাম শিরোনাম" else "Dag Column Header",
                    )
                    KhatianTextField(
                        value = cfg.areaColumnHeader,
                        onValueChange = { v -> viewModel.updateCustomConfig { it.copy(areaColumnHeader = v) } },
                        label = if (isBn) "জমির পরিমাণ কলাম শিরোনাম" else "Area Column Header",
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
