import { XML_DOCUMENT_TYPES, XmlDocumentTypeMeta, XmlNode } from './types';

const ANDROID_LAYOUT_ROOTS = new Set([
  'linearlayout',
  'relativelayout',
  'framelayout',
  'constraintlayout',
  'coordinatorlayout',
  'scrollview',
  'nestedscrollview',
  'motionlayout',
  'viewgroup',
  'merge',
]);

export function detectXmlDocumentType(rootNode: XmlNode): XmlDocumentTypeMeta {
  const rootName = rootNode.name.toLowerCase();
  const rootLocal = rootNode.localName.toLowerCase();
  const attrValues = Object.values(rootNode.attributes);
  const attrKeys = Object.keys(rootNode.attributes);

  if (
    rootName.includes('comprobante') ||
    attrValues.some((value) => value.toLowerCase().includes('sat.gob.mx')) ||
    rootNode.children.some((child) => {
      const lower = child.name.toLowerCase();
      return lower.startsWith('cfdi:') || lower.startsWith('tfd:');
    })
  ) {
    return XML_DOCUMENT_TYPES.CFDI_SAT;
  }

  if (rootName === 'manifest' || rootLocal === 'manifest') {
    return XML_DOCUMENT_TYPES.ANDROID_MANIFEST;
  }

  if (rootName === 'resources' || rootLocal === 'resources') {
    return XML_DOCUMENT_TYPES.ANDROID_RESOURCES;
  }

  if (
    ANDROID_LAYOUT_ROOTS.has(rootLocal) ||
    attrKeys.some((key) => key.startsWith('android:') || key.startsWith('app:'))
  ) {
    if (rootName !== 'manifest' && rootName !== 'resources') {
      return XML_DOCUMENT_TYPES.ANDROID_LAYOUT;
    }
  }

  if (rootName === 'svg' || rootLocal === 'svg') {
    return XML_DOCUMENT_TYPES.SVG_GRAPHIC;
  }

  if (
    rootName === 'project' &&
    ('modelVersion' in rootNode.attributes ||
      attrValues.some((value) => value.toLowerCase().includes('maven.apache.org')))
  ) {
    return XML_DOCUMENT_TYPES.MAVEN_POM;
  }

  if (
    rootName === 'beans' ||
    attrValues.some((value) => value.toLowerCase().includes('springframework'))
  ) {
    return XML_DOCUMENT_TYPES.SPRING_CONFIG;
  }

  return XML_DOCUMENT_TYPES.GENERIC_XML;
}
