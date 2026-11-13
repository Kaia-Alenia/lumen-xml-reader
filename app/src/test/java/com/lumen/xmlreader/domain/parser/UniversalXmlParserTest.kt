package com.lumen.xmlreader.domain.parser

import com.lumen.xmlreader.domain.model.XmlDocumentType
import com.lumen.xmlreader.domain.model.XmlParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class UniversalXmlParserTest {

    @Test
    fun testParseGenericXmlHierarchy() {
        val xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <root app="test">
                <parent id="p1">
                    <child value="100">Contenido Texto</child>
                </parent>
            </root>
        """.trimIndent()

        val inputStream = ByteArrayInputStream(xmlContent.toByteArray(Charsets.UTF_8))
        val result = UniversalXmlParser.parse(
            inputStream = inputStream,
            fileName = "sample.xml",
            fileSizeFormatted = "1.0 KB",
            uriString = "content://test/sample.xml"
        )

        assertTrue(result is XmlParseResult.Success)
        val success = result as XmlParseResult.Success
        assertEquals("root", success.documentInfo.rootNode.nombre)
        assertEquals("test", success.documentInfo.rootNode.atributos["app"])
        assertEquals(1, success.documentInfo.rootNode.hijos.size)

        val parent = success.documentInfo.rootNode.hijos[0]
        assertEquals("parent", parent.nombre)
        assertEquals("p1", parent.atributos["id"])

        val child = parent.hijos[0]
        assertEquals("child", child.nombre)
        assertEquals("100", child.atributos["value"])
        assertEquals("Contenido Texto", child.valorTexto)
    }

    @Test
    fun testParseCfdiSatXmlDetection() {
        val cfdiXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <cfdi:Comprobante xmlns:cfdi="http://www.sat.gob.mx/cfd/4" Version="4.0" Total="1500.00" Moneda="MXN">
                <cfdi:Emisor Rfc="XAXX010101000" Nombre="EMPRESA PRUEBA SA DE CV" RegimenFiscal="601" />
                <cfdi:Receptor Rfc="XEXX010101000" Nombre="PUBLICO EN GENERAL" UsoCFDI="G03" />
                <cfdi:Conceptos>
                    <cfdi:Concepto ClaveProdServ="01010101" Cantidad="1" Descripcion="Servicio" ValorUnitario="1500.00" Importe="1500.00" />
                </cfdi:Conceptos>
            </cfdi:Comprobante>
        """.trimIndent()

        val inputStream = ByteArrayInputStream(cfdiXml.toByteArray(Charsets.UTF_8))
        val result = UniversalXmlParser.parse(
            inputStream = inputStream,
            fileName = "factura_sat.xml",
            fileSizeFormatted = "2.5 KB",
            uriString = "content://test/factura_sat.xml"
        )

        assertTrue(result is XmlParseResult.Success)
        val success = result as XmlParseResult.Success
        assertEquals(XmlDocumentType.CFDI_SAT, success.documentInfo.documentType)
        assertEquals("cfdi:Comprobante", success.documentInfo.rootNode.nombre)
        assertEquals("4.0", success.documentInfo.rootNode.atributos["Version"])
        assertEquals(3, success.documentInfo.rootNode.hijos.size)
    }

    @Test
    fun testParseMalformedXmlReturnsErrorWithLine() {
        val malformedXml = """
            <root>
                <unclosed_tag>
            </root>
        """.trimIndent()

        val inputStream = ByteArrayInputStream(malformedXml.toByteArray(Charsets.UTF_8))
        val result = UniversalXmlParser.parse(
            inputStream = inputStream,
            fileName = "broken.xml",
            fileSizeFormatted = "500 B",
            uriString = "content://test/broken.xml"
        )

        assertTrue(result is XmlParseResult.Error)
        val error = result as XmlParseResult.Error
        assertTrue(error.line > 0)
        assertNotNull(error.message)
    }
}
