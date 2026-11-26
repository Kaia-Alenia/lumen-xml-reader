import { useCallback, useEffect, useMemo, useState } from 'react';
import { ErrorView } from './components/ErrorView';
import { InvoiceView } from './components/InvoiceView';
import { RawXmlView } from './components/RawXmlView';
import { Sidebar } from './components/Sidebar';
import { ActiveViewTab, TopBar } from './components/TopBar';
import { TreeView } from './components/TreeView';
import { UpdateModal } from './components/UpdateModal';
import { WelcomeDropZone } from './components/WelcomeDropZone';
import { extractInvoiceData } from './domain/invoiceExtractor';
import {
  clearRecentFiles,
  loadRecentFiles,
  removeRecentFile,
  saveRecentFile,
} from './domain/recentFiles';
import { RecentFileEntry, XmlDocumentInfo } from './domain/types';
import { AppUpdateInfo } from './domain/updateChecker';
import { parseXmlContent } from './domain/xmlParser';
import {
  getLaunchXmlPath,
  LoadedXmlPayload,
  openXmlFileDialog,
  readXmlFromPath,
  subscribeToTauriDrop,
} from './services/fileService';
import { checkForUpdates } from './services/updateService';

interface ErrorState {
  message: string;
  line: number;
  column: number;
}

export function App() {
  const [isDark, setIsDark] = useState<boolean>(() => {
    const saved = window.localStorage.getItem('lumen.theme');
    if (saved === 'dark') return true;
    if (saved === 'light') return false;
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
  });

  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [recentFiles, setRecentFiles] = useState<RecentFileEntry[]>(() => loadRecentFiles());
  const [activeDoc, setActiveDoc] = useState<XmlDocumentInfo | null>(null);
  const [errorState, setErrorState] = useState<ErrorState | null>(null);
  const [activeTab, setActiveTab] = useState<ActiveViewTab>('invoice');
  const [isDragging, setIsDragging] = useState(false);
  const [availableUpdate, setAvailableUpdate] = useState<AppUpdateInfo | null>(null);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [checkingUpdate, setCheckingUpdate] = useState(false);
  const [upToDateFeedback, setUpToDateFeedback] = useState(false);

  useEffect(() => {
    document.documentElement.classList.toggle('dark', isDark);
    window.localStorage.setItem('lumen.theme', isDark ? 'dark' : 'light');
  }, [isDark]);

  const loadPayload = useCallback((payload: LoadedXmlPayload) => {
    const parsed = parseXmlContent(
      payload.content,
      payload.fileName,
      payload.sizeBytes,
      payload.path
    );

    if (!parsed.ok) {
      setActiveDoc(null);
      setErrorState({
        message: parsed.message,
        line: parsed.line,
        column: parsed.column,
      });
      return;
    }

    setErrorState(null);
    setActiveDoc(parsed.document);
    setActiveTab(parsed.document.documentType.id === 'CFDI_SAT' ? 'invoice' : 'tree');

    const updatedRecent = saveRecentFile({
      path: parsed.document.path,
      fileName: parsed.document.fileName,
      documentType: parsed.document.documentType.id,
      openedAt: Date.now(),
      sizeFormatted: parsed.document.fileSizeFormatted,
    });
    setRecentFiles(updatedRecent);
  }, []);

  const handleOpenFile = useCallback(async () => {
    try {
      const payload = await openXmlFileDialog();
      if (payload) {
        loadPayload(payload);
      }
    } catch (err) {
      setActiveDoc(null);
      setErrorState({
        message: err instanceof Error ? err.message : 'Error al abrir el archivo.',
        line: 0,
        column: 0,
      });
    }
  }, [loadPayload]);

  const handleOpenPath = useCallback(
    async (path: string) => {
      try {
        const payload = await readXmlFromPath(path);
        loadPayload(payload);
      } catch (err) {
        setActiveDoc(null);
        setErrorState({
          message: err instanceof Error ? err.message : 'No se pudo leer el archivo.',
          line: 0,
          column: 0,
        });
      }
    },
    [loadPayload]
  );

  // Open file passed via Windows "Open with" / CLI argument
  useEffect(() => {
    getLaunchXmlPath().then((launchPath) => {
      if (launchPath) {
        handleOpenPath(launchPath);
      }
    });
  }, [handleOpenPath]);

  const runUpdateCheck = useCallback(async (manual: boolean) => {
    setCheckingUpdate(true);
    setUpToDateFeedback(false);
    const result = await checkForUpdates();
    setCheckingUpdate(false);

    if (result?.isUpdateAvailable) {
      setAvailableUpdate(result);
      setShowUpdateModal(true);
    } else if (manual) {
      setUpToDateFeedback(true);
      setTimeout(() => setUpToDateFeedback(false), 3000);
    }
  }, []);

  // Silent background check for GitHub updates after startup
  useEffect(() => {
    const timer = window.setTimeout(() => {
      runUpdateCheck(false);
    }, 2000);
    return () => window.clearTimeout(timer);
  }, [runUpdateCheck]);

  // Native Tauri drag-and-drop listener
  useEffect(() => {
    let unlisten: (() => void) | undefined;
    subscribeToTauriDrop(
      (paths) => {
        const xmlPath = paths.find((p) => p.toLowerCase().endsWith('.xml')) ?? paths[0];
        if (xmlPath) {
          handleOpenPath(xmlPath);
        }
      },
      (hovering) => setIsDragging(hovering)
    ).then((fn) => {
      unlisten = fn;
    });

    return () => {
      unlisten?.();
    };
  }, [handleOpenPath]);

  // Keyboard shortcuts (Ctrl+O to open, Ctrl+P to export PDF)
  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'o') {
        e.preventDefault();
        handleOpenFile();
      } else if (
        (e.ctrlKey || e.metaKey) &&
        e.key.toLowerCase() === 'p' &&
        activeDoc &&
        activeTab === 'invoice'
      ) {
        e.preventDefault();
        window.print();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [handleOpenFile, activeDoc, activeTab]);

  // Browser HTML5 drag-and-drop fallback
  const handleHtmlDrop = async (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files?.[0];
    if (!file) return;
    const content = await file.text();
    loadPayload({
      path: file.name,
      fileName: file.name,
      sizeBytes: file.size,
      content,
    });
  };

  const invoiceData = useMemo(() => {
    if (!activeDoc) return null;
    return extractInvoiceData(activeDoc.rootNode);
  }, [activeDoc]);

  return (
    <div
      onDragOver={(e) => {
        e.preventDefault();
        setIsDragging(true);
      }}
      onDragLeave={(e) => {
        if (e.currentTarget === e.target) setIsDragging(false);
      }}
      onDrop={handleHtmlDrop}
      className="h-screen w-screen flex overflow-hidden bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100"
    >
      <Sidebar
        collapsed={sidebarCollapsed}
        onToggleCollapse={() => setSidebarCollapsed((c) => !c)}
        recentFiles={recentFiles}
        activePath={activeDoc?.path ?? null}
        isDark={isDark}
        onToggleTheme={() => setIsDark((d) => !d)}
        onOpenFile={handleOpenFile}
        onSelectRecent={(entry) => handleOpenPath(entry.path)}
        onRemoveRecent={(entry) =>
          setRecentFiles(removeRecentFile(entry.path, entry.fileName))
        }
        onClearRecent={() => setRecentFiles(clearRecentFiles())}
        availableUpdate={availableUpdate}
        checkingUpdate={checkingUpdate}
        upToDateFeedback={upToDateFeedback}
        onCheckUpdate={() => runUpdateCheck(true)}
        onOpenUpdateModal={() => setShowUpdateModal(true)}
      />

      <main className="flex-1 flex flex-col min-w-0 min-h-0 relative">
        {activeDoc ? (
          <>
            <TopBar
              document={activeDoc}
              activeTab={activeTab}
              onSelectTab={setActiveTab}
              onOpenFile={handleOpenFile}
              onExportPdf={() => window.print()}
            />

            {activeTab === 'invoice' && invoiceData && (
              <InvoiceView invoice={invoiceData} />
            )}
            {activeTab === 'tree' && <TreeView rootNode={activeDoc.rootNode} />}
            {activeTab === 'raw' && <RawXmlView rawXml={activeDoc.rawXml} />}
          </>
        ) : errorState ? (
          <ErrorView
            message={errorState.message}
            line={errorState.line}
            column={errorState.column}
            onOpenFile={handleOpenFile}
          />
        ) : (
          <WelcomeDropZone onOpenFile={handleOpenFile} isDragging={isDragging} />
        )}
      </main>

      {showUpdateModal && availableUpdate && (
        <UpdateModal
          update={availableUpdate}
          onClose={() => setShowUpdateModal(false)}
        />
      )}
    </div>
  );
}

export default App;
