package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.PageSizeOption
import com.example.model.PrintOrientationPref
import com.example.model.SessionSettings
import com.example.model.SurveyType
import com.example.util.BengaliNumberUtils

data class PageDimensions(
    val widthPt: Int,
    val heightPt: Int,
    val marginPt: Float = 28f,
) {
    val contentWidth: Float get() = widthPt - marginPt * 2f
    val contentBottom: Float get() = heightPt - marginPt - 24f
}

object PdfPageGeometry {
    fun resolvePageDimensions(
        surveyType: SurveyType,
        settings: SessionSettings,
        customLandscape: Boolean,
        overrideOrientation: PrintOrientationPref? = null,
    ): PageDimensions {
        val base = when (settings.defaultPageSize) {
            PageSizeOption.A4 -> 595 to 842
            PageSizeOption.LEGAL -> 612 to 1008
            PageSizeOption.NATIVE -> when (surveyType) {
                SurveyType.CS, SurveyType.SA, SurveyType.RS -> 612 to 936 // Traditional Folio/Legal ledger
                else -> 595 to 842
            }
        }
        val wantLandscape = when (overrideOrientation ?: PrintOrientationPref.AUTO) {
            PrintOrientationPref.LANDSCAPE -> true
            PrintOrientationPref.PORTRAIT -> false
            PrintOrientationPref.AUTO -> when (surveyType) {
                SurveyType.CUSTOM -> customLandscape
                else -> surveyType.isLandscapeNative
            }
        }
        val w = if (wantLandscape) maxOf(base.first, base.second) else minOf(base.first, base.second)
        val h = if (wantLandscape) minOf(base.first, base.second) else maxOf(base.first, base.second)
        return PageDimensions(widthPt = w, heightPt = h)
    }
}

class PdfPaintKit(
    context: Context,
    inkSaverGrayscale: Boolean,
) {
    val bengaliTypeface: Typeface =
        ResourcesCompat.getFont(context, R.font.noto_sans_bengali) ?: Typeface.DEFAULT

    private val primaryColorInt = Color.BLACK
    private val accentSlateInt = Color.BLACK
    private val headerBgInt = Color.WHITE
    private val subHeaderBgInt = Color.WHITE

    val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bengaliTypeface
        textSize = 13.5f
        color = Color.BLACK
        isFakeBoldText = true
    }

    val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bengaliTypeface
        textSize = 9.5f
        color = Color.BLACK
        isFakeBoldText = true
    }

    val labelBoldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bengaliTypeface
        textSize = 8.4f
        color = Color.BLACK
        isFakeBoldText = true
    }

    val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bengaliTypeface
        textSize = 8.2f
        color = Color.BLACK
    }

    val smallNotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bengaliTypeface
        textSize = 7.4f
        color = Color.rgb(40, 40, 40)
    }

    val disclaimerFooterPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bengaliTypeface
        textSize = 7.2f
        color = Color.rgb(35, 35, 35)
        isFakeBoldText = true
    }

    val watermarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bengaliTypeface
        textSize = 30f
        color = Color.argb(14, 0, 0, 0)
        isFakeBoldText = true
    }

    val borderStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 0.85f
        strokeCap = Paint.Cap.SQUARE
        strokeJoin = Paint.Join.MITER
        color = Color.BLACK
    }

    val outerDoubleBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.6f
        strokeCap = Paint.Cap.SQUARE
        strokeJoin = Paint.Join.MITER
        color = Color.BLACK
    }

    val dashedBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.1f
        color = Color.BLACK
        pathEffect = DashPathEffect(floatArrayOf(5f, 3f), 0f)
    }

    val headerFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = headerBgInt
    }

    val subHeaderFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = subHeaderBgInt
    }

    fun buildLayout(
        text: String,
        paint: TextPaint,
        width: Float,
        align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ): StaticLayout {
        val safeWidth = maxOf(10, width.toInt())
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, safeWidth)
            .setAlignment(align)
            .setLineSpacing(0f, 1.12f)
            .setIncludePad(false)
            .build()
    }

    fun drawTextInBox(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Float,
        paint: TextPaint,
        paddingH: Float = 4f,
        paddingV: Float = 4f,
        align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ): Float {
        val innerW = maxOf(8f, width - paddingH * 2f)
        val layout = buildLayout(text, paint, innerW, align)
        canvas.save()
        canvas.translate(x + paddingH, y + paddingV)
        layout.draw(canvas)
        canvas.restore()
        return layout.height + paddingV * 2f
    }

    fun measureTextHeight(
        text: String,
        paint: TextPaint,
        width: Float,
        paddingH: Float = 4f,
        paddingV: Float = 4f,
    ): Float {
        val innerW = maxOf(8f, width - paddingH * 2f)
        val layout = buildLayout(text.ifEmpty { " " }, paint, innerW)
        return layout.height + paddingV * 2f
    }

    fun drawFrameAndFooter(
        canvas: Canvas,
        dim: PageDimensions,
        pageNumber: Int,
        totalPages: Int,
        useBengaliNumerals: Boolean,
        isCustomTemplate: Boolean,
    ) {
        val m = dim.marginPt
        val rect = RectF(m, m, dim.widthPt - m, dim.heightPt - m - 14f)
        if (isCustomTemplate) {
            canvas.drawRect(rect, dashedBorder)
        } else {
            canvas.drawRect(rect, outerDoubleBorder)
            canvas.drawRect(
                RectF(m + 2.5f, m + 2.5f, dim.widthPt - m - 2.5f, dim.heightPt - m - 16.5f),
                borderStroke,
            )
        }

        // Subtle diagonal draft watermark
        canvas.save()
        canvas.translate(dim.widthPt / 2f, dim.heightPt / 2f)
        canvas.rotate(-28f)
        val wm = "DIGITAL DRAFT • ডিজিটাল খসড়া"
        val wmWidth = watermarkPaint.measureText(wm)
        canvas.drawText(wm, -wmWidth / 2f, 0f, watermarkPaint)
        canvas.restore()

        // Mandatory non-official draft footer
        val pageStr = BengaliNumberUtils.formatScript("$pageNumber", useBengaliNumerals)
        val totalStr = BengaliNumberUtils.formatScript("$totalPages", useBengaliNumerals)
        val footerLeft =
            "ব্যবহারকারীর তৈরি ডিজিটাল খসড়া — সরকারি রেকর্ড বা প্রত্যয়িত নকল নয় (Digital Draft Only)"
        val footerRight = "পৃষ্ঠা $pageStr / $totalStr"

        val footerY = dim.heightPt - m - 2f
        canvas.drawText(footerLeft, m, footerY, disclaimerFooterPaint)
        val rightW = smallNotePaint.measureText(footerRight)
        canvas.drawText(footerRight, dim.widthPt - m - rightW, footerY, smallNotePaint)
    }
}
