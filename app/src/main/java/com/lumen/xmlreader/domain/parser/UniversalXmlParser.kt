package com.lumen.xmlreader.domain.parser

import android.util.Xml
import com.lumen.xmlreader.domain.model.XmlDocumentInfo
import com.lumen.xmlreader.domain.model.XmlNode
import com.lumen.xmlreader.domain.model.XmlParseResult
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayDeque

object UniversalXmlParser {

    private class NodeBuilder(
        val nombre: String,
        val atributos: Map<String, String>,
        val nivel: Int,
        val namespacePrefix: String? = null
    ) {
        val textBuilder = java.lang.StringBuilder()
        val children = mutableListOf<XmlNode>()
    }

    fun parse(
        inputStream: InputStream,
        fileName: String,
        fileSizeFormatted: String,
        uriString: String
    ): XmlParseResult {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()

        return try {
            val reader = InputStreamReader(inputStream, Charsets.UTF_8)
            parser.setInput(reader)

            val stack = ArrayDeque<NodeBuilder>()
            var rootNode: XmlNode? = null
            var eventType = parser.eventType

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val prefix = parser.prefix
                        val rawName = parser.name ?: "desconocido"
                        val fullName = if (!prefix.isNullOrBlank()) "$prefix:$rawName" else rawName

                        val attrMap = linkedMapOf<String, String>()
                        val attrCount = parser.attributeCount
                        for (i in 0 until attrCount) {
                            val attrPrefix = parser.getAttributePrefix(i)
                            val attrName = parser.getAttributeName(i)
                            val qualifiedAttrName = if (!attrPrefix.isNullOrBlank()) "$attrPrefix:$attrName" else attrName
                            val attrValue = parser.getAttributeValue(i) ?: ""
                            attrMap[qualifiedAttrName] = attrValue
                        }

                        val currentLevel = stack.size
                        val builder = NodeBuilder(
                            nombre = fullName,
                            atributos = attrMap,
                            nivel = currentLevel,
                            namespacePrefix = prefix
                        )
                        stack.push(builder)
                    }

                    XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                        val text = parser.text
                        if (!text.isNullOrBlank() && stack.isNotEmpty()) {
                            stack.peek()?.textBuilder?.append(text)
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (stack.isNotEmpty()) {
                            val completed = stack.pop()
                            val trimmedText = completed.textBuilder.toString().trim().ifEmpty { null }
                            val node = XmlNode(
                                nombre = completed.nombre,
                                atributos = completed.atributos,
                                valorTexto = trimmedText,
                                hijos = completed.children.toList(),
                                nivel = completed.nivel,
                                namespacePrefix = completed.namespacePrefix
                            )

                            if (stack.isNotEmpty()) {
                                stack.peek()?.children?.add(node)
                            } else {
                                rootNode = node
                            }
                        }
                    }
                }
                eventType = parser.nextToken()
            }

            if (rootNode != null) {
                val totalNodes = rootNode.countTotalNodes()
                val detectedType = XmlTypeDetector.detect(rootNode)
                XmlParseResult.Success(
                    documentInfo = XmlDocumentInfo(
                        uriString = uriString,
                        fileName = fileName,
                        fileSizeFormatted = fileSizeFormatted,
                        rootNode = rootNode,
                        documentType = detectedType,
                        totalNodes = totalNodes,
                        encoding = parser.inputEncoding ?: "UTF-8"
                    )
                )
            } else {
                XmlParseResult.Error(
                    message = "El archivo XML está vacío o no contiene ningún nodo raíz válido.",
                    line = 0,
                    column = 0
                )
            }
        } catch (e: XmlPullParserException) {
            XmlParseResult.Error(
                message = e.localizedMessage ?: "Error de sintaxis XML",
                line = e.lineNumber,
                column = e.columnNumber,
                rawSnippet = "Línea ${e.lineNumber}, Columna ${e.columnNumber}"
            )
        } catch (e: Exception) {
            XmlParseResult.Error(
                message = e.localizedMessage ?: "Error de lectura del archivo",
                line = 0,
                column = 0
            )
        } finally {
            try {
                inputStream.close()
            } catch (_: Exception) {
            }
        }
    }
}
