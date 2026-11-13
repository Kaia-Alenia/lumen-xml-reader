# LUMEN - Lector XML y CFDI

Aplicación nativa para Android desarrollada en **Kotlin** con **Jetpack Compose** y **Material Design 3**, diseñada para abrir e inspeccionar cualquier archivo `.xml` en texto limpio estructurado como un árbol jerárquico interactivo, evitando visualizaciones crudas de código plano.

---

## Características Principales

- **Parser Universal de Alto Rendimiento:**
  - Basado en el `XmlPullParser` nativo de Android.
  - Streaming eficiente en memoria para archivos grandes.
  - Soporte completo para namespaces (`cfdi:`, `android:`, `tfd:`, `tools:`), atributos infinitos y valores de texto interno.
  - Detección precisa de errores sintácticos con indicador exacto de número de línea y columna.

- **Detección Automática de Tipo de Documento:**
  - Facturas CFDI 3.3 / 4.0 del SAT México.
  - `AndroidManifest.xml` y layouts de UI Android.
  - Archivos de recursos Android (`strings.xml`, `colors.xml`, etc.).
  - Proyectos Maven (`pom.xml`).
  - Gráficos vectoriales SVG.
  - Configuraciones de Spring Framework y XML genéricos.

- **Experiencia de Usuario (UI/UX) Moderna:**
  - Árbol plegable / desplegable con animaciones fluidas en `LazyColumn`.
  - Líneas guía de indentación vertical por nivel de profundidad.
  - Resaltado diferenciado para claves y valores de atributos mediante chips compactos.
  - Diálogo modal de detalles al seleccionar o mantener presionado cualquier nodo (con opción de copiar contenido, atributos y etiquetas).
  - Búsqueda en tiempo real por nombre de etiqueta, clave de atributo o valor interno.
  - Acciones rápidas para expandir o contraer la totalidad del documento.

- **Apertura Universal e Integración con el Sistema:**
  - Filtro de Intents configurado para `text/xml`, `application/xml` y extensiones `.xml` (disponible en "Abrir con" desde WhatsApp, Gmail, Telegram y administradores de archivos).
  - Selector de archivos interno mediante Storage Access Framework (SAF).
  - Historial de archivos abiertos recientemente con persistencia reactiva en Jetpack DataStore.

- **Compatibilidad de Diseño:**
  - Soporte completo para Tema Claro y Tema Oscuro con Material You.
  - Min SDK 26 (Android 8.0 Oreo), Target SDK 34 (Android 14).
  - Package ID: `com.lumen.xmlreader`.

---

## Integración Continua (CI/CD)

El repositorio incluye un workflow en GitHub Actions ([.github/workflows/android.yml](.github/workflows/android.yml)) que compila automáticamente el APK debug en cada push a la rama principal y lo publica como artefacto descargable denominado `LUMEN-debug-apk`.

---

## Licencia

Este proyecto está distribuido bajo la licencia **GNU General Public License v3 (GPL v3)**.

Desarrollado por **Alenia Studios**.  
Contacto: `contact.aleniastudios@gmail.com`
