package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppLanguage
import com.example.model.BasicInfo
import com.example.model.CustomTemplateConfig
import com.example.model.DagEntry
import com.example.model.ExtraSurveyInfo
import com.example.model.KhatianDraftState
import com.example.model.OwnerEntry
import com.example.model.PageSizeOption
import com.example.model.PdfQuality
import com.example.model.PrintOrientationPref
import com.example.model.SessionSettings
import com.example.model.SurveyType
import com.example.model.ValidationSummary
import com.example.pdf.KhatianPdfEngine
import com.example.pdf.KhatianPrintAdapter
import com.example.util.BengaliNumberUtils
import com.example.util.LandCalculationEngine
import com.example.util.LandUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface PreviewUiState {
    data object Idle : PreviewUiState
    data object Loading : PreviewUiState
    data class Ready(val pages: List<Bitmap>) : PreviewUiState
    data class Error(val message: String) : PreviewUiState
}

class KhatianViewModel : ViewModel() {

    private val _settings = MutableStateFlow(SessionSettings())
    val settings: StateFlow<SessionSettings> = _settings.asStateFlow()

    private val _draft = MutableStateFlow(KhatianDraftState())
    val draft: StateFlow<KhatianDraftState> = _draft.asStateFlow()

    private val _previewState = MutableStateFlow<PreviewUiState>(PreviewUiState.Idle)
    val previewState: StateFlow<PreviewUiState> = _previewState.asStateFlow()

    fun selectSurvey(type: SurveyType) {
        releaseScanBitmap()
        clearPreviewBitmaps()
        val lang = _settings.value.language
        val defaultSurveyName = if (lang == AppLanguage.BENGALI) type.titleBn else type.titleEn
        val defaultPageNo = if (_settings.value.useBengaliNumerals) "১" else "1"
        _draft.value = KhatianDraftState(
            surveyType = type,
            basicInfo = BasicInfo(
                surveyName = defaultSurveyName,
                pageNumber = defaultPageNo,
            ),
            customConfig = CustomTemplateConfig(
                useLandscape = type.isLandscapeNative,
            ),
        )
    }

    fun updateBasicInfo(transform: (BasicInfo) -> BasicInfo) {
        _draft.update { state ->
            val updated = transform(state.basicInfo)
            state.copy(
                basicInfo = updated,
                validation = state.validation.copy(
                    fieldErrors = state.validation.fieldErrors - setOf(
                        "district", "mouzaName", "jlNumber", "khatianNumber"
                    )
                ),
            )
        }
    }

    fun updateSurveySpecificField(key: String, value: String) {
        _draft.update { state ->
            val updatedMap = state.basicInfo.surveySpecificValues.toMutableMap()
            updatedMap[key] = value
            state.copy(
                basicInfo = state.basicInfo.copy(surveySpecificValues = updatedMap)
            )
        }
    }

    fun upsertOwner(entry: OwnerEntry) {
        _draft.update { state ->
            val existingIdx = state.owners.indexOfFirst { it.id == entry.id }
            val updatedList = if (existingIdx >= 0) {
                state.owners.toMutableList().apply { set(existingIdx, entry) }
            } else {
                state.owners + entry
            }
            state.copy(owners = updatedList)
        }
    }

    fun deleteOwner(id: String) {
        _draft.update { state ->
            state.copy(owners = state.owners.filterNot { it.id == id })
        }
    }

    fun upsertDag(entry: DagEntry) {
        _draft.update { state ->
            val existingIdx = state.dags.indexOfFirst { it.id == entry.id }
            val updatedList = if (existingIdx >= 0) {
                state.dags.toMutableList().apply { set(existingIdx, entry) }
            } else {
                state.dags + entry
            }
            state.copy(dags = updatedList)
        }
    }

    fun deleteDag(id: String) {
        _draft.update { state ->
            state.copy(dags = state.dags.filterNot { it.id == id })
        }
    }

    fun updateExtraInfo(transform: (ExtraSurveyInfo) -> ExtraSurveyInfo) {
        _draft.update { it.copy(extraInfo = transform(it.extraInfo)) }
    }

    fun updateCustomConfig(transform: (CustomTemplateConfig) -> CustomTemplateConfig) {
        _draft.update { it.copy(customConfig = transform(it.customConfig)) }
    }

    fun setAutoCalculateLand(enabled: Boolean) {
        _draft.update { it.copy(autoCalculateLand = enabled) }
    }

    fun setDisplayUnit(unit: LandUnit) {
        _draft.update { it.copy(displayUnit = unit) }
    }

    fun loadReferenceScanFromUri(context: Context, uri: Uri, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val decoded = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeStream(stream, null, options)
                        var sample = 1
                        val maxDim = maxOf(options.outWidth, options.outHeight)
                        while (maxDim / sample > 1600) {
                            sample *= 2
                        }
                        context.contentResolver.openInputStream(uri)?.use { second ->
                            val decodeOpts = BitmapFactory.Options().apply {
                                inSampleSize = sample
                            }
                            BitmapFactory.decodeStream(second, null, decodeOpts)
                        }
                    }
                }
                if (decoded != null) {
                    releaseScanBitmap()
                    _draft.update {
                        it.copy(
                            extraInfo = it.extraInfo.copy(
                                referenceScanBitmap = decoded,
                                referenceScanLabel = uri.lastPathSegment ?: "reference_scan",
                            )
                        )
                    }
                } else {
                    onError("Unable to decode image")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to load reference image")
            }
        }
    }

    fun removeReferenceScan() {
        releaseScanBitmap()
        _draft.update {
            it.copy(
                extraInfo = it.extraInfo.copy(
                    referenceScanBitmap = null,
                    referenceScanLabel = "",
                )
            )
        }
    }

    fun validateDraft(): Boolean {
        val state = _draft.value
        val lang = _settings.value.language
        val errors = mutableMapOf<String, String>()
        val warnings = mutableListOf<String>()

        if (state.basicInfo.district.isBlank()) {
            errors["district"] = if (lang == AppLanguage.BENGALI) "জেলার নাম আবশ্যক" else "District is required"
        }
        if (state.basicInfo.mouzaName.isBlank()) {
            errors["mouzaName"] = if (lang == AppLanguage.BENGALI) "মৌজার নাম আবশ্যক" else "Mouza name is required"
        }
        if (state.basicInfo.jlNumber.isBlank()) {
            errors["jlNumber"] = if (lang == AppLanguage.BENGALI) "জে.এল. নম্বর আবশ্যক" else "J.L. No. is required"
        }
        if (state.basicInfo.khatianNumber.isBlank()) {
            errors["khatianNumber"] = if (lang == AppLanguage.BENGALI) "খতিয়ান নম্বর আবশ্যক" else "Khatian No. is required"
        }

        if (state.owners.isEmpty()) {
            warnings.add(
                if (lang == AppLanguage.BENGALI) "কমপক্ষে একজন মালিক যুক্ত করলে খতিয়ান সম্পূর্ণ হবে"
                else "Add at least one owner for a complete Khatian"
            )
        }
        if (state.dags.isEmpty()) {
            warnings.add(
                if (lang == AppLanguage.BENGALI) "কমপক্ষে একটি দাগ ও জমির পরিমাণ যুক্ত করুন"
                else "Add at least one Dag/Plot entry"
            )
        }

        // Check numeric sanity in Dags
        state.dags.forEach { dag ->
            val tot = BengaliNumberUtils.parseDoubleOrNull(dag.totalDagArea)
            val ent = BengaliNumberUtils.parseDoubleOrNull(dag.entitledArea)
            if ((tot != null && tot < 0.0) || (ent != null && ent < 0.0)) {
                errors["dag_${dag.id}"] = if (lang == AppLanguage.BENGALI) {
                    "দাগ ${dag.dagNumber}-এ ভুল জমির পরিমাণ"
                } else {
                    "Invalid negative land area in Dag ${dag.dagNumber}"
                }
            }
            if (tot != null && ent != null && ent > tot + 0.0001) {
                warnings.add(
                    if (lang == AppLanguage.BENGALI) {
                        "দাগ ${dag.dagNumber}: প্রাপ্য জমি দাগের মোট জমির চেয়ে বেশি"
                    } else {
                        "Dag ${dag.dagNumber}: Entitled area exceeds total plot area"
                    }
                )
            }
        }

        // Check total Hissa across owners if numeric
        val hissaSum = state.owners.mapNotNull { LandCalculationEngine.parseHissaFraction(it.hissa) }.sum()
        if (hissaSum > 1.001) {
            warnings.add(
                if (lang == AppLanguage.BENGALI) {
                    "মালিকদের মোট হিস্যা ১.০০ (১৬ আনা) এর বেশি হয়েছে"
                } else {
                    "Combined owner Hissa exceeds 1.0 (100%)"
                }
            )
        }

        _draft.update {
            it.copy(validation = ValidationSummary(fieldErrors = errors, generalWarnings = warnings))
        }
        return errors.isEmpty()
    }

    fun clearAllData() {
        val currentSurvey = _draft.value.surveyType
        selectSurvey(currentSurvey)
    }

    fun updateLanguage(language: AppLanguage) {
        _settings.update { it.copy(language = language) }
    }

    fun updateUseBengaliNumerals(enabled: Boolean) {
        _settings.update { it.copy(useBengaliNumerals = enabled) }
    }

    fun updateDefaultPageSize(size: PageSizeOption) {
        _settings.update { it.copy(defaultPageSize = size) }
    }

    fun updatePdfQuality(quality: PdfQuality) {
        _settings.update { it.copy(pdfQuality = quality) }
    }

    fun updatePrintOrientationPref(pref: PrintOrientationPref) {
        _settings.update { it.copy(printOrientationPref = pref) }
    }

    fun updatePrintInkSaver(enabled: Boolean) {
        _settings.update { it.copy(printInkSaverGrayscale = enabled) }
    }

    fun generatePreview(context: Context) {
        val snapshotDraft = _draft.value
        val snapshotSettings = _settings.value
        clearPreviewBitmaps()
        _previewState.value = PreviewUiState.Loading
        viewModelScope.launch {
            try {
                val pages = withContext(Dispatchers.Default) {
                    KhatianPdfEngine(context.applicationContext)
                        .renderPreviewBitmaps(snapshotDraft, snapshotSettings)
                }
                _previewState.value = PreviewUiState.Ready(pages)
            } catch (e: Exception) {
                _previewState.value = PreviewUiState.Error(
                    e.message ?: "Failed to render Khatian preview"
                )
            }
        }
    }

    fun exportPdfToUri(
        context: Context,
        uri: Uri,
        onResult: (Boolean, String?) -> Unit,
    ) {
        val snapshotDraft = _draft.value
        val snapshotSettings = _settings.value
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val os = context.contentResolver.openOutputStream(uri)
                        ?: throw IllegalStateException("Cannot open selected file URI for writing")
                    os.use { stream ->
                        KhatianPdfEngine(context.applicationContext)
                            .writePdfToOutputStream(snapshotDraft, snapshotSettings, stream)
                    }
                }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Unable to write PDF file")
            }
        }
    }

    fun launchPrintJob(context: Context, onStarted: () -> Unit, onError: (String) -> Unit) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                onError("Android Print Service unavailable on this device")
                return
            }
            val currentDraft = _draft.value
            val currentSettings = _settings.value
            val jobName = "Khatian_${currentDraft.surveyType.code}_${currentDraft.basicInfo.khatianNumber.ifBlank { "Draft" }}"
            val adapter = KhatianPrintAdapter(context, currentDraft, currentSettings, jobName)
            val dpi = currentSettings.pdfQuality.previewDpi
            val attrsBuilder = PrintAttributes.Builder()
                .setResolution(PrintAttributes.Resolution("khatian_${dpi}dpi", "$dpi DPI", dpi, dpi))
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            printManager.print(jobName, adapter, attrsBuilder.build())
            onStarted()
        } catch (e: Exception) {
            onError(e.message ?: "Failed to start print service")
        }
    }

    private fun releaseScanBitmap() {
        _draft.value.extraInfo.referenceScanBitmap?.let { bmp ->
            if (!bmp.isRecycled) bmp.recycle()
        }
    }

    fun clearPreviewBitmaps() {
        val current = _previewState.value
        if (current is PreviewUiState.Ready) {
            current.pages.forEach { bmp ->
                if (!bmp.isRecycled) bmp.recycle()
            }
        }
        _previewState.value = PreviewUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        releaseScanBitmap()
        clearPreviewBitmaps()
    }
}
