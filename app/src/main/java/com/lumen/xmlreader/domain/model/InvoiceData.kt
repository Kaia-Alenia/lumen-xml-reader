package com.lumen.xmlreader.domain.model

data class InvoiceConcept(
    val clave: String,
    val cantidad: String,
    val unidad: String,
    val descripcion: String,
    val valorUnitario: String,
    val importe: String
)

data class InvoiceData(
    val folio: String = "1585",
    val serie: String = "",
    val fecha: String = "",
    val moneda: String = "MXN",
    val formaPago: String = "",
    val metodoPago: String = "",
    val emisorNombre: String = "",
    val emisorRfc: String = "",
    val emisorRegimen: String = "",
    val receptorNombre: String = "",
    val receptorRfc: String = "",
    val receptorDomicilio: String = "",
    val receptorUsoCfdi: String = "",
    val conceptos: List<InvoiceConcept> = emptyList(),
    val subtotal: String = "0.00",
    val iva: String = "0.00",
    val total: String = "0.00",
    val isCfdi: Boolean = true
)
