package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.text.Layout
import com.example.model.DagEntry
import com.example.model.KhatianDraftState
import com.example.model.OwnerEntry
import com.example.model.SessionSettings
import com.example.model.SurveyType
import com.example.util.BengaliNumberUtils
import com.example.util.LandCalculationEngine
import java.io.OutputStream

data class TableColumnDef(
    val title: String,
    val weight: Float,
    val align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
)

data class RenderRow(
    val cells: List<String>,
)

sealed interface PageContentPlan {
    data class ClassicLedgerPage(
        val rows: List<RenderRow>,
        val showHeaderExtras: Boolean,
        val showTotalsAndFooter: Boolean,
    ) : PageContentPlan

    data class ModernSplitPage(
        val ownerRows: List<RenderRow>,
        val dagRows: List<RenderRow>,
        val showHeaderBlock: Boolean,
        val showTotalsAndFooter: Boolean,
    ) : PageContentPlan
}

class KhatianPdfEngine(private val context: Context) {

    fun renderPreviewBitmaps(
        draft: KhatianDraftState,
        settings: SessionSettings,
    ): List<Bitmap> {
        val kit = PdfPaintKit(context, settings.printInkSaverGrayscale)
        val dim = PdfPageGeometry.resolvePageDimensions(
            surveyType = draft.surveyType,
            settings = settings,
            customLandscape = draft.customConfig.useLandscape,
        )
        val pages = buildPagePlans(draft, settings, dim, kit)
        val requestedScale = settings.pdfQuality.previewDpi / 72f
        val maxSidePt = maxOf(dim.widthPt, dim.heightPt).toFloat()
        val maxSafeScale = 3840f / maxOf(1f, maxSidePt)
        val scale = minOf(requestedScale, maxSafeScale)
        val bmpWidth = maxOf(200, (dim.widthPt * scale).toInt())
        val bmpHeight = maxOf(200, (dim.heightPt * scale).toInt())

        return pages.mapIndexed { index, plan ->
            val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.save()
            canvas.scale(scale, scale)
            drawPlannedPage(canvas, dim, kit, draft, settings, plan, index + 1, pages.size)
            canvas.restore()
            bitmap
        }
    }

    fun writePdfToOutputStream(
        draft: KhatianDraftState,
        settings: SessionSettings,
        outputStream: OutputStream,
        overrideOrientation: com.example.model.PrintOrientationPref? = null,
    ): Int {
        val kit = PdfPaintKit(context, settings.printInkSaverGrayscale)
        val dim = PdfPageGeometry.resolvePageDimensions(
            surveyType = draft.surveyType,
            settings = settings,
            customLandscape = draft.customConfig.useLandscape,
            overrideOrientation = overrideOrientation,
        )
        val pages = buildPagePlans(draft, settings, dim, kit)
        val pdfDocument = PdfDocument()
        try {
            pages.forEachIndexed { index, plan ->
                val pageInfo = PdfDocument.PageInfo.Builder(dim.widthPt, dim.heightPt, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                page.canvas.drawColor(Color.WHITE)
                drawPlannedPage(page.canvas, dim, kit, draft, settings, plan, index + 1, pages.size)
                pdfDocument.finishPage(page)
            }
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            return pages.size
        } finally {
            pdfDocument.close()
        }
    }

    fun buildPagePlans(
        draft: KhatianDraftState,
        settings: SessionSettings,
        dim: PageDimensions,
        kit: PdfPaintKit,
    ): List<PageContentPlan> {
        return when (draft.surveyType) {
            SurveyType.CS, SurveyType.SA -> CsSaRsPdfRenderer.buildCsSaPages(draft, settings, dim, kit)
            SurveyType.RS -> CsSaRsPdfRenderer.buildRsLandscapePages(draft, settings, dim, kit)
            SurveyType.CUSTOM -> {
                if (!draft.customConfig.useLandscape) {
                    planClassicPages(draft, settings, dim, kit)
                } else {
                    planModernLandscapePages(draft, settings, dim, kit)
                }
            }
            else -> planModernLandscapePages(draft, settings, dim, kit)
        }
    }

    private fun columnsForClassic(draft: KhatianDraftState): List<TableColumnDef> {
        return when (draft.surveyType) {
            SurveyType.CS -> listOf(
                TableColumnDef("উপরিস্থ/অধীনস্থ স্বত্ব ও খতিয়ান নং", 1.3f),
                TableColumnDef("মালিক/রায়তের নাম, পিতা/স্বামী ও ঠিকানা", 2.0f),
                TableColumnDef("হিস্যা", 0.75f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("খাজনা ও সেস", 0.8f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("দাগ নং ও শ্রেণি", 1.0f),
                TableColumnDef("দাগের মোট ও প্রাপ্য জমি", 1.15f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("মন্তব্য", 1.0f),
            )
            SurveyType.SA -> listOf(
                TableColumnDef("উপরিস্থ স্বত্ব", 1.15f),
                TableColumnDef("মালিক, অকৃষি প্রজা বা ইজারাদারের বিবরণ", 2.1f),
                TableColumnDef("অংশ", 0.7f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("রাজস্ব ও সেস", 0.8f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("দাগ নং ও শ্রেণি", 1.05f),
                TableColumnDef("মোট ও প্রাপ্য পরিমাণ", 1.15f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("দখল বিষয়ক মন্তব্য", 1.05f),
            )
            SurveyType.RS -> listOf(
                TableColumnDef("১. অংশ", 0.65f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("২. মালিক, অকৃষি প্রজা বা ইজারাদারের নাম ও ঠিকানা", 2.1f),
                TableColumnDef("৩. অংশ / ৪. রাজস্ব", 0.9f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("৫. দাগ নং", 0.75f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("৬. জমির শ্রেণি", 0.8f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("৭. দাগের মোট পরিমাণ", 1.0f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("৮. অংশানুযায়ী প্রাপ্য", 1.0f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("৯. বিশেষ মন্তব্য", 0.95f),
            )
            else -> buildCustomColumns(draft)
        }
    }

    private fun buildCustomColumns(draft: KhatianDraftState): List<TableColumnDef> {
        val cfg = draft.customConfig
        val cols = mutableListOf<TableColumnDef>()
        if (cfg.showSuperiorTenureColumn) {
            cols.add(TableColumnDef("স্বত্ব / রেফারেন্স", 1.1f))
        }
        cols.add(TableColumnDef(cfg.ownerColumnHeader.ifBlank { "মালিকের বিবরণ" }, 2.1f))
        cols.add(TableColumnDef(cfg.hissaColumnHeader.ifBlank { "হিস্যা" }, 0.8f, Layout.Alignment.ALIGN_CENTER))
        if (cfg.showRevenueColumn) {
            cols.add(TableColumnDef("রাজস্ব/কর", 0.8f, Layout.Alignment.ALIGN_CENTER))
        }
        cols.add(TableColumnDef(cfg.dagColumnHeader.ifBlank { "দাগ নং" }, 0.85f, Layout.Alignment.ALIGN_CENTER))
        if (cfg.showLandClassColumn) {
            cols.add(TableColumnDef("জমির শ্রেণি", 0.85f, Layout.Alignment.ALIGN_CENTER))
        }
        cols.add(TableColumnDef(cfg.areaColumnHeader.ifBlank { "জমির পরিমাণ" }, 1.15f, Layout.Alignment.ALIGN_CENTER))
        if (cfg.showRemarksColumn) {
            cols.add(TableColumnDef("মন্তব্য", 1.0f))
        }
        return cols
    }

    private fun formatOwnerFull(owner: OwnerEntry, useBn: Boolean): String {
        val parts = mutableListOf<String>()
        parts.add("[${owner.category.labelBn}] ${owner.name}")
        if (owner.fatherOrHusbandName.isNotBlank()) parts.add("পিং/স্বাং: ${owner.fatherOrHusbandName}")
        if (owner.address.isNotBlank()) parts.add("সাং: ${owner.address}")
        if (owner.ownershipDetails.isNotBlank()) parts.add(owner.ownershipDetails)
        if (owner.landAmount.isNotBlank()) {
            val amt = BengaliNumberUtils.formatScript(owner.landAmount, useBn)
            parts.add("অংশ জমি: $amt ${owner.landUnit.labelBn}")
        }
        return parts.joinToString("\n")
    }

    private fun formatAreaDisplay(rawValue: String, unitLabel: String, useBn: Boolean): String {
        val parsed = BengaliNumberUtils.parseDoubleOrNull(rawValue)
        if (parsed == null) return BengaliNumberUtils.formatScript(rawValue.ifBlank { "-" }, useBn)
        val shotok = LandCalculationEngine.toShotok(
            parsed,
            com.example.util.LandUnit.entries.firstOrNull { it.labelBn == unitLabel } ?: com.example.util.LandUnit.SHOTOK
        )
        val (acres, remShotok) = LandCalculationEngine.splitToAcreAndShotok(shotok)
        val acreStr = BengaliNumberUtils.formatScript("$acres", useBn)
        val shotokStr = BengaliNumberUtils.formatDecimal(remShotok, 2, useBn)
        val rawFormatted = BengaliNumberUtils.formatScript(rawValue, useBn)
        return "$acreStr এ. $shotokStr শ.\n($rawFormatted $unitLabel)"
    }

    private fun buildClassicRows(draft: KhatianDraftState, settings: SessionSettings): List<RenderRow> {
        val useBn = settings.useBengaliNumerals
        val maxCount = maxOf(1, maxOf(draft.owners.size, draft.dags.size))
        val revText = buildString {
            if (draft.extraInfo.revenueAmount.isNotBlank()) {
                append("খাজনা: ${BengaliNumberUtils.formatScript(draft.extraInfo.revenueAmount, useBn)}")
            }
            if (draft.extraInfo.cessAmount.isNotBlank()) {
                if (isNotEmpty()) append("\n")
                append("সেস: ${BengaliNumberUtils.formatScript(draft.extraInfo.cessAmount, useBn)}")
            }
            if (isEmpty()) append("-")
        }
        val extra = draft.basicInfo.surveySpecificValues
        val rows = mutableListOf<RenderRow>()

        for (i in 0 until maxCount) {
            val owner = draft.owners.getOrNull(i)
            val dag = draft.dags.getOrNull(i)
            val ownerText = owner?.let { formatOwnerFull(it, useBn) } ?: ""
            val hissaText = owner?.hissa?.let { BengaliNumberUtils.formatScript(it, useBn) } ?: ""
            val dagNoText = dag?.dagNumber?.let { BengaliNumberUtils.formatScript(it, useBn) } ?: ""
            val classText = dag?.landClass ?: ""
            val totalAreaText = dag?.totalDagArea?.takeIf { it.isNotBlank() }?.let {
                formatAreaDisplay(it, dag.landUnit.labelBn, useBn)
            } ?: ""
            val entitledAreaText = dag?.entitledArea?.takeIf { it.isNotBlank() }?.let {
                formatAreaDisplay(it, dag.landUnit.labelBn, useBn)
            } ?: ""
            val remarkText = dag?.remarks ?: ""

            val cells = when (draft.surveyType) {
                SurveyType.CS -> {
                    val tenureCell = if (i == 0) {
                        listOfNotNull(
                            extra["superior_tenure"]?.takeIf { it.isNotBlank() }?.let { "উপরিস্থ: $it" },
                            extra["subordinate_tenure"]?.takeIf { it.isNotBlank() }?.let { "অধীনস্থ: $it" },
                            extra["khatian_batta"]?.takeIf { it.isNotBlank() }?.let { "বাট্টা: $it" },
                        ).joinToString("\n").ifBlank { "খতিয়ান নং: ${BengaliNumberUtils.formatScript(draft.basicInfo.khatianNumber, useBn)}" }
                    } else ""
                    val dagClass = listOf(dagNoText, classText).filter { it.isNotBlank() }.joinToString("\n")
                    val areaBoth = listOfNotNull(
                        totalAreaText.takeIf { it.isNotBlank() }?.let { "মোট: $it" },
                        entitledAreaText.takeIf { it.isNotBlank() }?.let { "প্রাপ্য: $it" },
                    ).joinToString("\n")
                    listOf(tenureCell, ownerText, hissaText, if (i == 0) revText else "", dagClass, areaBoth, remarkText)
                }
                SurveyType.SA -> {
                    val supCell = if (i == 0) {
                        listOfNotNull(
                            extra["superior_tenure"]?.takeIf { it.isNotBlank() },
                            extra["subordinate_tenure"]?.takeIf { it.isNotBlank() }?.let { "অধীনস্থ: $it" },
                        ).joinToString("\n").ifBlank { "-" }
                    } else ""
                    val dagClass = listOf(dagNoText, classText).filter { it.isNotBlank() }.joinToString("\n")
                    val areaBoth = listOfNotNull(
                        totalAreaText.takeIf { it.isNotBlank() }?.let { "মোট: $it" },
                        entitledAreaText.takeIf { it.isNotBlank() }?.let { "প্রাপ্য: $it" },
                    ).joinToString("\n")
                    listOf(supCell, ownerText, hissaText, if (i == 0) revText else "", dagClass, areaBoth, remarkText)
                }
                SurveyType.RS -> {
                    listOf(
                        hissaText,
                        ownerText,
                        if (i == 0) revText else "",
                        dagNoText,
                        classText,
                        totalAreaText,
                        entitledAreaText,
                        remarkText,
                    )
                }
                else -> {
                    // Custom vertical layout
                    val cfg = draft.customConfig
                    val list = mutableListOf<String>()
                    if (cfg.showSuperiorTenureColumn) {
                        list.add(if (i == 0) extra["custom_ref_code"].orEmpty().ifBlank { "-" } else "")
                    }
                    list.add(ownerText)
                    list.add(hissaText)
                    if (cfg.showRevenueColumn) list.add(if (i == 0) revText else "")
                    list.add(dagNoText)
                    if (cfg.showLandClassColumn) list.add(classText)
                    list.add(entitledAreaText.ifBlank { totalAreaText })
                    if (cfg.showRemarksColumn) list.add(remarkText)
                    list
                }
            }
            rows.add(RenderRow(cells))
        }
        return rows
    }

    private fun planClassicPages(
        draft: KhatianDraftState,
        settings: SessionSettings,
        dim: PageDimensions,
        kit: PdfPaintKit,
    ): List<PageContentPlan.ClassicLedgerPage> {
        val cols = columnsForClassic(draft)
        val totalWeight = cols.sumOf { it.weight.toDouble() }.toFloat()
        val colWidths = cols.map { dim.contentWidth * (it.weight / totalWeight) }
        val allRows = buildClassicRows(draft, settings)

        val headerRowHeight = cols.mapIndexed { idx, col ->
            kit.measureTextHeight(col.title, kit.labelBoldPaint, colWidths[idx], paddingH = 3f, paddingV = 4f)
        }.maxOrNull() ?: 24f

        val plans = mutableListOf<PageContentPlan.ClassicLedgerPage>()
        var idx = 0
        var isFirst = true

        while (idx < allRows.size || isFirst) {
            val topBlockEst = if (isFirst) 128f else 52f
            var remainingY = dim.contentBottom - (dim.marginPt + 6f) - topBlockEst - headerRowHeight - 28f
            val pageRows = mutableListOf<RenderRow>()

            while (idx < allRows.size) {
                val row = allRows[idx]
                val rowH = maxOf(
                    20f,
                    row.cells.mapIndexed { cIdx, cellText ->
                        kit.measureTextHeight(cellText, kit.bodyPaint, colWidths[cIdx], paddingH = 3f, paddingV = 3.5f)
                    }.maxOrNull() ?: 20f
                )
                if (pageRows.isNotEmpty() && remainingY - rowH < 0f) {
                    break
                }
                pageRows.add(row)
                remainingY -= rowH
                idx++
            }

            val isLast = idx >= allRows.size
            plans.add(
                PageContentPlan.ClassicLedgerPage(
                    rows = pageRows,
                    showHeaderExtras = isFirst,
                    showTotalsAndFooter = isLast,
                )
            )
            isFirst = false
        }
        return plans
    }

    private fun buildOwnerSplitRows(draft: KhatianDraftState, settings: SessionSettings): List<RenderRow> {
        val useBn = settings.useBengaliNumerals
        if (draft.owners.isEmpty()) {
            return listOf(RenderRow(listOf("-", "কোনো মালিকের তথ্য দেওয়া হয়নি", "-", "-")))
        }
        return draft.owners.mapIndexed { index, owner ->
            val sl = BengaliNumberUtils.formatScript("${index + 1}", useBn)
            val full = formatOwnerFull(owner, useBn)
            val hissa = BengaliNumberUtils.formatScript(owner.hissa.ifBlank { "-" }, useBn)
            val rev = if (index == 0) {
                BengaliNumberUtils.formatScript(draft.extraInfo.revenueAmount.ifBlank { "-" }, useBn)
            } else ""
            RenderRow(listOf(sl, full, hissa, rev))
        }
    }

    private fun buildDagSplitRows(draft: KhatianDraftState, settings: SessionSettings): List<RenderRow> {
        val useBn = settings.useBengaliNumerals
        if (draft.dags.isEmpty()) {
            return listOf(RenderRow(listOf("-", "-", "-", "-", "-")))
        }
        return draft.dags.map { dag ->
            val dagNo = BengaliNumberUtils.formatScript(dag.dagNumber.ifBlank { "-" }, useBn)
            val landClass = dag.landClass.ifBlank { "-" }
            val totalStr = if (dag.totalDagArea.isNotBlank()) {
                formatAreaDisplay(dag.totalDagArea, dag.landUnit.labelBn, useBn)
            } else "-"
            val entitledStr = if (dag.entitledArea.isNotBlank()) {
                formatAreaDisplay(dag.entitledArea, dag.landUnit.labelBn, useBn)
            } else "-"
            val note = buildString {
                if (dag.ownerShare.isNotBlank()) {
                    append("অংশ: ${BengaliNumberUtils.formatScript(dag.ownerShare, useBn)}")
                }
                if (dag.remarks.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append(dag.remarks)
                }
                if (isEmpty()) append("-")
            }
            RenderRow(listOf(dagNo, landClass, totalStr, entitledStr, note))
        }
    }

    private fun planModernLandscapePages(
        draft: KhatianDraftState,
        settings: SessionSettings,
        dim: PageDimensions,
        kit: PdfPaintKit,
    ): List<PageContentPlan.ModernSplitPage> {
        val gap = 10f
        val leftW = dim.contentWidth * 0.42f
        val rightW = dim.contentWidth - leftW - gap

        val ownerColWeights = listOf(0.45f, 2.15f, 0.7f, 0.7f)
        val ownerWTotal = ownerColWeights.sum()
        val ownerWidths = ownerColWeights.map { leftW * (it / ownerWTotal) }

        val dagColWeights = listOf(0.75f, 0.85f, 1.15f, 1.15f, 1.1f)
        val dagWTotal = dagColWeights.sum()
        val dagWidths = dagColWeights.map { rightW * (it / dagWTotal) }

        val allOwners = buildOwnerSplitRows(draft, settings)
        val allDags = buildDagSplitRows(draft, settings)

        var oIdx = 0
        var dIdx = 0
        var isFirst = true
        val plans = mutableListOf<PageContentPlan.ModernSplitPage>()

        while (oIdx < allOwners.size || dIdx < allDags.size || isFirst) {
            val headerBlockH = if (isFirst) 108f else 44f
            val tableHeaderH = 26f
            val availH = dim.contentBottom - (dim.marginPt + 6f) - headerBlockH - tableHeaderH - 46f

            var remLeft = availH
            val pageOwners = mutableListOf<RenderRow>()
            while (oIdx < allOwners.size) {
                val r = allOwners[oIdx]
                val h = maxOf(
                    20f,
                    r.cells.mapIndexed { c, t -> kit.measureTextHeight(t, kit.bodyPaint, ownerWidths[c], 3f, 3f) }.maxOrNull() ?: 20f
                )
                if (pageOwners.isNotEmpty() && remLeft - h < 0f) break
                pageOwners.add(r)
                remLeft -= h
                oIdx++
            }

            var remRight = availH
            val pageDags = mutableListOf<RenderRow>()
            while (dIdx < allDags.size) {
                val r = allDags[dIdx]
                val h = maxOf(
                    20f,
                    r.cells.mapIndexed { c, t -> kit.measureTextHeight(t, kit.bodyPaint, dagWidths[c], 3f, 3f) }.maxOrNull() ?: 20f
                )
                if (pageDags.isNotEmpty() && remRight - h < 0f) break
                pageDags.add(r)
                remRight -= h
                dIdx++
            }

            val isLast = oIdx >= allOwners.size && dIdx >= allDags.size
            plans.add(
                PageContentPlan.ModernSplitPage(
                    ownerRows = pageOwners,
                    dagRows = pageDags,
                    showHeaderBlock = isFirst,
                    showTotalsAndFooter = isLast,
                )
            )
            isFirst = false
        }
        return plans
    }

    private fun drawPlannedPage(
        canvas: Canvas,
        dim: PageDimensions,
        kit: PdfPaintKit,
        draft: KhatianDraftState,
        settings: SessionSettings,
        plan: PageContentPlan,
        pageNumber: Int,
        totalPages: Int,
    ) {
        val isCustom = draft.surveyType == SurveyType.CUSTOM
        kit.drawFrameAndFooter(canvas, dim, pageNumber, totalPages, settings.useBengaliNumerals, isCustom)

        when (plan) {
            is PageContentPlan.ClassicLedgerPage -> {
                when (draft.surveyType) {
                    SurveyType.CS, SurveyType.SA -> CsSaRsPdfRenderer.drawCsOrSaPage(canvas, dim, kit, draft, settings, plan)
                    SurveyType.RS -> CsSaRsPdfRenderer.drawRsLandscapePage(canvas, dim, kit, draft, settings, plan)
                    else -> drawClassicPage(canvas, dim, kit, draft, settings, plan)
                }
            }
            is PageContentPlan.ModernSplitPage -> drawModernSplitPage(canvas, dim, kit, draft, settings, plan)
        }
    }

    private fun drawClassicPage(
        canvas: Canvas,
        dim: PageDimensions,
        kit: PdfPaintKit,
        draft: KhatianDraftState,
        settings: SessionSettings,
        plan: PageContentPlan.ClassicLedgerPage,
    ) {
        val useBn = settings.useBengaliNumerals
        val m = dim.marginPt + 6f
        val w = dim.contentWidth - 12f
        var y = dim.marginPt + 8f

        val headingTitle = if (draft.surveyType == SurveyType.CUSTOM) {
            "${draft.customConfig.customSurveyTitle.ifBlank { "কাস্টম খতিয়ান খসড়া" }} (Custom Template)"
        } else {
            val activeName = draft.basicInfo.surveyName.ifBlank { draft.surveyType.titleBn }
            "$activeName — খসড়া নমুনা"
        }

        if (plan.showHeaderExtras) {
            y += kit.drawTextInBox(canvas, headingTitle, m, y, w, kit.titlePaint, align = Layout.Alignment.ALIGN_CENTER)
            y += kit.drawTextInBox(
                canvas,
                "${draft.surveyType.formRefBn} • ${draft.surveyType.legalProvisionBn}",
                m, y - 2f, w, kit.smallNotePaint, paddingV = 2f, align = Layout.Alignment.ALIGN_CENTER
            )

            // Administrative info grid (3 rows x 3 columns)
            val b = draft.basicInfo
            val ex = b.surveySpecificValues
            val infoCells = buildList {
                add("বিভাগ: ${b.division.ifBlank { "-" }}")
                add("জেলা: ${b.district.ifBlank { "-" }}")
                add("উপজেলা/থানা: ${listOf(b.upazila, b.thana).filter { it.isNotBlank() }.joinToString("/").ifBlank { "-" }}")
                add("ইউনিয়ন: ${b.unionName.ifBlank { "-" }}")
                add("মৌজা: ${b.mouzaName.ifBlank { "-" }}")
                add("জে.এল. নং: ${BengaliNumberUtils.formatScript(b.jlNumber.ifBlank { "-" }, useBn)}")
                add("খতিয়ান নং: ${BengaliNumberUtils.formatScript(b.khatianNumber.ifBlank { "-" }, useBn)}")
                add("পৃষ্ঠা নং: ${BengaliNumberUtils.formatScript(b.pageNumber.ifBlank { "১" }, useBn)}")
                when (draft.surveyType) {
                    SurveyType.CS -> add("পরগনা/তৌজি: ${ex["pargana"].orEmpty()} / ${ex["touzi_no"].orEmpty()}")
                    SurveyType.SA -> add("পরগনা/তৌজি/রে.সা.: ${ex["pargana"].orEmpty()} / ${ex["touzi_no"].orEmpty()} / ${ex["resa_no"].orEmpty()}")
                    SurveyType.RS -> add("রে.সা. নং: ${ex["resa_no"].orEmpty()} | সাবেক খতিয়ান: ${ex["former_khatian"].orEmpty()}")
                    else -> add("রেফারেন্স: ${ex["custom_ref_code"].orEmpty().ifBlank { "-" }}")
                }
            }

            val colW = w / 3f
            infoCells.chunked(3).forEach { rowItems ->
                val rowH = rowItems.maxOf { kit.measureTextHeight(it, kit.bodyPaint, colW, 4f, 3f) }
                rowItems.forEachIndexed { idx, txt ->
                    val cx = m + idx * colW
                    canvas.drawRect(cx, y, cx + colW, y + rowH, kit.subHeaderFill)
                    canvas.drawRect(cx, y, cx + colW, y + rowH, kit.borderStroke)
                    kit.drawTextInBox(canvas, txt, cx, y, colW, kit.bodyPaint, 4f, 3f)
                }
                y += rowH
            }
            y += 6f
        } else {
            val contHeader = "$headingTitle (চলমান পৃষ্ঠা) — খতিয়ান নং: ${BengaliNumberUtils.formatScript(draft.basicInfo.khatianNumber, useBn)}"
            y += kit.drawTextInBox(canvas, contHeader, m, y, w, kit.subtitlePaint, paddingV = 3f)
            y += 4f
        }

        // Table Header + Rows
        val cols = columnsForClassic(draft)
        val totalWeight = cols.sumOf { it.weight.toDouble() }.toFloat()
        val colWidths = cols.map { w * (it.weight / totalWeight) }

        val headerH = cols.mapIndexed { i, c ->
            kit.measureTextHeight(c.title, kit.labelBoldPaint, colWidths[i], 3f, 4f)
        }.maxOrNull() ?: 24f

        var curX = m
        cols.forEachIndexed { i, col ->
            val cw = colWidths[i]
            canvas.drawRect(curX, y, curX + cw, y + headerH, kit.headerFill)
            canvas.drawRect(curX, y, curX + cw, y + headerH, kit.borderStroke)
            kit.drawTextInBox(canvas, col.title, curX, y, cw, kit.labelBoldPaint, 3f, 4f, col.align)
            curX += cw
        }
        y += headerH

        plan.rows.forEach { row ->
            val rowH = maxOf(
                20f,
                row.cells.mapIndexed { i, txt ->
                    kit.measureTextHeight(txt, kit.bodyPaint, colWidths[i], 3f, 3.5f)
                }.maxOrNull() ?: 20f
            )
            var rx = m
            row.cells.forEachIndexed { i, txt ->
                val cw = colWidths[i]
                canvas.drawRect(rx, y, rx + cw, y + rowH, kit.borderStroke)
                kit.drawTextInBox(canvas, txt, rx, y, cw, kit.bodyPaint, 3f, 3.5f, cols[i].align)
                rx += cw
            }
            y += rowH
        }

        if (plan.showTotalsAndFooter) {
            y += 6f
            drawSummaryFooterBox(canvas, m, y, w, kit, draft, settings)
        }
    }

    private fun drawModernSplitPage(
        canvas: Canvas,
        dim: PageDimensions,
        kit: PdfPaintKit,
        draft: KhatianDraftState,
        settings: SessionSettings,
        plan: PageContentPlan.ModernSplitPage,
    ) {
        val useBn = settings.useBengaliNumerals
        val m = dim.marginPt + 6f
        val w = dim.contentWidth - 12f
        var y = dim.marginPt + 8f

        val headingTitle = if (draft.surveyType == SurveyType.CUSTOM) {
            "${draft.customConfig.customSurveyTitle.ifBlank { "কাস্টম খতিয়ান খসড়া" }} (Custom Template — সরকারি ফরম নয়)"
        } else {
            val activeName = draft.basicInfo.surveyName.ifBlank { draft.surveyType.titleBn }
            "$activeName — ডিজিটাল খসড়া"
        }

        if (plan.showHeaderBlock) {
            y += kit.drawTextInBox(canvas, headingTitle, m, y, w, kit.titlePaint, align = Layout.Alignment.ALIGN_CENTER)
            y += kit.drawTextInBox(
                canvas,
                "${draft.surveyType.formRefBn} • ${draft.surveyType.legalProvisionBn}",
                m, y - 2f, w, kit.smallNotePaint, paddingV = 2f, align = Layout.Alignment.ALIGN_CENTER
            )

            val b = draft.basicInfo
            val ex = b.surveySpecificValues
            val gridCells = buildList {
                add("বিভাগ: ${b.division.ifBlank { "-" }}")
                add("জেলা: ${b.district.ifBlank { "-" }}")
                add("উপজেলা/থানা: ${listOf(b.upazila, b.thana).filter { it.isNotBlank() }.joinToString("/").ifBlank { "-" }}")
                add("ইউনিয়ন/পৌর: ${b.unionName.ifBlank { "-" }}")
                add("মৌজা: ${b.mouzaName.ifBlank { "-" }}")
                add("জে.এল. নং: ${BengaliNumberUtils.formatScript(b.jlNumber.ifBlank { "-" }, useBn)}")
                add("খতিয়ান নং: ${BengaliNumberUtils.formatScript(b.khatianNumber.ifBlank { "-" }, useBn)}")
                add("পৃষ্ঠা: ${BengaliNumberUtils.formatScript(b.pageNumber.ifBlank { "১" }, useBn)}")
                when (draft.surveyType) {
                    SurveyType.BS -> add("রে.সা. নং: ${ex["resa_no"].orEmpty()} | সন: ${ex["survey_year"].orEmpty()}")
                    SurveyType.BRS -> add("পূর্ববর্তী RS: ${ex["prev_rs_khatian"].orEmpty()} | দাগ: ${ex["prev_dag_ref"].orEmpty()}")
                    SurveyType.BDRS -> add("দিয়ারা সার্কেল: ${ex["diara_circle"].orEmpty()} | শিট: ${ex["sheet_no"].orEmpty()}")
                    SurveyType.CITY_SURVEY -> add("সিটি/ওয়ার্ড: ${ex["city_corp"].orEmpty()} (${ex["ward_no"].orEmpty()}) | হোল্ডিং: ${ex["holding_no"].orEmpty()}")
                    SurveyType.CUSTOM -> add("অফিস/রেফ: ${ex["custom_office"].orEmpty().ifBlank { ex["custom_ref_code"].orEmpty() }}")
                    else -> add("-")
                }
            }

            val colW = w / 4f
            gridCells.chunked(4).forEach { rowItems ->
                val rowH = rowItems.maxOf { kit.measureTextHeight(it, kit.bodyPaint, colW, 4f, 2.8f) }
                rowItems.forEachIndexed { idx, txt ->
                    val cx = m + idx * colW
                    canvas.drawRect(cx, y, cx + colW, y + rowH, kit.subHeaderFill)
                    canvas.drawRect(cx, y, cx + colW, y + rowH, kit.borderStroke)
                    kit.drawTextInBox(canvas, txt, cx, y, colW, kit.bodyPaint, 4f, 2.8f)
                }
                y += rowH
            }
            y += 6f
        } else {
            val contHeader = "$headingTitle (চলমান পৃষ্ঠা) — খতিয়ান নং: ${BengaliNumberUtils.formatScript(draft.basicInfo.khatianNumber, useBn)}"
            y += kit.drawTextInBox(canvas, contHeader, m, y, w, kit.subtitlePaint, paddingV = 3f)
            y += 4f
        }

        val gap = 10f
        val leftW = w * 0.42f
        val rightW = w - leftW - gap
        val rightX = m + leftW + gap

        // Left: Owner Table
        val ownerCols = if (draft.surveyType == SurveyType.CUSTOM) {
            listOf(
                TableColumnDef("ক্রম", 0.45f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef(draft.customConfig.ownerColumnHeader.ifBlank { "মালিকের বিবরণ" }, 2.15f),
                TableColumnDef(draft.customConfig.hissaColumnHeader.ifBlank { "হিস্যা" }, 0.7f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef(if (draft.customConfig.showRevenueColumn) "রাজস্ব" else "মন্তব্য", 0.7f, Layout.Alignment.ALIGN_CENTER),
            )
        } else {
            listOf(
                TableColumnDef("ক্রম", 0.45f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("মালিক/স্বত্বভোগীর নাম, পিতা/স্বামী ও ঠিকানা", 2.15f),
                TableColumnDef("হিস্যা", 0.7f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("রাজস্ব", 0.7f, Layout.Alignment.ALIGN_CENTER),
            )
        }
        val oTotalW = ownerCols.sumOf { it.weight.toDouble() }.toFloat()
        val oWidths = ownerCols.map { leftW * (it.weight / oTotalW) }

        // Right: Dag Table
        val dagCols = if (draft.surveyType == SurveyType.CUSTOM) {
            listOf(
                TableColumnDef(draft.customConfig.dagColumnHeader.ifBlank { "দাগ নং" }, 0.75f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef(if (draft.customConfig.showLandClassColumn) "শ্রেণি" else "বিবরণ", 0.85f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("মোট জমি", 1.15f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef(draft.customConfig.areaColumnHeader.ifBlank { "প্রাপ্য জমি" }, 1.15f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef(if (draft.customConfig.showRemarksColumn) "মন্তব্য" else "নোট", 1.1f),
            )
        } else {
            listOf(
                TableColumnDef(if (draft.surveyType == SurveyType.CITY_SURVEY) "প্লট/দাগ" else "দাগ নং", 0.75f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("জমির শ্রেণি", 0.85f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("দাগের মোট পরিমাণ\n(একর | শতক)", 1.15f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("অংশানুযায়ী জমি\n(একর | শতক)", 1.15f, Layout.Alignment.ALIGN_CENTER),
                TableColumnDef("মন্তব্য / বিবরণ", 1.1f),
            )
        }
        val dTotalW = dagCols.sumOf { it.weight.toDouble() }.toFloat()
        val dWidths = dagCols.map { rightW * (it.weight / dTotalW) }

        val sharedHeaderH = maxOf(
            ownerCols.mapIndexed { i, c -> kit.measureTextHeight(c.title, kit.labelBoldPaint, oWidths[i], 3f, 3.5f) }.maxOrNull() ?: 24f,
            dagCols.mapIndexed { i, c -> kit.measureTextHeight(c.title, kit.labelBoldPaint, dWidths[i], 3f, 3.5f) }.maxOrNull() ?: 24f,
        )

        var ox = m
        ownerCols.forEachIndexed { i, col ->
            canvas.drawRect(ox, y, ox + oWidths[i], y + sharedHeaderH, kit.headerFill)
            canvas.drawRect(ox, y, ox + oWidths[i], y + sharedHeaderH, kit.borderStroke)
            kit.drawTextInBox(canvas, col.title, ox, y, oWidths[i], kit.labelBoldPaint, 3f, 3.5f, col.align)
            ox += oWidths[i]
        }

        var dx = rightX
        dagCols.forEachIndexed { i, col ->
            canvas.drawRect(dx, y, dx + dWidths[i], y + sharedHeaderH, kit.headerFill)
            canvas.drawRect(dx, y, dx + dWidths[i], y + sharedHeaderH, kit.borderStroke)
            kit.drawTextInBox(canvas, col.title, dx, y, dWidths[i], kit.labelBoldPaint, 3f, 3.5f, col.align)
            dx += dWidths[i]
        }

        var oy = y + sharedHeaderH
        plan.ownerRows.forEach { row ->
            val h = maxOf(
                20f,
                row.cells.mapIndexed { i, t -> kit.measureTextHeight(t, kit.bodyPaint, oWidths[i], 3f, 3f) }.maxOrNull() ?: 20f
            )
            var cx = m
            row.cells.forEachIndexed { i, t ->
                canvas.drawRect(cx, oy, cx + oWidths[i], oy + h, kit.borderStroke)
                kit.drawTextInBox(canvas, t, cx, oy, oWidths[i], kit.bodyPaint, 3f, 3f, ownerCols[i].align)
                cx += oWidths[i]
            }
            oy += h
        }

        var dy = y + sharedHeaderH
        plan.dagRows.forEach { row ->
            val h = maxOf(
                20f,
                row.cells.mapIndexed { i, t -> kit.measureTextHeight(t, kit.bodyPaint, dWidths[i], 3f, 3f) }.maxOrNull() ?: 20f
            )
            var cx = rightX
            row.cells.forEachIndexed { i, t ->
                canvas.drawRect(cx, dy, cx + dWidths[i], dy + h, kit.borderStroke)
                kit.drawTextInBox(canvas, t, cx, dy, dWidths[i], kit.bodyPaint, 3f, 3f, dagCols[i].align)
                cx += dWidths[i]
            }
            dy += h
        }

        if (plan.showTotalsAndFooter) {
            val summaryY = maxOf(oy, dy) + 8f
            drawSummaryFooterBox(canvas, m, summaryY, w, kit, draft, settings)
        }
    }

    private fun drawSummaryFooterBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        kit: PdfPaintKit,
        draft: KhatianDraftState,
        settings: SessionSettings,
    ) {
        val useBn = settings.useBengaliNumerals
        var totalEntitledShotok = 0.0
        draft.dags.forEach { dag ->
            val v = BengaliNumberUtils.parseDoubleOrNull(dag.entitledArea)
            if (v != null && v > 0.0) {
                totalEntitledShotok += LandCalculationEngine.toShotok(v, dag.landUnit)
            }
        }
        val (totAcres, totRemShotok) = LandCalculationEngine.splitToAcreAndShotok(totalEntitledShotok)
        val totalShotokStr = BengaliNumberUtils.formatDecimal(totalEntitledShotok, 2, useBn)
        val totAcreStr = BengaliNumberUtils.formatScript("$totAcres", useBn)
        val totRemStr = BengaliNumberUtils.formatDecimal(totRemShotok, 2, useBn)

        val summaryLines = buildList {
            add("মোট দাগ সংখ্যা: ${BengaliNumberUtils.formatScript("${draft.dags.size}", useBn)}টি | মোট মালিক: ${BengaliNumberUtils.formatScript("${draft.owners.size}", useBn)} জন | মোট প্রাপ্য জমি: $totAcreStr একর $totRemStr শতক (মোট $totalShotokStr শতক)")
            val e = draft.extraInfo
            val boundary = listOfNotNull(
                e.boundaryNorth.takeIf { it.isNotBlank() }?.let { "উত্তর: $it" },
                e.boundarySouth.takeIf { it.isNotBlank() }?.let { "দক্ষিণ: $it" },
                e.boundaryEast.takeIf { it.isNotBlank() }?.let { "পূর্ব: $it" },
                e.boundaryWest.takeIf { it.isNotBlank() }?.let { "পশ্চিম: $it" },
            ).joinToString(" | ")
            if (boundary.isNotBlank()) add("চৌহদ্দি: $boundary")
            if (e.specialNotes.isNotBlank()) add("বিশেষ নোট: ${e.specialNotes}")
        }.joinToString("\n")

        val boxH = kit.measureTextHeight(summaryLines, kit.bodyPaint, w, 6f, 5f)
        canvas.drawRect(x, y, x + w, y + boxH, kit.subHeaderFill)
        canvas.drawRect(x, y, x + w, y + boxH, kit.borderStroke)
        kit.drawTextInBox(canvas, summaryLines, x, y, w, kit.bodyPaint, 6f, 5f)
    }
}
