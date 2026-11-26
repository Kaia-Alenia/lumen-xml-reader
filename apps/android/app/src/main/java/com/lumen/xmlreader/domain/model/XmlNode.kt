package com.lumen.xmlreader.domain.model

import java.util.UUID

data class XmlNode(
    val nombre: String,
    val atributos: Map<String, String>,
    val valorTexto: String?,
    val hijos: List<XmlNode>,
    val nivel: Int,
    val id: String = UUID.randomUUID().toString(),
    val namespacePrefix: String? = null,
    val localName: String = nombre.substringAfter(":")
) {
    fun matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        val lower = query.lowercase().trim()
        if (nombre.lowercase().contains(lower)) return true
        if (valorTexto?.lowercase()?.contains(lower) == true) return true
        return atributos.any { (k, v) ->
            k.lowercase().contains(lower) || v.lowercase().contains(lower)
        }
    }

    fun countTotalNodes(): Int {
        var count = 1
        for (hijo in hijos) {
            count += hijo.countTotalNodes()
        }
        return count
    }
}
