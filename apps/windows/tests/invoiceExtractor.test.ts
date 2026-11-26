import { describe, expect, it } from 'vitest';
import { extractInvoiceData } from '../src/domain/invoiceExtractor';
import { parseXmlContent } from '../src/domain/xmlParser';

describe('extractInvoiceData', () => {
  it('extracts complete CFDI 4.0 fields including UUID and concepts', () => {
    const cfdiXml = `<?xml version="1.0" encoding="UTF-8"?>
      <cfdi:Comprobante
        xmlns:cfdi="http://www.sat.gob.mx/cfd/4"
        xmlns:tfd="http://www.sat.gob.mx/TimbreFiscalDigital"
        Serie="A"
        Folio="1024"
        Fecha="2026-11-22T12:30:00"
        FormaPago="03"
        MetodoPago="PUE"
        Moneda="MXN"
        SubTotal="1000.00"
        Total="1160.00">
        <cfdi:Emisor Rfc="ALE200101AAA" Nombre="ALENIA STUDIOS SA DE CV" RegimenFiscal="601" />
        <cfdi:Receptor Rfc="CLI990101BBB" Nombre="CLIENTE EJEMPLO" DomicilioFiscalReceptor="06600" UsoCFDI="G03" />
        <cfdi:Conceptos>
          <cfdi:Concepto ClaveProdServ="81112200" Cantidad="2" ClaveUnidad="E48" Descripcion="Desarrollo de software" ValorUnitario="500.00" Importe="1000.00" />
        </cfdi:Conceptos>
        <cfdi:Impuestos TotalImpuestosTrasladados="160.00" />
        <cfdi:Complemento>
          <tfd:TimbreFiscalDigital UUID="12345678-ABCD-1234-ABCD-1234567890AB" />
        </cfdi:Complemento>
      </cfdi:Comprobante>`;

    const parsed = parseXmlContent(cfdiXml, 'cfdi.xml', 1500, 'cfdi.xml');
    expect(parsed.ok).toBe(true);
    if (!parsed.ok) return;

    const invoice = extractInvoiceData(parsed.document.rootNode);
    expect(invoice.isCfdi).toBe(true);
    expect(invoice.serie).toBe('A');
    expect(invoice.folio).toBe('1024');
    expect(invoice.uuid).toBe('12345678-ABCD-1234-ABCD-1234567890AB');
    expect(invoice.emisorNombre).toBe('ALENIA STUDIOS SA DE CV');
    expect(invoice.emisorRfc).toBe('ALE200101AAA');
    expect(invoice.receptorNombre).toBe('CLIENTE EJEMPLO');
    expect(invoice.receptorDomicilio).toBe('06600');
    expect(invoice.conceptos).toHaveLength(1);
    expect(invoice.conceptos[0].descripcion).toBe('Desarrollo de software');
    expect(invoice.subtotal).toBe('1,000.00');
    expect(invoice.iva).toBe('160.00');
    expect(invoice.total).toBe('1,160.00');
  });

  it('marks non-CFDI XML as isCfdi=false without throwing', () => {
    const parsed = parseXmlContent('<note><to>Ana</to></note>', 'note.xml', 40, 'note.xml');
    expect(parsed.ok).toBe(true);
    if (!parsed.ok) return;

    const invoice = extractInvoiceData(parsed.document.rootNode);
    expect(invoice.isCfdi).toBe(false);
    expect(invoice.conceptos).toHaveLength(0);
  });
});
