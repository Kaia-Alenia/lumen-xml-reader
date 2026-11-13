package com.lumen.xmlreader.domain.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.lumen.xmlreader.domain.model.InvoiceData
import java.io.File
import java.io.FileOutputStream

object PdfInvoiceGenerator {

    fun generateAndShare(context: Context, invoice: InvoiceData) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawInvoice(canvas, invoice, pageWidth, pageHeight)

        document.finishPage(page)

        val fileName = "LUMEN_Factura_1585.pdf"
        val pdfFile = File(context.cacheDir, fileName)

        try {
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (_: Exception) {
        } finally {
            document.close()
        }

        sharePdf(context, pdfFile)
    }

    private fun drawInvoice(canvas: Canvas, invoice: InvoiceData, width: Int, height: Int) {
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)

        paintFill.style = Paint.Style.FILL
        paintStroke.style = Paint.Style.STROKE

        val margin = 36f
        val contentWidth = width - (margin * 2)

        val logoSize = 34f
        val logoRect = RectF(margin, 34f, margin + logoSize, 34f + logoSize)
        paintFill.color = 0xFF1E1B4B.toInt()
        canvas.drawRoundRect(logoRect, 7f, 7f, paintFill)

        paintFill.color = 0x66FBBF24
        canvas.drawCircle(margin + (logoSize / 2f), 34f + (logoSize / 2f), 10f, paintFill)

        paintStroke.color = 0xFFFFFFFF.toInt()
        paintStroke.strokeWidth = 2f
        paintStroke.strokeCap = Paint.Cap.ROUND
        paintStroke.strokeJoin = Paint.Join.ROUND

        val leftBracket = android.graphics.Path().apply {
            moveTo(margin + 13f, 45f)
            lineTo(margin + 7f, 51f)
            lineTo(margin + 13f, 57f)
        }
        canvas.drawPath(leftBracket, paintStroke)

        val rightBracket = android.graphics.Path().apply {
            moveTo(margin + 21f, 45f)
            lineTo(margin + 27f, 51f)
            lineTo(margin + 21f, 57f)
        }
        canvas.drawPath(rightBracket, paintStroke)

        paintFill.color = 0xFFFFFBEB.toInt()
        canvas.drawCircle(margin + (logoSize / 2f), 34f + (logoSize / 2f), 3f, paintFill)

        val textStartX = margin + logoSize + 10f
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 17f
        paintText.color = 0xFF1E1B4B.toInt()
        canvas.drawText("Lumen", textStartX, 50f, paintText)

        paintText.textSize = 7.5f
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.letterSpacing = 0.15f
        paintText.color = 0xFF4338CA.toInt()
        canvas.drawText("XML READER", textStartX, 63f, paintText)
        paintText.letterSpacing = 0f

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 14f
        paintText.color = 0xFF0F172A.toInt()
        val folioText = "FACTURA #${invoice.folio.ifBlank { "1585" }}"
        val folioWidth = paintText.measureText(folioText)
        canvas.drawText(folioText, width - margin - folioWidth, 50f, paintText)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paintText.textSize = 8.5f
        paintText.color = 0xFF64748B.toInt()
        val dateText = if (invoice.fecha.isNotBlank()) "Fecha: ${invoice.fecha}" else "Moneda: ${invoice.moneda}"
        val dateWidth = paintText.measureText(dateText)
        canvas.drawText(dateText, width - margin - dateWidth, 64f, paintText)

        paintStroke.strokeWidth = 1f
        paintStroke.color = 0xFFCBD5E1.toInt()
        canvas.drawLine(margin, 76f, width - margin, 76f, paintStroke)

        val cardWidth = (contentWidth - 12f) / 2f
        val cardHeight = 90f
        val cardTop = 88f

        paintFill.color = 0xFFF8FAFC.toInt()
        val emisorRect = RectF(margin, cardTop, margin + cardWidth, cardTop + cardHeight)
        canvas.drawRoundRect(emisorRect, 6f, 6f, paintFill)
        paintStroke.color = 0xFFE2E8F0.toInt()
        canvas.drawRoundRect(emisorRect, 6f, 6f, paintStroke)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 9f
        paintText.color = 0xFF00897B.toInt()
        canvas.drawText("EMISOR", margin + 10f, cardTop + 16f, paintText)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 8f
        paintText.color = 0xFF0F172A.toInt()
        val emisorNomSafe = if (invoice.emisorNombre.length > 34) invoice.emisorNombre.take(32) + "..." else invoice.emisorNombre
        canvas.drawText(emisorNomSafe, margin + 10f, cardTop + 32f, paintText)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paintText.textSize = 7.5f
        paintText.color = 0xFF475569.toInt()
        canvas.drawText("RFC: ${invoice.emisorRfc}", margin + 10f, cardTop + 48f, paintText)
        val regSafe = if (invoice.emisorRegimen.length > 34) invoice.emisorRegimen.take(32) + "..." else invoice.emisorRegimen
        canvas.drawText("Régimen: $regSafe", margin + 10f, cardTop + 62f, paintText)

        val receptorLeft = margin + cardWidth + 12f
        val receptorRect = RectF(receptorLeft, cardTop, receptorLeft + cardWidth, cardTop + cardHeight)
        paintFill.color = 0xFFF8FAFC.toInt()
        canvas.drawRoundRect(receptorRect, 6f, 6f, paintFill)
        canvas.drawRoundRect(receptorRect, 6f, 6f, paintStroke)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 9f
        paintText.color = 0xFF00897B.toInt()
        canvas.drawText("RECEPTOR", receptorLeft + 10f, cardTop + 16f, paintText)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 8f
        paintText.color = 0xFF0F172A.toInt()
        val receptorNomSafe = if (invoice.receptorNombre.length > 34) invoice.receptorNombre.take(32) + "..." else invoice.receptorNombre
        canvas.drawText(receptorNomSafe, receptorLeft + 10f, cardTop + 32f, paintText)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paintText.textSize = 7.5f
        paintText.color = 0xFF475569.toInt()
        canvas.drawText("RFC: ${invoice.receptorRfc}", receptorLeft + 10f, cardTop + 48f, paintText)
        canvas.drawText("Domicilio: ${invoice.receptorDomicilio}", receptorLeft + 10f, cardTop + 62f, paintText)
        if (invoice.receptorUsoCfdi.isNotBlank()) {
            canvas.drawText("Uso CFDI: ${invoice.receptorUsoCfdi}", receptorLeft + 10f, cardTop + 76f, paintText)
        }

        val tableTop = 192f
        val tableHeaderHeight = 22f

        paintFill.color = 0xFF0F172A.toInt()
        val thRect = RectF(margin, tableTop, width - margin, tableTop + tableHeaderHeight)
        canvas.drawRoundRect(thRect, 4f, 4f, paintFill)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 8f
        paintText.color = 0xFFFFFFFF.toInt()

        val colCant = margin + 8f
        val colClave = margin + 45f
        val colDesc = margin + 115f
        val colUnit = width - margin - 140f
        val colImp = width - margin - 60f

        canvas.drawText("CANT", colCant, tableTop + 14f, paintText)
        canvas.drawText("CLAVE", colClave, tableTop + 14f, paintText)
        canvas.drawText("DESCRIPCIÓN", colDesc, tableTop + 14f, paintText)
        canvas.drawText("P. UNITARIO", colUnit, tableTop + 14f, paintText)
        canvas.drawText("IMPORTE", colImp, tableTop + 14f, paintText)

        var currentRowY = tableTop + tableHeaderHeight
        val rowHeight = 22f

        paintStroke.color = 0xFFF1F5F9.toInt()
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paintText.textSize = 7.5f
        paintText.color = 0xFF1E293B.toInt()

        val maxRows = 16
        val displayItems = invoice.conceptos.take(maxRows)

        for (item in displayItems) {
            paintFill.color = if ((displayItems.indexOf(item) % 2) == 0) 0xFFFFFFFF.toInt() else 0xFFF8FAFC.toInt()
            canvas.drawRect(margin, currentRowY, width - margin, currentRowY + rowHeight, paintFill)

            paintStroke.color = 0xFFE2E8F0.toInt()
            paintStroke.strokeWidth = 0.5f
            canvas.drawLine(margin, currentRowY + rowHeight, width - margin, currentRowY + rowHeight, paintStroke)

            canvas.drawText(item.cantidad, colCant, currentRowY + 14f, paintText)
            val claveSafe = if (item.clave.length > 10) item.clave.take(8) + ".." else item.clave
            canvas.drawText(claveSafe, colClave, currentRowY + 14f, paintText)

            val descSafe = if (item.descripcion.length > 38) item.descripcion.take(36) + "..." else item.descripcion
            canvas.drawText(descSafe, colDesc, currentRowY + 14f, paintText)

            val unitText = "$ ${item.valorUnitario}"
            canvas.drawText(unitText, colUnit, currentRowY + 14f, paintText)

            val impText = "$ ${item.importe}"
            canvas.drawText(impText, colImp, currentRowY + 14f, paintText)

            currentRowY += rowHeight
        }

        val totalsTop = currentRowY + 16f
        val totalsWidth = 210f
        val totalsLeft = width - margin - totalsWidth

        paintText.textSize = 8.5f
        paintText.color = 0xFF64748B.toInt()
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("SubTotal:", totalsLeft, totalsTop + 12f, paintText)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.color = 0xFF0F172A.toInt()
        val subStr = "$ ${invoice.subtotal} ${invoice.moneda}"
        val subW = paintText.measureText(subStr)
        canvas.drawText(subStr, width - margin - subW, totalsTop + 12f, paintText)

        paintText.textSize = 8.5f
        paintText.color = 0xFF64748B.toInt()
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("IVA:", totalsLeft, totalsTop + 28f, paintText)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.color = 0xFF0F172A.toInt()
        val ivaStr = "$ ${invoice.iva} ${invoice.moneda}"
        val ivaW = paintText.measureText(ivaStr)
        canvas.drawText(ivaStr, width - margin - ivaW, totalsTop + 28f, paintText)

        val totalBoxTop = totalsTop + 38f
        val totalBoxHeight = 36f
        val totalBoxRect = RectF(totalsLeft - 6f, totalBoxTop, width - margin, totalBoxTop + totalBoxHeight)

        paintFill.color = 0xFFFEF3C7.toInt()
        canvas.drawRoundRect(totalBoxRect, 6f, 6f, paintFill)

        paintStroke.color = 0xFFF59E0B.toInt()
        paintStroke.strokeWidth = 1.5f
        canvas.drawRoundRect(totalBoxRect, 6f, 6f, paintStroke)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 10f
        paintText.color = 0xFF92400E.toInt()
        canvas.drawText("TOTAL", totalsLeft + 6f, totalBoxTop + 22f, paintText)

        paintText.textSize = 13f
        paintText.color = 0xFFB45309.toInt()
        val totStr = "$ ${invoice.total} ${invoice.moneda}"
        val totW = paintText.measureText(totStr)
        canvas.drawText(totStr, width - margin - 10f - totW, totalBoxTop + 23f, paintText)

        val footerY = height - 28f
        paintStroke.color = 0xFFE2E8F0.toInt()
        paintStroke.strokeWidth = 0.5f
        canvas.drawLine(margin, footerY - 12f, width - margin, footerY - 12f, paintStroke)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paintText.textSize = 7f
        paintText.color = 0xFF94A3B8.toInt()
        val footerNotice = "Documento generado por LUMEN • Alenia Studios • contact.aleniastudios@gmail.com"
        val fnWidth = paintText.measureText(footerNotice)
        canvas.drawText(footerNotice, (width - fnWidth) / 2f, footerY, paintText)
    }

    private fun sharePdf(context: Context, file: File) {
        val authority = "${context.packageName}.provider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Factura LUMEN")
            putExtra(Intent.EXTRA_TEXT, "Factura generada desde LUMEN XML Reader")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Compartir Factura PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
