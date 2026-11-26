import { describe, expect, it } from 'vitest';
import { parseXmlContent } from '../src/domain/xmlParser';

describe('detectXmlDocumentType', () => {
  it('detects CFDI SAT documents', () => {
    const res = parseXmlContent(
      '<cfdi:Comprobante xmlns:cfdi="http://www.sat.gob.mx/cfd/4" />',
      'cfdi.xml',
      100,
      'cfdi.xml'
    );
    expect(res.ok).toBe(true);
    if (res.ok) expect(res.document.documentType.id).toBe('CFDI_SAT');
  });

  it('detects Android Manifest, Layout, and Resources', () => {
    const manifest = parseXmlContent('<manifest package="com.test" />', 'AndroidManifest.xml', 50, 'm.xml');
    const layout = parseXmlContent(
      '<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android" />',
      'activity_main.xml',
      80,
      'l.xml'
    );
    const resources = parseXmlContent('<resources><string name="app">Lumen</string></resources>', 'strings.xml', 60, 'r.xml');

    expect(manifest.ok && manifest.document.documentType.id).toBe('ANDROID_MANIFEST');
    expect(layout.ok && layout.document.documentType.id).toBe('ANDROID_LAYOUT');
    expect(resources.ok && resources.document.documentType.id).toBe('ANDROID_RESOURCES');
  });

  it('detects Maven POM, SVG, Spring Config, and Generic XML', () => {
    const pom = parseXmlContent('<project xmlns="http://maven.apache.org/POM/4.0.0" />', 'pom.xml', 50, 'pom.xml');
    const svg = parseXmlContent('<svg xmlns="http://www.w3.org/2000/svg" />', 'icon.svg', 50, 'icon.svg');
    const spring = parseXmlContent('<beans xmlns="http://www.springframework.org/schema/beans" />', 'app.xml', 50, 'app.xml');
    const generic = parseXmlContent('<catalog><book id="1" /></catalog>', 'catalog.xml', 50, 'catalog.xml');

    expect(pom.ok && pom.document.documentType.id).toBe('MAVEN_POM');
    expect(svg.ok && svg.document.documentType.id).toBe('SVG_GRAPHIC');
    expect(spring.ok && spring.document.documentType.id).toBe('SPRING_CONFIG');
    expect(generic.ok && generic.document.documentType.id).toBe('GENERIC_XML');
  });
});
