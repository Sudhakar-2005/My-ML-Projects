package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ui.viewmodel.ResearchReport
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {
    fun generateAndSharePdf(context: Context, report: ResearchReport) {
        try {
            val pdfDocument = PdfDocument()
            // A4 page dimensions in postscript points (1/72 of an inch): 595 x 842
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paint styles
            val brandNavy = Color.rgb(0, 29, 54)
            val accentBlue = Color.rgb(0, 97, 164)
            val textGray = Color.rgb(100, 116, 139)
            val borderLight = Color.rgb(221, 226, 234)

            val paint = Paint()

            val titlePaint = Paint().apply {
                color = brandNavy
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = textGray
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = accentBlue
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val boldPaint = Paint().apply {
                color = brandNavy
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.rgb(26, 28, 30)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            var y = 50f

            // Top Header Banner Area
            paint.color = Color.rgb(243, 244, 246)
            canvas.drawRoundRect(35f, 35f, 560f, 95f, 8f, 8f, paint)

            // Header Logo Text
            canvas.drawText("SHARROW.AI INSTITUTIONAL INTELLIGENCE", 50f, 62f, titlePaint.apply { textSize = 16f })
            canvas.drawText("Premium Generative Research Report & Portfolio Risk Diagnostics", 50f, 78f, subtitlePaint)
            y = 120f

            // Report Title Section
            paint.color = brandNavy
            canvas.drawText("EQUITY RESEARCH BRIEFING", 40f, y, titlePaint.apply { textSize = 20f })
            y += 22f
            canvas.drawText("Target Asset: ${report.companyName} (${report.symbol})", 40f, y, textPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            y += 25f

            // Divider Line
            paint.color = borderLight
            paint.strokeWidth = 1.5f
            canvas.drawLine(40f, y, 555f, y, paint)
            y += 25f

            // Key Metrics Card
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(40f, y, 555f, y + 80f, 6f, 6f, paint)

            // Fill Metrics inside card
            val cy = y + 20f
            canvas.drawText("VALUATION:", 55f, cy, boldPaint)
            val valText = report.valuationRating.uppercase()
            val valColor = when {
                valText.contains("UNDER") -> Color.rgb(46, 125, 50)  // Green
                valText.contains("OVER") -> Color.rgb(198, 40, 40)   // Red
                else -> Color.rgb(0, 97, 164)                       // Blue
            }
            boldPaint.color = valColor
            canvas.drawText(valText, 170f, cy, boldPaint)
            boldPaint.color = brandNavy

            canvas.drawText("CONFIDENCE SCORE:", 310f, cy, boldPaint)
            canvas.drawText("${report.confidenceScore}% (Institutional Conviction)", 445f, cy, textPaint)

            canvas.drawText("DEBT TO EQUITY:", 55f, cy + 22f, boldPaint)
            canvas.drawText(report.debtEquity, 170f, cy + 22f, textPaint)

            canvas.drawText("GROWTH (REV / PROF):", 310f, cy + 22f, boldPaint)
            canvas.drawText("${report.revenueGrowth} / ${report.profitGrowth}", 445f, cy + 22f, textPaint)

            canvas.drawText("GENERATED TOKEN:", 55f, cy + 44f, boldPaint)
            canvas.drawText(report.pdfExportToken ?: "SH_REP_DEFAULT", 170f, cy + 44f, subtitlePaint.apply { textSize = 9f })
            subtitlePaint.textSize = 10f

            y += 105f

            // Executive Summary
            canvas.drawText("1. EXECUTIVE OPERATIONS SUMMARY", 40f, y, headerPaint)
            y += 18f
            y = drawMultilineText(canvas, report.overview, 40f, y, 515, textPaint)
            y += 20f

            // Strategic SWOT Analysis
            canvas.drawText("2. STRATEGIC SWOT RADAR ANALYSIS", 40f, y, headerPaint)
            y += 20f

            report.swotAnalysis.forEach { (key, items) ->
                // Badge category
                paint.color = when (key.uppercase()) {
                    "STRENGTHS" -> Color.rgb(232, 253, 240)
                    "WEAKNESSES" -> Color.rgb(253, 242, 242)
                    "OPPORTUNITIES" -> Color.rgb(239, 246, 255)
                    else -> Color.rgb(254, 243, 199)
                }
                val labelColor = when (key.uppercase()) {
                    "STRENGTHS" -> Color.rgb(46, 125, 50)
                    "WEAKNESSES" -> Color.rgb(198, 40, 40)
                    "OPPORTUNITIES" -> Color.rgb(0, 97, 164)
                    else -> Color.rgb(180, 83, 9)
                }

                canvas.drawRoundRect(50f, y - 11f, 160f, y + 6f, 4f, 4f, paint)
                boldPaint.color = labelColor
                canvas.drawText(key.uppercase(), 58f, y, boldPaint.apply { textSize = 9f })
                boldPaint.color = brandNavy
                boldPaint.textSize = 11f

                y += 18f
                items.forEach { item ->
                    y = drawMultilineText(canvas, "•  $item", 60f, y, 485, textPaint)
                }
                y += 10f
            }
            y += 10f

            // Investment Thesis & Valuation
            canvas.drawText("3. CORE INVESTMENT THESIS & CONVICTION", 40f, y, headerPaint)
            y += 18f
            y = drawMultilineText(canvas, report.investmentThesis, 40f, y, 515, textPaint)
            y += 20f

            // Risk Assessment & Comparative Standing
            canvas.drawText("4. STRATEGIC RISKS & COMPETITIVE BENCHMARKING", 40f, y, headerPaint)
            y += 18f

            canvas.drawText("Key Risks Node:", 50f, y, boldPaint)
            y += 16f
            y = drawMultilineText(canvas, report.riskAnalysis, 50f, y, 505, textPaint)
            y += 10f

            canvas.drawText("Competitive Peer Benchmarking:", 50f, y, boldPaint)
            y += 16f
            y = drawMultilineText(canvas, report.peerComparisonText, 50f, y, 505, textPaint)
            y += 30f

            // Footer disclaimer
            paint.color = borderLight
            canvas.drawLine(40f, y, 555f, y, paint)
            y += 18f
            val disclaimer = "Disclaimer: Sharrow.ai generative reports are computed based on artificial intelligence macro models and publicly aggregated indexes. Past outcomes are not guarantee indicators of absolute future asset appreciation cycles."
            drawMultilineText(canvas, disclaimer, 40f, y, 515, subtitlePaint.apply { textSize = 8f })

            pdfDocument.finishPage(page)

            // Save PDF to cache directory
            val fileName = "Sharrow_Research_${report.symbol}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            // Share PDF report using FileProvider
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sharrow Research Briefing: ${report.symbol}")
                putExtra(Intent.EXTRA_TEXT, "Generated institutional grade briefing report for ${report.companyName} (${report.symbol}). Created by Sharrow.ai.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export AI Research PDF Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            // Also copy to public Downloads folder for offline viewing persistence
            saveToDownloads(context, file, "Sharrow_Research_${report.symbol}.pdf")

        } catch (e: Exception) {
            Log.e("PdfReportGenerator", "Failed to generate PDF", e)
            Toast.makeText(context, "PDF compilation failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        width: Int,
        paint: Paint
    ): Float {
        var y = startY
        val words = text.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val testWidth = paint.measureText(testLine)
            if (testWidth > width) {
                canvas.drawText(line, x, y, paint)
                y += paint.textSize + 4f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, x, y, paint)
            y += paint.textSize + 4f
        }
        return y
    }

    private fun saveToDownloads(context: Context, cacheFile: File, fileName: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val destinationFile = File(downloadsDir, fileName)
            cacheFile.inputStream().use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(context, "Saved offline report to Downloads/$fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("PdfReportGenerator", "Failed to save to downloads directory", e)
        }
    }
}
