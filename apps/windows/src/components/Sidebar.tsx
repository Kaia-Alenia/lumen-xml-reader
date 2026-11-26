import {
  Clock,
  Download,
  FileCode2,
  FolderOpen,
  Moon,
  PanelLeftClose,
  PanelLeftOpen,
  RefreshCw,
  Sun,
  Trash2,
  X,
} from 'lucide-react';
import { RecentFileEntry, XML_DOCUMENT_TYPES } from '../domain/types';
import { AppUpdateInfo } from '../domain/updateChecker';

interface SidebarProps {
  collapsed: boolean;
  onToggleCollapse: () => void;
  recentFiles: RecentFileEntry[];
  activePath: string | null;
  isDark: boolean;
  onToggleTheme: () => void;
  onOpenFile: () => void;
  onSelectRecent: (entry: RecentFileEntry) => void;
  onRemoveRecent: (entry: RecentFileEntry) => void;
  onClearRecent: () => void;
  availableUpdate: AppUpdateInfo | null;
  checkingUpdate: boolean;
  upToDateFeedback: boolean;
  onCheckUpdate: () => void;
  onOpenUpdateModal: () => void;
}

export function Sidebar({
  collapsed,
  onToggleCollapse,
  recentFiles,
  activePath,
  isDark,
  onToggleTheme,
  onOpenFile,
  onSelectRecent,
  onRemoveRecent,
  onClearRecent,
  availableUpdate,
  checkingUpdate,
  upToDateFeedback,
  onCheckUpdate,
  onOpenUpdateModal,
}: SidebarProps) {
  if (collapsed) {
    return (
      <aside className="no-print flex flex-col items-center justify-between w-14 shrink-0 border-r border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900/90 py-3">
        <div className="flex flex-col items-center gap-3">
          <button
            type="button"
            onClick={onToggleCollapse}
            title="Expandir barra lateral"
            className="p-2 rounded-lg text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <PanelLeftOpen className="w-5 h-5" />
          </button>
          <button
            type="button"
            onClick={onOpenFile}
            title="Abrir XML (Ctrl+O)"
            className="p-2.5 rounded-xl bg-teal-600 hover:bg-teal-700 text-white shadow-sm transition-colors"
          >
            <FolderOpen className="w-4 h-4" />
          </button>
        </div>

        <button
          type="button"
          onClick={onToggleTheme}
          title={isDark ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'}
          className="p-2 rounded-lg text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
        >
          {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
        </button>
      </aside>
    );
  }

  return (
    <aside className="no-print flex flex-col w-68 min-w-[260px] max-w-[280px] shrink-0 border-r border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900/95">
      {/* Brand header */}
      <div className="flex items-center justify-between px-4 py-3.5 border-b border-slate-100 dark:border-slate-800">
        <div className="flex items-center gap-2.5">
          <img src="/lumen-icon.svg" alt="LUMEN" className="w-8 h-8 rounded-lg shadow-sm" />
          <div>
            <div className="font-bold text-sm tracking-tight text-slate-900 dark:text-slate-100 leading-none">
              LUMEN
            </div>
            <div className="text-[11px] font-medium text-slate-400 dark:text-slate-500 mt-0.5">
              XML &amp; CFDI Reader
            </div>
          </div>
        </div>
        <button
          type="button"
          onClick={onToggleCollapse}
          title="Contraer barra lateral"
          className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
        >
          <PanelLeftClose className="w-4 h-4" />
        </button>
      </div>

      {/* Primary open action */}
      <div className="p-3.5">
        <button
          type="button"
          onClick={onOpenFile}
          className="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-teal-600 hover:bg-teal-700 active:bg-teal-800 text-white text-sm font-semibold shadow-sm transition-colors"
        >
          <FolderOpen className="w-4 h-4" />
          <span>Abrir XML</span>
        </button>
      </div>

      {/* Recent files list */}
      <div className="flex-1 overflow-y-auto px-3 pb-3">
        <div className="flex items-center justify-between px-1.5 mb-2">
          <div className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wider text-slate-400 dark:text-slate-500">
            <Clock className="w-3.5 h-3.5" />
            <span>Recientes</span>
          </div>
          {recentFiles.length > 0 && (
            <button
              type="button"
              onClick={onClearRecent}
              title="Limpiar historial"
              className="text-[11px] text-slate-400 hover:text-rose-500 dark:hover:text-rose-400 flex items-center gap-1 transition-colors"
            >
              <Trash2 className="w-3 h-3" />
              <span>Limpiar</span>
            </button>
          )}
        </div>

        {recentFiles.length === 0 ? (
          <div className="px-3 py-6 text-center rounded-xl border border-dashed border-slate-200 dark:border-slate-800 text-xs text-slate-400 dark:text-slate-500">
            Tus archivos XML abiertos aparecerán aquí.
          </div>
        ) : (
          <ul className="space-y-1">
            {recentFiles.map((entry) => {
              const meta = XML_DOCUMENT_TYPES[entry.documentType] ?? XML_DOCUMENT_TYPES.GENERIC_XML;
              const isActive = activePath === entry.path;

              return (
                <li key={`${entry.path}-${entry.openedAt}`} className="group relative">
                  <button
                    type="button"
                    onClick={() => onSelectRecent(entry)}
                    className={`w-full text-left px-2.5 py-2 rounded-xl border transition-colors flex items-start gap-2.5 ${
                      isActive
                        ? 'bg-teal-50/80 border-teal-200 dark:bg-teal-950/40 dark:border-teal-800/70'
                        : 'border-transparent hover:bg-slate-100/80 dark:hover:bg-slate-800/60'
                    }`}
                  >
                    <FileCode2
                      className="w-4 h-4 mt-0.5 shrink-0"
                      style={{ color: meta.badgeColor }}
                    />
                    <div className="min-w-0 flex-1 pr-4">
                      <div className="text-xs font-medium text-slate-800 dark:text-slate-200 truncate">
                        {entry.fileName}
                      </div>
                      <div className="flex items-center gap-1.5 mt-0.5 text-[10px] text-slate-400 dark:text-slate-500">
                        <span className="truncate">{meta.label}</span>
                        <span>•</span>
                        <span className="shrink-0">{entry.sizeFormatted}</span>
                      </div>
                    </div>
                  </button>
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      onRemoveRecent(entry);
                    }}
                    title="Quitar de recientes"
                    className="opacity-0 group-hover:opacity-100 absolute right-2 top-2 p-1 rounded-md text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-200/70 dark:hover:bg-slate-700/70 transition-opacity"
                  >
                    <X className="w-3 h-3" />
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      {/* Footer updates + theme toggle */}
      <div className="p-3 border-t border-slate-100 dark:border-slate-800 space-y-2">
        {availableUpdate && (
          <button
            type="button"
            onClick={onOpenUpdateModal}
            className="w-full flex items-center justify-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-teal-50 dark:bg-teal-950/60 border border-teal-200 dark:border-teal-800 text-teal-700 dark:text-teal-300 text-xs font-semibold hover:bg-teal-100 dark:hover:bg-teal-900/60 transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Actualizar a v{availableUpdate.latestVersion}</span>
          </button>
        )}

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <span className="text-[11px] text-slate-400 dark:text-slate-500 font-medium">
              Alenia Studios
            </span>
            <button
              type="button"
              onClick={onCheckUpdate}
              disabled={checkingUpdate}
              title={upToDateFeedback ? 'Ya tienes la última versión' : 'Buscar actualizaciones'}
              className="p-1 rounded-md text-slate-400 hover:text-teal-600 dark:hover:text-teal-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <RefreshCw
                className={`w-3 h-3 ${checkingUpdate ? 'animate-spin text-teal-600' : ''}`}
              />
            </button>
            {upToDateFeedback && (
              <span className="text-[10px] text-emerald-600 dark:text-emerald-400 font-medium">
                Al día
              </span>
            )}
          </div>

          <button
            type="button"
            onClick={onToggleTheme}
            className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            {isDark ? (
              <>
                <Sun className="w-3.5 h-3.5 text-amber-400" />
                <span>Claro</span>
              </>
            ) : (
              <>
                <Moon className="w-3.5 h-3.5 text-indigo-500" />
                <span>Oscuro</span>
              </>
            )}
          </button>
        </div>
      </div>
    </aside>
  );
}
