# LUMEN — Lector XML y CFDI (Monorepo)

> *Read this documentation in [English](README.en.md).*

Monorepo multiplataforma de **LUMEN**, diseñado para abrir, inspeccionar y exportar archivos `.xml` y facturas **CFDI 3.3 / 4.0** con una interfaz limpia y sin código crudo.

---

## Estructura del Monorepo

```text
lumen-xml-reader/
├── apps/
│   ├── android/     # Aplicación nativa Android (Kotlin + Jetpack Compose + Material 3)
│   └── windows/     # Aplicación de escritorio Windows (Tauri v2 + React + Tailwind + Rust)
├── .github/workflows/
│   └── ci-release.yml
└── LICENSE
```

---

## Aplicaciones

### 1. LUMEN para Windows (`apps/windows`)
- **Interfaz limpia y moderna** con soporte para **Tema Claro y Tema Oscuro**.
- **Arrastrar y soltar** archivos `.xml` directamente desde el Explorador de Windows o abrir con doble clic (`Abrir con LUMEN`).
- **Vista Factura CFDI**: emisor, receptor, folio fiscal (UUID), conceptos, impuestos y exportación directa a **PDF** (`Exportar PDF` / `Ctrl+P`).
- **Árbol XML Interactivo**: líneas guía de indentación, badges de namespace, búsqueda en tiempo real e inspector de nodo con copiado rápido.
- **XML Crudo**: vista numerada con copiado completo.
- **Historial de Recientes**: acceso rápido a tus últimos documentos abiertos.

#### Desarrollo local (Windows / Linux)
```bash
cd apps/windows
npm install
npm test
npm run build
npm run tauri dev
```

---

### 2. LUMEN para Android (`apps/android`)
- Aplicación nativa en **Kotlin** y **Jetpack Compose** (Min SDK 26, Target SDK 34).
- Parser universal `XmlPullParser`, vista de árbol, vista factura CFDI con generación de PDF y soporte de Intents para abrir `.xml` desde cualquier app.

#### Compilación local
```bash
cd apps/android
./gradlew assembleDebug
```

---

## Integración Continua (CI/CD)

El workflow [.github/workflows/ci-release.yml](.github/workflows/ci-release.yml) compila automáticamente ambas plataformas en cada push a `main` o tag `v*` y publica los artefactos en GitHub Releases:
- **Android**: `LUMEN-app-debug.apk`
- **Windows**: `LUMEN-windows-setup.exe` (NSIS) y `LUMEN-windows.msi`

---

## Licencia

Distribuido bajo la licencia **GNU General Public License v3 (GPL v3)**.  
Desarrollado por **Alenia Studios** — `contact.aleniastudios@gmail.com`
