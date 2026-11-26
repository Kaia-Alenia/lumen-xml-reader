import { XmlNode, XmlParseResult } from './types';
import { detectXmlDocumentType } from './xmlTypeDetector';

export function formatFileSize(bytes: number): string {
  if (!Number.isFinite(bytes) || bytes <= 0) return '0 B';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

function extractParserErrorDetails(errorNode: Element): {
  message: string;
  line: number;
  column: number;
} {
  const rawMessage = (errorNode.textContent || 'XML syntax error').trim();
  const lineMatch = rawMessage.match(/line\s+(\d+)/i) || rawMessage.match(/l[ií]nea\s+(\d+)/i);
  const colMatch = rawMessage.match(/column\s+(\d+)/i) || rawMessage.match(/columna\s+(\d+)/i);

  return {
    message: rawMessage.split('\n')[0].trim() || 'XML syntax error',
    line: lineMatch ? Number.parseInt(lineMatch[1], 10) : 0,
    column: colMatch ? Number.parseInt(colMatch[1], 10) : 0,
  };
}

function extractEncoding(rawXml: string): string {
  const match = rawXml.match(/<\?xml[^>]*encoding=["']([^"']+)["']/i);
  return match ? match[1].toUpperCase() : 'UTF-8';
}

export function parseXmlContent(
  rawXml: string,
  fileName: string,
  fileSizeBytes: number,
  path: string
): XmlParseResult {
  const trimmed = rawXml.replace(/^\uFEFF/, '').trim();
  if (!trimmed) {
    return {
      ok: false,
      message: 'El archivo XML está vacío o no contiene ningún nodo raíz válido.',
      line: 0,
      column: 0,
    };
  }

  const parser = new DOMParser();
  const doc = parser.parseFromString(trimmed, 'application/xml');
  const parserError = doc.querySelector('parsererror');

  if (parserError) {
    const details = extractParserErrorDetails(parserError);
    return {
      ok: false,
      message: details.message,
      line: details.line,
      column: details.column,
    };
  }

  const rootElement = doc.documentElement;
  if (!rootElement) {
    return {
      ok: false,
      message: 'No se encontró un elemento raíz en el documento XML.',
      line: 0,
      column: 0,
    };
  }

  let nodeCounter = 0;

  function buildNode(element: Element, level: number): XmlNode {
    const id = `node-${++nodeCounter}`;
    const attributes: Record<string, string> = {};

    for (let i = 0; i < element.attributes.length; i += 1) {
      const attr = element.attributes[i];
      attributes[attr.name] = attr.value;
    }

    const children: XmlNode[] = [];
    const directTextParts: string[] = [];

    for (let i = 0; i < element.childNodes.length; i += 1) {
      const child = element.childNodes[i];
      if (child.nodeType === 1) {
        children.push(buildNode(child as Element, level + 1));
      } else if (child.nodeType === 3 || child.nodeType === 4) {
        const text = (child.nodeValue || '').trim();
        if (text) {
          directTextParts.push(text);
        }
      }
    }

    const fullName = element.tagName || element.nodeName;
    const colonIndex = fullName.indexOf(':');
    const prefix = element.prefix || (colonIndex > 0 ? fullName.slice(0, colonIndex) : null);
    const localName = element.localName || (colonIndex > 0 ? fullName.slice(colonIndex + 1) : fullName);

    return {
      id,
      name: fullName,
      localName,
      namespacePrefix: prefix,
      attributes,
      textValue: directTextParts.length > 0 ? directTextParts.join(' ') : null,
      children,
      level,
    };
  }

  const rootNode = buildNode(rootElement, 0);
  const documentType = detectXmlDocumentType(rootNode);

  return {
    ok: true,
    document: {
      path,
      fileName,
      fileSizeFormatted: formatFileSize(fileSizeBytes),
      rootNode,
      documentType,
      totalNodes: nodeCounter,
      encoding: extractEncoding(trimmed),
      rawXml: trimmed,
    },
  };
}
