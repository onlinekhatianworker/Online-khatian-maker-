package com.example.pdf

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import com.example.model.KhatianDraftState
import com.example.model.SessionSettings
import java.io.FileOutputStream

class KhatianPrintAdapter(
    private val context: Context,
    private val draft: KhatianDraftState,
    private val settings: SessionSettings,
    private val jobName: String,
) : PrintDocumentAdapter() {

    private var computedPageCount: Int = 1

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val engine = KhatianPdfEngine(context)
        val kit = PdfPaintKit(context, settings.printInkSaverGrayscale)
        val dim = PdfPageGeometry.resolvePageDimensions(
            surveyType = draft.surveyType,
            settings = settings,
            customLandscape = draft.customConfig.useLandscape,
            overrideOrientation = settings.printOrientationPref,
        )
        computedPageCount = engine.buildPagePlans(draft, settings, dim, kit).size

        val info = PrintDocumentInfo.Builder("$jobName.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(computedPageCount)
            .build()

        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback,
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onWriteCancelled()
            return
        }

        try {
            FileOutputStream(destination.fileDescriptor).use { out ->
                val engine = KhatianPdfEngine(context)
                engine.writePdfToOutputStream(
                    draft = draft,
                    settings = settings,
                    outputStream = out,
                    overrideOrientation = settings.printOrientationPref,
                )
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message ?: "Print failed")
        }
    }
}
