import { Code2, FolderOpen, GitFork, Printer, Receipt } from 'lucide-react';
import { XmlDocumentInfo } from '../domain/types';

export type ActiveViewTab = 'invoice' | 'tree' | 'raw';

interface TopBarProps {
  document: XmlDocumentInfo;
  activeTab: ActiveViewTab;
  onSelectTab: (tab: ActiveViewTab) => void;
  onOpenFile: () => void;
  onExportPdf: () => void;
}

export function TopBar({
  document,
  activeTab,
  onSelectTab,
  onOpenFile,
  onExportPdf,
}: TopBarProps) {
  const tabs: { id: ActiveViewTab; label: string; icon: typeof Receipt }[] = [
    { id: 'invoice', label: 'Factura', icon: Receipt },
    { id: 'tree', label: 'Árbol', icon: GitFork },
    { id: 'raw', label: 'XML Crudo', icon: Code2 },
  ];

  return (
    <header className="no-print flex flex-wrap items-center justify-between gap-3 px-5 py-3 border-b border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
      {/* File info */}
      <div className="min-w-0 flex items-center gap-3">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 truncate max-w-[300px]">
              {document.fileName}
            </h2>
            <span
              className="px-2 py-0.5 rounded-full text-[11px] font-semibold text-white shrink-0"
              style={{ backgroundColor: document.documentType.badgeColor }}
            >
              {document.documentType.label}
            </span>
          </div>
          <div className="text-[11px] text-slate-400 dark:text-slate-500 mt-0.5 flex items-center gap-2">
            <span>{document.fileSizeFormatted}</span>
            <span>•</span>
            <span>{document.totalNodes} nodos</span>
            <span>•</span>
            <span>{document.encoding}</span>
          </div>
        </div>
      </div>

      {/* View mode tabs */}
      <div className="flex items-center gap-1 p-1 rounded-xl bg-slate-100 dark:bg-slate-800 border border-slate-200/70 dark:border-slate-700/70">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => onSelectTab(tab.id)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                isActive
                  ? 'bg-white dark:bg-slate-900 text-teal-700 dark:text-teal-300 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              <Icon className="w-3.5 h-3.5" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Quick actions */}
      <div className="flex items-center gap-2">
        {activeTab === 'invoice' && (
          <button
            type="button"
            onClick={onExportPdf}
            title="Imprimir o Guardar como PDF (Ctrl+P)"
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-white text-xs font-semibold shadow-sm transition-colors"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Exportar PDF</span>
          </button>
        )}
        <button
          type="button"
          onClick={onOpenFile}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 text-xs font-medium transition-colors"
        >
          <FolderOpen className="w-3.5 h-3.5" />
          <span>Abrir otro</span>
        </button>
      </div>
    </header>
  );
}
