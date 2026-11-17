package com.example.pdf

import android.graphics.Canvas
import android.text.Layout
import com.example.model.DagEntry
import com.example.model.KhatianDraftState
import com.example.model.OwnerEntry
import com.example.model.SessionSettings
import com.example.model.SurveyType
import com.example.util.BengaliNumberUtils
import com.example.util.LandCalculationEngine

object CsSaRsPdfRenderer {

    fun buildCsSaPages(
        draft: KhatianDraftState,
        settings: SessionSettings,
        dim: PageDimensions,
        kit: PdfPaintKit,
    ): List<PageContentPlan.ClassicLedgerPage> {
        val pages = mutableListOf<PageContentPlan.ClassicLedgerPage>()
        // Page 1 is always the exact CS or SA front ledger matching reference Forms 1 & 2
        pages.add(
            PageContentPlan.ClassicLedgerPage(
                rows = emptyList(),
                showHeaderExtras = true,
                showTotalsAndFooter = draft.dags.isEmpty(),
            )
        )
        // If user entered Dag/Plot rows, paginate them cleanly starting from Page 2
        if (draft.dags.isNotEmpty()) {
            val useBn = settings.useBengaliNumerals
            val dagCols = dagContinuationWeights(dim.contentWidth - 12f)
            val dagRows = draft.dags.map { dag ->
                val dagNo = BengaliNumberUtils.formatScript(dag.dagNumber.ifBlank { "-" }, useBn)
                val cls = dag.landClass.ifBlank { "-" }
                val totPair = splitAreaPair(dag.totalDagArea, dag.landUnit.labelBn, useBn)
                val share = BengaliNumberUtils.formatScript(dag.ownerShare.ifBlank { "-" }, useBn)
                val entPair = splitAreaPair(dag.entitledArea, dag.landUnit.labelBn, useBn)
                RenderRow(listOf(dagNo, cls, totPair.first, totPair.second, share, entPair.first, entPair.second, dag.remarks.ifBlank { "-" }))
            }
            var idx = 0
            while (idx < dagRows.size) {
                var remY = dim.contentBottom - (dim.marginPt + 6f) - 80f
                val pageRows = mutableListOf<RenderRow>()
                while (idx < dagRows.size) {
                    val r = dagRows[idx]
                    val h = maxOf(
                        20f,
                        r.cells.mapIndexed { c, t -> kit.measureTextHeight(t, kit.bodyPaint, dagCols[c], 3f, 3f) }.maxOrNull() ?: 20f
                    )
                    if (pageRows.isNotEmpty() && remY - h < 0f) break
                    pageRows.add(r)
                    remY -= h
                    idx++
                }
                pages.add(
                    PageContentPlan.ClassicLedgerPage(
                        rows = pageRows,
                        showHeaderExtras = false,
                        showTotalsAndFooter = idx >= dagRows.size,
                    )
                )
            }
        }
        return pages
    }

    fun buildRsLandscapePages(
        draft: KhatianDraftState,
        settings: SessionSettings,
        dim: PageDimensions,
        kit: PdfPaintKit,
    ): List<PageContentPlan.ClassicLedgerPage> {
        val useBn = settings.useBengaliNumerals
        val w = dim.contentWidth - 12f
        val rsWidths = rsColumnWidths(w)

        // Build unified RS rows combining owners (on initial rows) + all Dags
        val ownerFullBlock = if (draft.owners.isEmpty()) {
            "মালিক তথ্য দেওয়া হয়নি"
        } else {
            draft.owners.joinToString("\n\n") { o -> formatOwnerRsBlock(o, useBn) }
        }
        val ownerShareBlock = if (draft.owners.isEmpty()) {
            ""
        } else {
            draft.owners.joinToString("\n\n") { o ->
                BengaliNumberUtils.formatScript(o.hissa.ifBlank { "-" }, useBn)
            }
        }
        val revText = BengaliNumberUtils.formatScript(draft.extraInfo.revenueAmount.ifBlank { "" }, useBn)

        val allRows = if (draft.dags.isEmpty()) {
            listOf(
                RenderRow(
                    listOf(ownerFullBlock, ownerShareBlock, revText, "-", "-", "-", "-", "-", "-", "-", "-", "-")
                )
            )
        } else {
            draft.dags.mapIndexed { index, dag ->
                val dagNo = BengaliNumberUtils.formatScript(dag.dagNumber.ifBlank { "-" }, useBn)
                val agriCls = if (!dag.isNonAgricultural) dag.landClass else ""
                val nonAgriCls = if (dag.isNonAgricultural) dag.landClass else ""
                val totPair = splitAreaPair(dag.totalDagArea, dag.landUnit.labelBn, useBn)
                val dagShare = BengaliNumberUtils.formatScript(dag.ownerShare.ifBlank { "-" }, useBn)
                val entPair = splitAreaPair(dag.entitledArea, dag.landUnit.labelBn, useBn)
                RenderRow(
                    listOf(
                        if (index == 0) ownerFullBlock else "",
                        if (index == 0) ownerShareBlock else "",
                        if (index == 0) revText else "",
                        dagNo,
                        agriCls,
                        nonAgriCls,
                        totPair.first,
                        totPair.second,
                        dagShare,
                        entPair.first,
                        entPair.second,
                        dag.remarks,
                    )
                )
            }
        }

        val plans = mutableListOf<PageContentPlan.ClassicLedgerPage>()
        var idx = 0
        var isFirst = true
        while (idx < allRows.size || isFirst) {
            val topUsed = if (isFirst) 118f else 78f
            val footerReserve = 66f
            var remY = dim.contentBottom - (dim.marginPt + 6f) - topUsed - footerReserve
            val pageRows = mutableListOf<RenderRow>()
            while (idx < allRows.size) {
                val r = allRows[idx]
                val h = maxOf(
                    19f,
                    r.cells.mapIndexed { c, t -> kit.measureTextHeight(t, kit.bodyPaint, rsWidths[c], 2.5f, 3f) }.maxOrNull() ?: 19f
                )
                if (pageRows.isNotEmpty() && remY - h < 0f) break
                pageRows.add(r)
                remY -= h
                idx++
            }
            plans.add(
                PageContentPlan.ClassicLedgerPage(
                    rows = pageRows,
                    showHeaderExtras = isFirst,
                    showTotalsAndFooter = idx >= allRows.size,
                )
            )
            isFirst = false
        }
        return plans
    }

    fun drawCsOrSaPage(
        canvas: Canvas,
        dim: PageDimensions,
        kit: PdfPaintKit,
        draft: KhatianDraftState,
        settings: SessionSettings,
        plan: PageContentPlan.ClassicLedgerPage,
    ) {
        val isSa = draft.surveyType == SurveyType.SA
        val useBn = settings.useBengaliNumerals
        val m = dim.marginPt + 6f
        val w = dim.contentWidth - 12f
        var y = dim.marginPt + 8f

        val b = draft.basicInfo
        val ex = b.surveySpecificValues

        if (plan.showHeaderExtras) {
            val title = if (isSa) "এস এ খতিয়ান" else "সি এস খতিয়ান"
            y += kit.drawTextInBox(canvas, title, m, y, w, kit.titlePaint, align = Layout.Alignment.ALIGN_CENTER)

            // 2x4 Header strip matching Photos 1 & 2
            val thanaDisplay = b.thana.ifBlank { b.upazila }
            val topPairs = listOf(
                "জিলা: ${b.district.ifBlank { "-" }}",
                "মৌজা: ${b.mouzaName.ifBlank { "-" }}",
                "জে: এল: নং: ${BengaliNumberUtils.formatScript(b.jlNumber.ifBlank { "-" }, useBn)}",
                "খতিয়ান নং: ${BengaliNumberUtils.formatScript(b.khatianNumber.ifBlank { "-" }, useBn)}",
                "থানা: ${thanaDisplay.ifBlank { "-" }}",
                "পরগণা: ${ex["pargana"].orEmpty().ifBlank { "-" }}",
                "রে: সা: নং: ${BengaliNumberUtils.formatScript(ex["resa_no"].orEmpty().ifBlank { "-" }, useBn)}",
                "তৌজি নং: ${BengaliNumberUtils.formatScript(ex["touzi_no"].orEmpty().ifBlank { "-" }, useBn)}",
            )
            val metaW = w / 4f
            topPairs.chunked(4).forEach { rowItems ->
                val rowH = rowItems.maxOf { kit.measureTextHeight(it, kit.bodyPaint, metaW, 3f, 2.5f) }
                rowItems.forEachIndexed { idx, txt ->
                    kit.drawTextInBox(canvas, txt, m + idx * metaW, y, metaW, kit.bodyPaint, 3f, 2.5f)
                }
                y += rowH
            }
            y += 4f

            // Upper Table
            if (!isSa) {
                y = drawCsUpperTable(canvas, m, y, w, kit, ex, useBn)
            } else {
                y = drawSaUpperTable(canvas, m, y, w, kit, ex, useBn)
            }

            // Lower Ownership & Tenure Table (5 columns matching Photos 1 & 2)
            y = drawCsSaLowerOwnershipTable(canvas, m, y, w, dim.contentBottom - 62f - y, kit, draft, isSa, useBn)

            // Bottom Change / Case No. Box
            val bottomLabel = if (isSa) {
                "ধারামতে নোট বা পরিবর্তন\n[মায় মোকদ্দমা নং এবং সন]"
            } else {
                "ধারা মতে পরিবর্তন ও\nমোকদ্দমা নম্বর"
            }
            val noteText = ex["case_change_note"].orEmpty()
            val bLeftW = w * 0.24f
            val bRightW = w - bLeftW
            val bRowH = maxOf(
                44f,
                kit.measureTextHeight(bottomLabel, kit.bodyPaint, bLeftW, 4f, 4f),
                kit.measureTextHeight(noteText, kit.bodyPaint, bRightW, 4f, 4f),
            )
            canvas.drawRect(m, y, m + bLeftW, y + bRowH, kit.borderStroke)
            canvas.drawRect(m + bLeftW, y, m + w, y + bRowH, kit.borderStroke)
            kit.drawTextInBox(canvas, bottomLabel, m, y, bLeftW, kit.labelBoldPaint, 4f, 6f, Layout.Alignment.ALIGN_CENTER)
            kit.drawTextInBox(canvas, noteText, m + bLeftW, y, bRightW, kit.bodyPaint, 6f, 6f)
        } else {
            // Continuation Page 2+: Dag & Land Schedule for CS / SA
            val contTitle = "${if (isSa) "এস এ" else "সি এস"} খতিয়ান — দাগ ও জমির বিবরণ (খতিয়ান নং: ${BengaliNumberUtils.formatScript(b.khatianNumber, useBn)})"
            y += kit.drawTextInBox(canvas, contTitle, m, y, w, kit.subtitlePaint, paddingV = 4f)
            y += 4f
            drawDagContinuationTable(canvas, m, y, w, kit, draft, settings, plan)
        }
    }

    private fun drawCsUpperTable(
        canvas: Canvas,
        x: Float,
        startY: Float,
        w: Float,
        kit: PdfPaintKit,
        ex: Map<String, String>,
        useBn: Boolean,
    ): Float {
        var y = startY
        // Sub-columns: 0.24 (dakhalkar) | 0.09 (share) | 0.09 (khajana) | 0.09 (cess) | 0.29 (remarks) | 0.10 (d-khaj) | 0.10 (d-cess)
        val colW = listOf(w * 0.24f, w * 0.09f, w * 0.09f, w * 0.09f, w * 0.29f, w * 0.10f, w * 0.10f)
        val g1W = colW[0] + colW[1]
        val g2W = colW[2] + colW[3]
        val g3W = colW[4]
        val g4W = colW[5] + colW[6]

        val r1H = 22f
        val r2H = 24f

        // Header Row 1
        canvas.drawRect(x, y, x + g1W, y + r1H, kit.headerFill)
        canvas.drawRect(x, y, x + g1W, y + r1H, kit.borderStroke)
        kit.drawTextInBox(canvas, "উপরিস্থ স্বত্বের", x, y, g1W, kit.labelBoldPaint, 2f, 4f, Layout.Alignment.ALIGN_CENTER)

        val g2X = x + g1W
        canvas.drawRect(g2X, y, g2X + g2W, y + r1H, kit.headerFill)
        canvas.drawRect(g2X, y, g2X + g2W, y + r1H, kit.borderStroke)
        kit.drawTextInBox(canvas, "অত্র স্বত্বের দেয়", g2X, y, g2W, kit.labelBoldPaint, 2f, 4f, Layout.Alignment.ALIGN_CENTER)

        val g3X = g2X + g2W
        canvas.drawRect(g3X, y, g3X + g3W, y + r1H + r2H, kit.headerFill)
        canvas.drawRect(g3X, y, g3X + g3W, y + r1H + r2H, kit.borderStroke)
        kit.drawTextInBox(canvas, "মন্তব্য", g3X, y + 12f, g3W, kit.labelBoldPaint, 2f, 4f, Layout.Alignment.ALIGN_CENTER)

        val g4X = g3X + g3W
        canvas.drawRect(g4X, y, g4X + g4W, y + r1H, kit.headerFill)
        canvas.drawRect(g4X, y, g4X + g4W, y + r1H, kit.borderStroke)
        kit.drawTextInBox(canvas, "ধারামতে ও\nকোন সন হইতে", g4X, y, g4W, kit.smallNotePaint, 2f, 2f, Layout.Alignment.ALIGN_CENTER)

        y += r1H

        // Header Row 2
        val subTitles = listOf("দখলকার সংক্ষিপ্ত", "পরস্পর\nঅংশ", "খাজানা", "সেস্", null, "খাজানা", "সেস্")
        var cx = x
        subTitles.forEachIndexed { idx, label ->
            val cw = colW[idx]
            if (label != null) {
                canvas.drawRect(cx, y, cx + cw, y + r2H, kit.headerFill)
                canvas.drawRect(cx, y, cx + cw, y + r2H, kit.borderStroke)
                kit.drawTextInBox(canvas, label, cx, y, cw, kit.bodyPaint, 2f, 3f, Layout.Alignment.ALIGN_CENTER)
            }
            cx += cw
        }
        y += r2H

        // Data Box (generous ledger height matching Photo 1)
        val vals = listOf(
            BengaliNumberUtils.formatScript(ex["superior_khatian_occupant"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["superior_mutual_share"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["own_dey_khajana"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["own_dey_cess"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["upper_remarks"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["dharamote_son_khajana"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["dharamote_son_cess"].orEmpty(), useBn),
        )
        val dataH = maxOf(
            135f,
            vals.mapIndexed { idx, txt -> kit.measureTextHeight(txt, kit.bodyPaint, colW[idx], 3f, 4f) }.maxOrNull() ?: 135f
        )
        cx = x
        vals.forEachIndexed { idx, txt ->
            val cw = colW[idx]
            canvas.drawRect(cx, y, cx + cw, y + dataH, kit.borderStroke)
            val align = if (idx == 1 || idx == 2 || idx == 3 || idx == 5 || idx == 6) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL
            kit.drawTextInBox(canvas, txt, cx, y, cw, kit.bodyPaint, 3f, 5f, align)
            cx += cw
        }
        return y + dataH
    }

    private fun drawSaUpperTable(
        canvas: Canvas,
        x: Float,
        startY: Float,
        w: Float,
        kit: PdfPaintKit,
        ex: Map<String, String>,
        useBn: Boolean,
    ): Float {
        var y = startY
        // 9 sub-columns across w:
        // 0.085 (khatian batta) | 0.195 (occupant) | 0.08 (mutual share) | 0.08 (khajana) | 0.07 (cess) | 0.07 (edu cess) | 0.22 (remarks) | 0.10 (d-khaj) | 0.10 (d-cess)
        val colW = listOf(
            w * 0.085f, w * 0.195f, w * 0.08f,
            w * 0.08f, w * 0.07f, w * 0.07f,
            w * 0.22f,
            w * 0.10f, w * 0.10f,
        )
        val g1W = colW[0] + colW[1] + colW[2]
        val g2W = colW[3] + colW[4] + colW[5]
        val g3W = colW[6]
        val g4W = colW[7] + colW[8]

        val r1H = 22f
        val r2H = 26f

        canvas.drawRect(x, y, x + g1W, y + r1H, kit.headerFill)
        canvas.drawRect(x, y, x + g1W, y + r1H, kit.borderStroke)
        kit.drawTextInBox(canvas, "উপরিস্থ স্বত্বের", x, y, g1W, kit.labelBoldPaint, 2f, 4f, Layout.Alignment.ALIGN_CENTER)

        val g2X = x + g1W
        canvas.drawRect(g2X, y, g2X + g2W, y + r1H, kit.headerFill)
        canvas.drawRect(g2X, y, g2X + g2W, y + r1H, kit.borderStroke)
        kit.drawTextInBox(canvas, "অত্র স্বত্বের দেয়", g2X, y, g2W, kit.labelBoldPaint, 2f, 4f, Layout.Alignment.ALIGN_CENTER)

        val g3X = g2X + g2W
        canvas.drawRect(g3X, y, g3X + g3W, y + r1H + r2H, kit.headerFill)
        canvas.drawRect(g3X, y, g3X + g3W, y + r1H + r2H, kit.borderStroke)
        kit.drawTextInBox(canvas, "মন্তব্য", g3X, y + 14f, g3W, kit.labelBoldPaint, 2f, 4f, Layout.Alignment.ALIGN_CENTER)

        val g4X = g3X + g3W
        canvas.drawRect(g4X, y, g4X + g4W, y + r1H, kit.headerFill)
        canvas.drawRect(g4X, y, g4X + g4W, y + r1H, kit.borderStroke)
        kit.drawTextInBox(canvas, "ধারামতে ও কোন\nতারিখ হইতে", g4X, y, g4W, kit.smallNotePaint, 2f, 2f, Layout.Alignment.ALIGN_CENTER)

        y += r1H

        val subTitles = listOf("খতিয়ান নং\nমায় বাটা", "বিবরণ ও দখলকার\n[সংক্ষিপ্ত]", "পরস্পর\nঅংশ", "খাজানা", "সেস", "শিক্ষা\nসেস", null, "খাজানা", "সেস্")
        var cx = x
        subTitles.forEachIndexed { idx, label ->
            val cw = colW[idx]
            if (label != null) {
                canvas.drawRect(cx, y, cx + cw, y + r2H, kit.headerFill)
                canvas.drawRect(cx, y, cx + cw, y + r2H, kit.borderStroke)
                kit.drawTextInBox(canvas, label, cx, y, cw, kit.smallNotePaint, 1.5f, 3f, Layout.Alignment.ALIGN_CENTER)
            }
            cx += cw
        }
        y += r2H

        val vals = listOf(
            BengaliNumberUtils.formatScript(ex["superior_khatian_batta"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["superior_khatian_occupant"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["superior_mutual_share"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["own_dey_khajana"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["own_dey_cess"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["own_dey_edu_cess"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["upper_remarks"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["dharamote_date_khajana"].orEmpty(), useBn),
            BengaliNumberUtils.formatScript(ex["dharamote_date_cess"].orEmpty(), useBn),
        )
        val dataH = maxOf(
            135f,
            vals.mapIndexed { idx, txt -> kit.measureTextHeight(txt, kit.bodyPaint, colW[idx], 2.5f, 4f) }.maxOrNull() ?: 135f
        )
        cx = x
        vals.forEachIndexed { idx, txt ->
            val cw = colW[idx]
            canvas.drawRect(cx, y, cx + cw, y + dataH, kit.borderStroke)
            val align = if (idx in listOf(0, 2, 3, 4, 5, 7, 8)) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL
            kit.drawTextInBox(canvas, txt, cx, y, cw, kit.bodyPaint, 2.5f, 5f, align)
            cx += cw
        }
        return y + dataH
    }

    private fun drawCsSaLowerOwnershipTable(
        canvas: Canvas,
        x: Float,
        startY: Float,
        w: Float,
        suggestedRemH: Float,
        kit: PdfPaintKit,
        draft: KhatianDraftState,
        isSa: Boolean,
        useBn: Boolean,
    ): Float {
        var y = startY
        val colW = listOf(w * 0.25f, w * 0.08f, w * 0.27f, w * 0.08f, w * 0.32f)
        val headers = listOf(
            "অত্র স্বত্বের বিবরণ ও দখলকার",
            "অংশ",
            "অত্র স্বত্বের বিবরণ ও দখলকার",
            "অংশ",
            if (isSa) "অত্র স্বত্বের শ্রেণী (এবং\nবিশেষ নিয়ম ও অনুসঙ্গ)" else "অত্র স্বত্বের শ্রেণী এবং\nবিশেষ নিয়ম ও অনুসঙ্গ",
        )
        val headerH = headers.mapIndexed { i, t ->
            kit.measureTextHeight(t, kit.labelBoldPaint, colW[i], 3f, 4f)
        }.maxOrNull() ?: 28f

        var cx = x
        headers.forEachIndexed { i, title ->
            canvas.drawRect(cx, y, cx + colW[i], y + headerH, kit.headerFill)
            canvas.drawRect(cx, y, cx + colW[i], y + headerH, kit.borderStroke)
            kit.drawTextInBox(canvas, title, cx, y, colW[i], kit.labelBoldPaint, 3f, 4f, Layout.Alignment.ALIGN_CENTER)
            cx += colW[i]
        }
        y += headerH

        val ownersText = draft.owners.joinToString("\n\n") { o ->
            buildList {
                add(o.name)
                if (o.fatherOrHusbandName.isNotBlank()) add("পিং ${o.fatherOrHusbandName}")
                if (o.address.isNotBlank()) add("সাং ${o.address}")
                if (o.ownershipDetails.isNotBlank()) add(o.ownershipDetails)
            }.joinToString("\n")
        }
        val sharesText = draft.owners.joinToString("\n\n") { o ->
            BengaliNumberUtils.formatScript(o.hissa.ifBlank { "-" }, useBn)
        }
        val ex = draft.basicInfo.surveySpecificValues
        val cellVals = listOf(
            ownersText,
            sharesText,
            ex["second_occupant_details"].orEmpty(),
            BengaliNumberUtils.formatScript(ex["second_occupant_share"].orEmpty(), useBn),
            ex["tenure_class_rules"].orEmpty(),
        )

        val minBoxH = maxOf(155f, suggestedRemH - headerH)
        val bodyH = maxOf(
            minBoxH,
            cellVals.mapIndexed { i, t -> kit.measureTextHeight(t, kit.bodyPaint, colW[i], 4f, 6f) }.maxOrNull() ?: minBoxH
        )
        cx = x
        cellVals.forEachIndexed { i, t ->
            canvas.drawRect(cx, y, cx + colW[i], y + bodyH, kit.borderStroke)
            val align = if (i == 1 || i == 3) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL
            kit.drawTextInBox(canvas, t, cx, y, colW[i], kit.bodyPaint, 4f, 6f, align)
            cx += colW[i]
        }
        return y + bodyH
    }

    fun drawRsLandscapePage(
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
        val b = draft.basicInfo
        val ex = b.surveySpecificValues

        val kNoStr = BengaliNumberUtils.formatScript(b.khatianNumber.ifBlank { "---" }, useBn)
        val title = "আর এস খতিয়ান নং-$kNoStr"
        y += kit.drawTextInBox(canvas, title, m, y, w, kit.titlePaint, paddingV = 3f, align = Layout.Alignment.ALIGN_CENTER)

        // 5-item metadata strip matching Photo 3
        val metaItems = listOf(
            "জেলা : ${b.district.ifBlank { "-" }}",
            "থানা/উপজেলা : ${b.thana.ifBlank { b.upazila }.ifBlank { "-" }}",
            "মৌজা : ${b.mouzaName.ifBlank { "-" }}",
            "জেঃ এলঃ নং : ${BengaliNumberUtils.formatScript(b.jlNumber.ifBlank { "-" }, useBn)}",
            "রেঃ সাঃ নং : ${BengaliNumberUtils.formatScript(ex["resa_no"].orEmpty().ifBlank { "-" }, useBn)}",
        )
        val metaW = w / 5f
        val metaH = metaItems.maxOf { kit.measureTextHeight(it, kit.bodyPaint, metaW, 2f, 2f) }
        metaItems.forEachIndexed { idx, txt ->
            kit.drawTextInBox(canvas, txt, m + idx * metaW, y, metaW, kit.bodyPaint, 2f, 2f)
        }
        y += metaH + 4f

        // RS 9-Column Table (12 leaf columns including sub-columns 5(a)/5(b), 6(a)/6(b), 8(a)/8(b))
        val colW = rsColumnWidths(w)
        y = drawRsTableHeader(canvas, m, y, colW, kit)

        plan.rows.forEach { row ->
            val rowH = maxOf(
                19f,
                row.cells.mapIndexed { idx, txt ->
                    kit.measureTextHeight(txt, kit.bodyPaint, colW[idx], 2.5f, 3f)
                }.maxOrNull() ?: 19f
            )
            var cx = m
            row.cells.forEachIndexed { idx, txt ->
                val cw = colW[idx]
                canvas.drawRect(cx, y, cx + cw, y + rowH, kit.borderStroke)
                val align = if (idx == 0 || idx == 11) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_CENTER
                kit.drawTextInBox(canvas, txt, cx, y, cw, kit.bodyPaint, 2.5f, 3f, align)
                cx += cw
            }
            y += rowH
        }

        if (plan.showTotalsAndFooter) {
            // Footer Summary Row matching Photo 3
            val totalShareVal = draft.owners.mapNotNull { LandCalculationEngine.parseHissaFraction(it.hissa) }.sum()
            val totalShareStr = if (totalShareVal > 0.0) {
                BengaliNumberUtils.formatFixedDecimal(totalShareVal, 2, useBn)
            } else {
                BengaliNumberUtils.formatScript("১.০০", useBn)
            }
            var totalShotok = 0.0
            draft.dags.forEach { dag ->
                val v = BengaliNumberUtils.parseDoubleOrNull(dag.entitledArea)
                if (v != null && v > 0.0) {
                    totalShotok += LandCalculationEngine.toShotok(v, dag.landUnit)
                }
            }
            val (totAcre, totRem) = LandCalculationEngine.splitToAcreAndShotok(totalShotok)
            val totAcreStr = BengaliNumberUtils.formatScript("$totAcre", useBn)
            val totRemStr = BengaliNumberUtils.formatDecimal(totRem, 2, useBn)

            val noteLabel = buildString {
                append("ধারামতে নোট বা পরিবর্তন মায় মোকদ্দমা নং এবং সন")
                val note = ex["case_change_note"].orEmpty()
                if (note.isNotBlank()) append(": $note")
            }

            val footH = maxOf(28f, kit.measureTextHeight(noteLabel, kit.smallNotePaint, colW[0], 3f, 3f))
            // Col 0: Note
            canvas.drawRect(m, y, m + colW[0], y + footH, kit.borderStroke)
            kit.drawTextInBox(canvas, noteLabel, m, y, colW[0], kit.smallNotePaint, 3f, 3f)
            // Col 1: Total Share
            val c1X = m + colW[0]
            canvas.drawRect(c1X, y, c1X + colW[1], y + footH, kit.borderStroke)
            kit.drawTextInBox(canvas, totalShareStr, c1X, y, colW[1], kit.labelBoldPaint, 2f, 6f, Layout.Alignment.ALIGN_CENTER)
            // Span Cols 2..8: "মোট জমি :"
            val spanMidX = c1X + colW[1]
            val spanMidW = colW[2] + colW[3] + colW[4] + colW[5] + colW[6] + colW[7] + colW[8]
            canvas.drawRect(spanMidX, y, spanMidX + spanMidW, y + footH, kit.borderStroke)
            kit.drawTextInBox(canvas, "মোট জমি :", spanMidX, y, spanMidW, kit.labelBoldPaint, 6f, 6f, Layout.Alignment.ALIGN_OPPOSITE)
            // Col 9 & 10: Total Acre | Total Shotok
            val c9X = spanMidX + spanMidW
            canvas.drawRect(c9X, y, c9X + colW[9], y + footH, kit.borderStroke)
            kit.drawTextInBox(canvas, totAcreStr, c9X, y, colW[9], kit.labelBoldPaint, 2f, 6f, Layout.Alignment.ALIGN_CENTER)
            val c10X = c9X + colW[9]
            canvas.drawRect(c10X, y, c10X + colW[10], y + footH, kit.borderStroke)
            kit.drawTextInBox(canvas, totRemStr, c10X, y, colW[10], kit.labelBoldPaint, 2f, 6f, Layout.Alignment.ALIGN_CENTER)
            // Col 11: Empty remarks
            val c11X = c10X + colW[10]
            canvas.drawRect(c11X, y, c11X + colW[11], y + footH, kit.borderStroke)

            y += footH + 10f
            // Draft Signature Placeholder Strip (strictly labeled as draft)
            val sigLabels = listOf("নকলকারী (খসড়া)", "যাচাইকারী (খসড়া)", "তুলনাকারী (খসড়া)", "প্রস্তুতকারকের স্বাক্ষর")
            val sigW = w / 4f
            sigLabels.forEachIndexed { idx, lbl ->
                kit.drawTextInBox(canvas, lbl, m + idx * sigW, y, sigW, kit.smallNotePaint, 4f, 2f, Layout.Alignment.ALIGN_CENTER)
            }
        }
    }

    private fun drawRsTableHeader(
        canvas: Canvas,
        x: Float,
        startY: Float,
        colW: List<Float>,
        kit: PdfPaintKit,
    ): Float {
        var y = startY
        val r1H = 26f
        val r2H = 22f

        // Merged Top Header Row
        val groups = listOf(
            Triple(0, 1, "মালিক, অকৃষি প্রজা বা ইজারাদারের নাম ও ঠিকানা"),
            Triple(1, 1, "অংশ"),
            Triple(2, 1, "রাজস্ব"),
            Triple(3, 1, "দাগ নং"),
            Triple(4, 2, "জমির শ্রেণী"),
            Triple(6, 2, "দাগের মোট পরিমাণ"),
            Triple(8, 1, "দাগের মধ্যে অত্র খতিয়ানের অংশ"),
            Triple(9, 2, "অংশানুযায়ী জমির পরিমাণ"),
            Triple(11, 1, "দখল বিষয়ক বা অন্যান্য বিশেষ মন্তব্য"),
        )
        var gx = x
        groups.forEach { (startIdx, count, title) ->
            val gw = (0 until count).sumOf { colW[startIdx + it].toDouble() }.toFloat()
            canvas.drawRect(gx, y, gx + gw, y + r1H, kit.headerFill)
            canvas.drawRect(gx, y, gx + gw, y + r1H, kit.borderStroke)
            kit.drawTextInBox(canvas, title, gx, y, gw, kit.smallNotePaint, 2f, 3f, Layout.Alignment.ALIGN_CENTER)
            gx += gw
        }
        y += r1H

        // Subheader Row with exact Column Numbers ১..৯
        val subLabels = listOf(
            "১", "২", "৩", "৪",
            "কৃষি ৫(ক)", "অকৃষি ৫(খ)",
            "একর ৬(ক)", "শতাংশ ৬(খ)",
            "৭",
            "একর ৮(ক)", "শতাংশ ৮(খ)",
            "৯",
        )
        var cx = x
        subLabels.forEachIndexed { idx, lbl ->
            val cw = colW[idx]
            canvas.drawRect(cx, y, cx + cw, y + r2H, kit.headerFill)
            canvas.drawRect(cx, y, cx + cw, y + r2H, kit.borderStroke)
            kit.drawTextInBox(canvas, lbl, cx, y, cw, kit.smallNotePaint, 1.5f, 3f, Layout.Alignment.ALIGN_CENTER)
            cx += cw
        }
        return y + r2H
    }

    private fun rsColumnWidths(w: Float): List<Float> = listOf(
        w * 0.175f, // 1: Owner
        w * 0.055f, // 2: Share
        w * 0.055f, // 3: Revenue
        w * 0.065f, // 4: Dag No
        w * 0.065f, // 5(a): Agri
        w * 0.065f, // 5(b): Non-Agri
        w * 0.06f,  // 6(a): Total Acre
        w * 0.07f,  // 6(b): Total Shotok
        w * 0.09f,  // 7: Dag Share
        w * 0.06f,  // 8(a): Entitled Acre
        w * 0.07f,  // 8(b): Entitled Shotok
        w * 0.17f,  // 9: Remarks
    )

    private fun dagContinuationWeights(w: Float): List<Float> = listOf(
        w * 0.11f, w * 0.13f, w * 0.10f, w * 0.11f, w * 0.11f, w * 0.10f, w * 0.11f, w * 0.23f
    )

    private fun drawDagContinuationTable(
        canvas: Canvas,
        x: Float,
        startY: Float,
        w: Float,
        kit: PdfPaintKit,
        draft: KhatianDraftState,
        settings: SessionSettings,
        plan: PageContentPlan.ClassicLedgerPage,
    ) {
        var y = startY
        val colW = dagContinuationWeights(w)
        val headers = listOf("দাগ নং", "জমির শ্রেণি", "মোট একর", "মোট শতক", "অত্র অংশ", "প্রাপ্য একর", "প্রাপ্য শতক", "মন্তব্য")
        val hdrH = 26f
        var hx = x
        headers.forEachIndexed { idx, h ->
            canvas.drawRect(hx, y, hx + colW[idx], y + hdrH, kit.headerFill)
            canvas.drawRect(hx, y, hx + colW[idx], y + hdrH, kit.borderStroke)
            kit.drawTextInBox(canvas, h, hx, y, colW[idx], kit.labelBoldPaint, 2f, 4f, Layout.Alignment.ALIGN_CENTER)
            hx += colW[idx]
        }
        y += hdrH

        plan.rows.forEach { row ->
            val rH = maxOf(
                20f,
                row.cells.mapIndexed { i, t -> kit.measureTextHeight(t, kit.bodyPaint, colW[i], 3f, 3f) }.maxOrNull() ?: 20f
            )
            var rx = x
            row.cells.forEachIndexed { i, t ->
                canvas.drawRect(rx, y, rx + colW[i], y + rH, kit.borderStroke)
                kit.drawTextInBox(canvas, t, rx, y, colW[i], kit.bodyPaint, 3f, 3f, Layout.Alignment.ALIGN_CENTER)
                rx += colW[i]
            }
            y += rH
        }
    }

    private fun formatOwnerRsBlock(owner: OwnerEntry, useBn: Boolean): String {
        val parts = mutableListOf<String>()
        parts.add(owner.category.labelBn)
        parts.add(owner.name)
        if (owner.fatherOrHusbandName.isNotBlank()) parts.add("পিং/জং ${owner.fatherOrHusbandName}")
        if (owner.address.isNotBlank()) parts.add("সাং ${owner.address}")
        return parts.joinToString("\n")
    }

    private fun splitAreaPair(raw: String, unitLabel: String, useBn: Boolean): Pair<String, String> {
        val parsed = BengaliNumberUtils.parseDoubleOrNull(raw) ?: return ("" to "")
        val unit = com.example.util.LandUnit.entries.firstOrNull { it.labelBn == unitLabel }
            ?: com.example.util.LandUnit.SHOTOK
        val shotok = LandCalculationEngine.toShotok(parsed, unit)
        val (acres, rem) = LandCalculationEngine.splitToAcreAndShotok(shotok)
        val acreStr = if (acres > 0) BengaliNumberUtils.formatScript("$acres", useBn) else ""
        val shotokStr = BengaliNumberUtils.formatDecimal(rem, 2, useBn)
        return acreStr to shotokStr
    }
}
