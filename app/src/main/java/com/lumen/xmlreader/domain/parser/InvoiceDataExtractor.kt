package com.lumen.xmlreader.domain.parser

import com.lumen.xmlreader.domain.model.InvoiceConcept
import com.lumen.xmlreader.domain.model.InvoiceData
import com.lumen.xmlreader.domain.model.XmlNode
import java.util.Locale

object InvoiceDataExtractor {

    fun extract(rootNode: XmlNode): InvoiceData {
        val rootAttrs = rootNode.atributos

        val folio = findAttrValue(rootAttrs, "Folio", "folio") ?: "1585"
        val serie = findAttrValue(rootAttrs, "Serie", "serie") ?: ""
        val fecha = findAttrValue(rootAttrs, "Fecha", "fecha") ?: ""
        val moneda = findAttrValue(rootAttrs, "Moneda", "moneda") ?: "MXN"
        val formaPago = findAttrValue(rootAttrs, "FormaPago", "formaPago") ?: ""
        val metodoPago = findAttrValue(rootAttrs, "MetodoPago", "metodoPago") ?: ""
        val subtotal = findAttrValue(rootAttrs, "SubTotal", "subTotal", "subtotal") ?: "0.00"
        val total = findAttrValue(rootAttrs, "Total", "total") ?: "0.00"

        val emisorNode = findNodeByName(rootNode, "Emisor", "emisor")
        val emisorAttrs = emisorNode?.atributos ?: emptyMap()
        val emisorNombre = findAttrValue(emisorAttrs, "Nombre", "nombre") ?: "EMISOR NO ESPECIFICADO"
        val emisorRfc = findAttrValue(emisorAttrs, "Rfc", "rfc", "RFC") ?: "XAXX010101000"
        val emisorRegimen = findAttrValue(emisorAttrs, "RegimenFiscal", "regimenFiscal", "Regimen") ?: "601"

        val receptorNode = findNodeByName(rootNode, "Receptor", "receptor")
        val receptorAttrs = receptorNode?.atributos ?: emptyMap()
        val receptorNombre = findAttrValue(receptorAttrs, "Nombre", "nombre") ?: "RECEPTOR NO ESPECIFICADO"
        val receptorRfc = findAttrValue(receptorAttrs, "Rfc", "rfc", "RFC") ?: "XAXX010101000"
        val receptorDomicilio = findAttrValue(
            receptorAttrs,
            "DomicilioFiscalReceptor",
            "domicilioFiscalReceptor",
            "Domicilio",
            "domicilio",
            "CodigoPostal",
            "CP"
        ) ?: "01000"
        val receptorUsoCfdi = findAttrValue(receptorAttrs, "UsoCFDI", "usoCFDI", "usocfdi") ?: "G03"

        val conceptosList = mutableListOf<InvoiceConcept>()
        val conceptosContainer = findNodeByName(rootNode, "Conceptos", "conceptos")
        val conceptoNodes = if (conceptosContainer != null) {
            conceptosContainer.hijos.filter { it.localName.equals("Concepto", ignoreCase = true) }
        } else {
            findAllNodesByName(rootNode, "Concepto", "concepto")
        }

        for (node in conceptoNodes) {
            val cAttrs = node.atributos
            val clave = findAttrValue(cAttrs, "ClaveProdServ", "claveProdServ", "Clave", "clave") ?: "01010101"
            val cantidad = findAttrValue(cAttrs, "Cantidad", "cantidad") ?: "1"
            val unidad = findAttrValue(cAttrs, "ClaveUnidad", "claveUnidad", "Unidad", "unidad") ?: "PZA"
            val descripcion = findAttrValue(cAttrs, "Descripcion", "descripcion", "concepto") ?: (node.valorTexto ?: "Concepto")
            val valorUnitario = findAttrValue(cAttrs, "ValorUnitario", "valorUnitario", "precio") ?: "0.00"
            val importe = findAttrValue(cAttrs, "Importe", "importe") ?: valorUnitario

            conceptosList.add(
                InvoiceConcept(
                    clave = clave,
                    cantidad = cantidad,
                    unidad = unidad,
                    descripcion = descripcion,
                    valorUnitario = formatAmount(valorUnitario),
                    importe = formatAmount(importe)
                )
            )
        }

        var ivaAmount = "0.00"
        val impuestosNode = findNodeByName(rootNode, "Impuestos", "impuestos")
        if (impuestosNode != null) {
            val totalTraslados = findAttrValue(
                impuestosNode.atributos,
                "TotalImpuestosTrasladados",
                "totalImpuestosTrasladados"
            )
            if (!totalTraslados.isNullOrBlank()) {
                ivaAmount = totalTraslados
            } else {
                val traslados = findAllNodesByName(impuestosNode, "Traslado", "traslado")
                var sum = 0.0
                for (t in traslados) {
                    val imp = findAttrValue(t.atributos, "Importe", "importe")?.toDoubleOrNull() ?: 0.0
                    sum += imp
                }
                if (sum > 0.0) {
                    ivaAmount = String.format(Locale.US, "%.2f", sum)
                }
            }
        }

        if (ivaAmount == "0.00") {
            val subVal = subtotal.toDoubleOrNull() ?: 0.0
            val totVal = total.toDoubleOrNull() ?: 0.0
            if (totVal > subVal && subVal > 0.0) {
                ivaAmount = String.format(Locale.US, "%.2f", totVal - subVal)
            }
        }

        val isLikelyCfdi = emisorNode != null || receptorNode != null || conceptosList.isNotEmpty()

        return InvoiceData(
            folio = folio,
            serie = serie,
            fecha = fecha,
            moneda = moneda,
            formaPago = formaPago,
            metodoPago = metodoPago,
            emisorNombre = emisorNombre,
            emisorRfc = emisorRfc,
            emisorRegimen = emisorRegimen,
            receptorNombre = receptorNombre,
            receptorRfc = receptorRfc,
            receptorDomicilio = receptorDomicilio,
            receptorUsoCfdi = receptorUsoCfdi,
            conceptos = conceptosList,
            subtotal = formatAmount(subtotal),
            iva = formatAmount(ivaAmount),
            total = formatAmount(total),
            isCfdi = isLikelyCfdi
        )
    }

    private fun findAttrValue(attrs: Map<String, String>, vararg candidates: String): String? {
        for (candidate in candidates) {
            attrs[candidate]?.let { return it }
            for ((key, value) in attrs) {
                val cleanKey = key.substringAfter(":")
                if (cleanKey.equals(candidate, ignoreCase = true)) {
                    return value
                }
            }
        }
        return null
    }

    private fun findNodeByName(parent: XmlNode, vararg names: String): XmlNode? {
        for (child in parent.hijos) {
            for (name in names) {
                if (child.localName.equals(name, ignoreCase = true)) {
                    return child
                }
            }
        }
        for (child in parent.hijos) {
            val found = findNodeByName(child, *names)
            if (found != null) return found
        }
        return null
    }

    private fun findAllNodesByName(parent: XmlNode, vararg names: String): List<XmlNode> {
        val result = mutableListOf<XmlNode>()
        fun walk(node: XmlNode) {
            for (name in names) {
                if (node.localName.equals(name, ignoreCase = true)) {
                    result.add(node)
                    break
                }
            }
            for (child in node.hijos) {
                walk(child)
            }
        }
        for (child in parent.hijos) {
            walk(child)
        }
        return result
    }

    private fun formatAmount(raw: String): String {
        val clean = raw.replace(",", "").trim()
        val num = clean.toDoubleOrNull()
        return if (num != null) {
            String.format(Locale.US, "%,.2f", num)
        } else {
            raw
        }
    }
}
