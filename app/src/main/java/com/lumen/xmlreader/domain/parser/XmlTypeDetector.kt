package com.lumen.xmlreader.domain.parser

import com.lumen.xmlreader.domain.model.XmlDocumentType
import com.lumen.xmlreader.domain.model.XmlNode

object XmlTypeDetector {

    fun detect(rootNode: XmlNode): XmlDocumentType {
        val rootName = rootNode.nombre.lowercase()
        val allAttrs = rootNode.atributos

        if (rootName.contains("comprobante") ||
            allAttrs.values.any { it.contains("sat.gob.mx", ignoreCase = true) } ||
            rootNode.hijos.any { it.nombre.startsWith("cfdi:", ignoreCase = true) || it.nombre.startsWith("tfd:", ignoreCase = true) }
        ) {
            return XmlDocumentType.CFDI_SAT
        }

        if (rootName == "manifest" || rootNode.localName.equals("manifest", ignoreCase = true)) {
            return XmlDocumentType.ANDROID_MANIFEST
        }

        if (rootName == "resources" || rootNode.localName.equals("resources", ignoreCase = true)) {
            return XmlDocumentType.ANDROID_RESOURCES
        }

        val androidLayoutRoots = setOf(
            "linearlayout", "relativelayout", "framelayout", "constraintlayout",
            "coordinatorlayout", "scrollview", "nestedscrollview", "motionlayout",
            "viewgroup", "merge"
        )
        if (androidLayoutRoots.contains(rootNode.localName.lowercase()) ||
            allAttrs.keys.any { it.startsWith("android:") || it.startsWith("app:") }
        ) {
            if (rootName != "manifest" && rootName != "resources") {
                return XmlDocumentType.ANDROID_LAYOUT
            }
        }

        if (rootName == "svg" || rootNode.localName.equals("svg", ignoreCase = true)) {
            return XmlDocumentType.SVG_GRAPHIC
        }

        if (rootName == "project" && (
            allAttrs.containsKey("modelVersion") ||
            allAttrs.values.any { it.contains("maven.apache.org", ignoreCase = true) }
        )) {
            return XmlDocumentType.MAVEN_POM
        }

        if (rootName == "beans" || allAttrs.values.any { it.contains("springframework", ignoreCase = true) }) {
            return XmlDocumentType.SPRING_CONFIG
        }

        return XmlDocumentType.GENERIC_XML
    }
}
