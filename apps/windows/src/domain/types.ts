export type XmlDocumentTypeId =
  | 'CFDI_SAT'
  | 'ANDROID_MANIFEST'
  | 'ANDROID_LAYOUT'
  | 'ANDROID_RESOURCES'
  | 'MAVEN_POM'
  | 'SVG_GRAPHIC'
  | 'SPRING_CONFIG'
  | 'GENERIC_XML';

export interface XmlDocumentTypeMeta {
  id: XmlDocumentTypeId;
  label: string;
  badgeColor: string;
}

export const XML_DOCUMENT_TYPES: Record<XmlDocumentTypeId, XmlDocumentTypeMeta> = {
  CFDI_SAT: {
    id: 'CFDI_SAT',
    label: 'CFDI Factura SAT',
    badgeColor: '#00897B',
  },
  ANDROID_MANIFEST: {
    id: 'ANDROID_MANIFEST',
    label: 'Android Manifest',
    badgeColor: '#3DDC84',
  },
  ANDROID_LAYOUT: {
    id: 'ANDROID_LAYOUT',
    label: 'Android Layout UI',
    badgeColor: '#1E88E5',
  },
  ANDROID_RESOURCES: {
    id: 'ANDROID_RESOURCES',
    label: 'Android Resources',
    badgeColor: '#5E35B1',
  },
  MAVEN_POM: {
    id: 'MAVEN_POM',
    label: 'Maven POM',
    badgeColor: '#E53935',
  },
  SVG_GRAPHIC: {
    id: 'SVG_GRAPHIC',
    label: 'Vector SVG',
    badgeColor: '#FB8C00',
  },
  SPRING_CONFIG: {
    id: 'SPRING_CONFIG',
    label: 'Spring Config',
    badgeColor: '#43A047',
  },
  GENERIC_XML: {
    id: 'GENERIC_XML',
    label: 'XML Genérico',
    badgeColor: '#64748B',
  },
};

export interface XmlNode {
  id: string;
  name: string;
  localName: string;
  namespacePrefix: string | null;
  attributes: Record<string, string>;
  textValue: string | null;
  children: XmlNode[];
  level: number;
}

export interface XmlDocumentInfo {
  path: string;
  fileName: string;
  fileSizeFormatted: string;
  rootNode: XmlNode;
  documentType: XmlDocumentTypeMeta;
  totalNodes: number;
  encoding: string;
  rawXml: string;
}

export type XmlParseResult =
  | {
      ok: true;
      document: XmlDocumentInfo;
    }
  | {
      ok: false;
      message: string;
      line: number;
      column: number;
    };

export interface InvoiceConcept {
  clave: string;
  cantidad: string;
  unidad: string;
  descripcion: string;
  valorUnitario: string;
  importe: string;
}

export interface InvoiceData {
  folio: string;
  serie: string;
  fecha: string;
  moneda: string;
  formaPago: string;
  metodoPago: string;
  uuid: string;
  emisorNombre: string;
  emisorRfc: string;
  emisorRegimen: string;
  receptorNombre: string;
  receptorRfc: string;
  receptorDomicilio: string;
  receptorUsoCfdi: string;
  conceptos: InvoiceConcept[];
  subtotal: string;
  iva: string;
  total: string;
  isCfdi: boolean;
}

export interface RecentFileEntry {
  path: string;
  fileName: string;
  documentType: XmlDocumentTypeId;
  openedAt: number;
  sizeFormatted: string;
}
