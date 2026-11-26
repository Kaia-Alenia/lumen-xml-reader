import { InvoiceConcept, InvoiceData, XmlNode } from './types';

function findAttrValue(
  attrs: Record<string, string>,
  ...candidates: string[]
): string | null {
  for (const candidate of candidates) {
    if (attrs[candidate] !== undefined) {
      return attrs[candidate];
    }
    const lowerCandidate = candidate.toLowerCase();
    for (const [key, value] of Object.entries(attrs)) {
      const cleanKey = key.includes(':') ? key.slice(key.indexOf(':') + 1) : key;
      if (cleanKey.toLowerCase() === lowerCandidate) {
        return value;
      }
    }
  }
  return null;
}

function findNodeByName(parent: XmlNode, ...names: string[]): XmlNode | null {
  const lowerNames = names.map((n) => n.toLowerCase());
  for (const child of parent.children) {
    if (lowerNames.includes(child.localName.toLowerCase())) {
      return child;
    }
  }
  for (const child of parent.children) {
    const found = findNodeByName(child, ...names);
    if (found) return found;
  }
  return null;
}

function findAllNodesByName(parent: XmlNode, ...names: string[]): XmlNode[] {
  const lowerNames = names.map((n) => n.toLowerCase());
  const result: XmlNode[] = [];

  function walk(node: XmlNode): void {
    if (lowerNames.includes(node.localName.toLowerCase())) {
      result.push(node);
    }
    for (const child of node.children) {
      walk(child);
    }
  }

  for (const child of parent.children) {
    walk(child);
  }
  return result;
}

function formatAmount(raw: string): string {
  const clean = raw.replace(/,/g, '').trim();
  const num = Number.parseFloat(clean);
  if (Number.isFinite(num)) {
    return num.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }
  return raw;
}

export function extractInvoiceData(rootNode: XmlNode): InvoiceData {
  const rootAttrs = rootNode.attributes;

  const folio = findAttrValue(rootAttrs, 'Folio', 'folio') ?? '—';
  const serie = findAttrValue(rootAttrs, 'Serie', 'serie') ?? '';
  const fecha = findAttrValue(rootAttrs, 'Fecha', 'fecha') ?? '';
  const moneda = findAttrValue(rootAttrs, 'Moneda', 'moneda') ?? 'MXN';
  const formaPago = findAttrValue(rootAttrs, 'FormaPago', 'formaPago') ?? '';
  const metodoPago = findAttrValue(rootAttrs, 'MetodoPago', 'metodoPago') ?? '';
  const subtotal = findAttrValue(rootAttrs, 'SubTotal', 'subTotal', 'subtotal') ?? '0.00';
  const total = findAttrValue(rootAttrs, 'Total', 'total') ?? '0.00';

  const timbreNode = findNodeByName(rootNode, 'TimbreFiscalDigital');
  const uuid = timbreNode
    ? (findAttrValue(timbreNode.attributes, 'UUID', 'uuid') ?? '')
    : '';

  const emisorNode = findNodeByName(rootNode, 'Emisor', 'emisor');
  const emisorAttrs = emisorNode?.attributes ?? {};
  const emisorNombre =
    findAttrValue(emisorAttrs, 'Nombre', 'nombre') ?? 'EMISOR NO ESPECIFICADO';
  const emisorRfc =
    findAttrValue(emisorAttrs, 'Rfc', 'rfc', 'RFC') ?? 'XAXX010101000';
  const emisorRegimen =
    findAttrValue(emisorAttrs, 'RegimenFiscal', 'regimenFiscal', 'Regimen') ?? '601';

  const receptorNode = findNodeByName(rootNode, 'Receptor', 'receptor');
  const receptorAttrs = receptorNode?.attributes ?? {};
  const receptorNombre =
    findAttrValue(receptorAttrs, 'Nombre', 'nombre') ?? 'RECEPTOR NO ESPECIFICADO';
  const receptorRfc =
    findAttrValue(receptorAttrs, 'Rfc', 'rfc', 'RFC') ?? 'XAXX010101000';
  const receptorDomicilio =
    findAttrValue(
      receptorAttrs,
      'DomicilioFiscalReceptor',
      'domicilioFiscalReceptor',
      'Domicilio',
      'domicilio',
      'CodigoPostal',
      'CP'
    ) ?? '01000';
  const receptorUsoCfdi =
    findAttrValue(receptorAttrs, 'UsoCFDI', 'usoCFDI', 'usocfdi') ?? 'G03';

  const conceptosContainer = findNodeByName(rootNode, 'Conceptos', 'conceptos');
  const conceptoNodes = conceptosContainer
    ? conceptosContainer.children.filter(
        (child) => child.localName.toLowerCase() === 'concepto'
      )
    : findAllNodesByName(rootNode, 'Concepto', 'concepto');

  const conceptos: InvoiceConcept[] = conceptoNodes.map((node) => {
    const attrs = node.attributes;
    const clave =
      findAttrValue(attrs, 'ClaveProdServ', 'claveProdServ', 'Clave', 'clave') ??
      '01010101';
    const cantidad = findAttrValue(attrs, 'Cantidad', 'cantidad') ?? '1';
    const unidad =
      findAttrValue(attrs, 'ClaveUnidad', 'claveUnidad', 'Unidad', 'unidad') ?? 'PZA';
    const descripcion =
      findAttrValue(attrs, 'Descripcion', 'descripcion', 'concepto') ??
      node.textValue ??
      'Concepto';
    const valorUnitario =
      findAttrValue(attrs, 'ValorUnitario', 'valorUnitario', 'precio') ?? '0.00';
    const importe = findAttrValue(attrs, 'Importe', 'importe') ?? valorUnitario;

    return {
      clave,
      cantidad,
      unidad,
      descripcion,
      valorUnitario: formatAmount(valorUnitario),
      importe: formatAmount(importe),
    };
  });

  let ivaAmount = '0.00';
  const impuestosNode = findNodeByName(rootNode, 'Impuestos', 'impuestos');
  if (impuestosNode) {
    const totalTraslados = findAttrValue(
      impuestosNode.attributes,
      'TotalImpuestosTrasladados',
      'totalImpuestosTrasladados'
    );
    if (totalTraslados && totalTraslados.trim().length > 0) {
      ivaAmount = totalTraslados;
    } else {
      const traslados = findAllNodesByName(impuestosNode, 'Traslado', 'traslado');
      let sum = 0;
      for (const traslado of traslados) {
        const imp = Number.parseFloat(
          findAttrValue(traslado.attributes, 'Importe', 'importe') ?? '0'
        );
        if (Number.isFinite(imp)) {
          sum += imp;
        }
      }
      if (sum > 0) {
        ivaAmount = sum.toFixed(2);
      }
    }
  }

  if (ivaAmount === '0.00') {
    const subVal = Number.parseFloat(subtotal.replace(/,/g, '')) || 0;
    const totVal = Number.parseFloat(total.replace(/,/g, '')) || 0;
    if (totVal > subVal && subVal > 0) {
      ivaAmount = (totVal - subVal).toFixed(2);
    }
  }

  const isCfdi =
    emisorNode !== null || receptorNode !== null || conceptos.length > 0;

  return {
    folio,
    serie,
    fecha,
    moneda,
    formaPago,
    metodoPago,
    uuid,
    emisorNombre,
    emisorRfc,
    emisorRegimen,
    receptorNombre,
    receptorRfc,
    receptorDomicilio,
    receptorUsoCfdi,
    conceptos,
    subtotal: formatAmount(subtotal),
    iva: formatAmount(ivaAmount),
    total: formatAmount(total),
    isCfdi,
  };
}
