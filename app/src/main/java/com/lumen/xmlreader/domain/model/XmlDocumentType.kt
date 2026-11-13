package com.lumen.xmlreader.domain.model

enum class XmlDocumentType(val label: String, val badgeColorHex: Long) {
    CFDI_SAT("CFDI Factura SAT", 0xFF00897B),
    ANDROID_MANIFEST("Android Manifest", 0xFF3DDC84),
    ANDROID_LAYOUT("Android Layout UI", 0xFF1E88E5),
    ANDROID_RESOURCES("Android Resources", 0xFF5E35B1),
    MAVEN_POM("Maven POM", 0xFFE53935),
    SVG_GRAPHIC("Vector SVG", 0xFFFB8C00),
    SPRING_CONFIG("Spring Config", 0xFF43A047),
    GENERIC_XML("XML Genérico", 0xFF757575)
}
