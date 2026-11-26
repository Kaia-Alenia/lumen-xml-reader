# LUMEN — XML & CFDI Reader (Monorepo)

> *Lee esta documentación en [Español](README.md).*

Cross-platform monorepo for **LUMEN**, built to open, inspect, and export `.xml` documents and **CFDI 3.3 / 4.0** invoices through a clean, readable interface.

---

## Monorepo Structure

```text
lumen-xml-reader/
├── apps/
│   ├── android/     # Native Android app (Kotlin + Jetpack Compose + Material 3)
│   └── windows/     # Windows desktop app (Tauri v2 + React + Tailwind + Rust)
├── .github/workflows/
│   └── ci-release.yml
└── LICENSE
```

---

## Applications

### 1. LUMEN for Windows (`apps/windows`)
- **Clean modern desktop UI** with **Light & Dark themes**.
- **Drag & drop** `.xml` files directly from Windows Explorer or associate `.xml` files to open with LUMEN.
- **CFDI Invoice View**: issuer, recipient, fiscal UUID, line items, taxes, and one-click **PDF export** (`Ctrl+P`).
- **Interactive XML Tree**: indentation guides, namespace badges, real-time search, and node inspector with quick copy.
- **Raw XML View**: line-numbered source viewer with one-click copy.
- **Recent Files**: quick sidebar access to recently opened documents.

#### Local development
```bash
cd apps/windows
npm install
npm test
npm run build
npm run tauri dev
```

---

### 2. LUMEN for Android (`apps/android`)
- Native **Kotlin** and **Jetpack Compose** app (Min SDK 26, Target SDK 34).
- Streaming `XmlPullParser`, interactive tree view, CFDI invoice view with PDF export, and system Intent support.

#### Local build
```bash
cd apps/android
./gradlew assembleDebug
```

---

## Continuous Integration (CI/CD)

The workflow [.github/workflows/ci-release.yml](.github/workflows/ci-release.yml) builds both platforms on pushes to `main` or `v*` tags and publishes them to GitHub Releases:
- **Android**: `LUMEN-app-debug.apk`
- **Windows**: `LUMEN-windows-setup.exe` (NSIS) and `LUMEN-windows.msi`

---

## License

Distributed under the **GNU General Public License v3 (GPL v3)**.  
Developed by **Alenia Studios** — `contact.aleniastudios@gmail.com`
