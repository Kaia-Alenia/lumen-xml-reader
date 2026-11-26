import { describe, expect, it } from 'vitest';
import { formatFileSize, parseXmlContent } from '../src/domain/xmlParser';

describe('formatFileSize', () => {
  it('formats bytes, kilobytes, and megabytes cleanly', () => {
    expect(formatFileSize(0)).toBe('0 B');
    expect(formatFileSize(512)).toBe('512 B');
    expect(formatFileSize(2048)).toBe('2.0 KB');
    expect(formatFileSize(2 * 1024 * 1024)).toBe('2.00 MB');
  });
});

describe('parseXmlContent', () => {
  it('parses valid XML with namespaces, attributes, and inner text', () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
      <cfdi:Comprobante xmlns:cfdi="http://www.sat.gob.mx/cfd/4" Folio="42" Total="116.00">
        <cfdi:Emisor Nombre="Alenia Studios" Rfc="ALE200101AAA" />
        <cfdi:Conceptos>
          <cfdi:Concepto Descripcion="Licencia Software">Detalle</cfdi:Concepto>
        </cfdi:Conceptos>
      </cfdi:Comprobante>`;

    const result = parseXmlContent(xml, 'factura.xml', 1024, 'C:\\factura.xml');
    expect(result.ok).toBe(true);
    if (!result.ok) return;

    expect(result.document.fileName).toBe('factura.xml');
    expect(result.document.fileSizeFormatted).toBe('1.0 KB');
    expect(result.document.documentType.id).toBe('CFDI_SAT');
    expect(result.document.rootNode.name).toBe('cfdi:Comprobante');
    expect(result.document.rootNode.namespacePrefix).toBe('cfdi');
    expect(result.document.rootNode.attributes['Folio']).toBe('42');
    expect(result.document.totalNodes).toBe(4);
    expect(result.document.rootNode.children[1].children[0].textValue).toBe('Detalle');
  });

  it('returns error result for empty or malformed XML', () => {
    const emptyRes = parseXmlContent('   ', 'empty.xml', 0, 'empty.xml');
    expect(emptyRes.ok).toBe(false);

    const badRes = parseXmlContent('<root><unclosed></root>', 'bad.xml', 22, 'bad.xml');
    expect(badRes.ok).toBe(false);
  });
});
