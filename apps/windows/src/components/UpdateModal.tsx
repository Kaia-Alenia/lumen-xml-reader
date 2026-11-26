import { Download, ExternalLink, Loader2, Sparkles, X } from 'lucide-react';
import { useState } from 'react';
import { AppUpdateInfo } from '../domain/updateChecker';
import { installUpdate } from '../services/updateService';

interface UpdateModalProps {
  update: AppUpdateInfo;
  onClose: () => void;
}

export function UpdateModal({ update, onClose }: UpdateModalProps) {
  const [installing, setInstalling] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleInstall = async () => {
    setInstalling(true);
    setErrorMessage(null);
    try {
      await installUpdate(update.downloadUrl);
    } catch (err) {
      setErrorMessage(
        err instanceof Error
          ? err.message
          : 'No se pudo descargar el instalador automáticamente.'
      );
      setInstalling(false);
    }
  };

  return (
    <div className="no-print fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 backdrop-blur-sm p-4">
      <div className="w-full max-w-md rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 shadow-xl overflow-hidden">
        <div className="flex items-start justify-between gap-3 px-5 py-4 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                Nueva versión disponible
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                v{update.currentVersion} → v{update.latestVersion}
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            disabled={installing}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="p-5 space-y-4">
          <div className="text-xs font-semibold text-slate-800 dark:text-slate-200">
            {update.releaseTitle}
          </div>

          {update.releaseNotes && (
            <div className="max-h-40 overflow-y-auto rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/70 p-3 text-xs text-slate-600 dark:text-slate-300 whitespace-pre-wrap leading-relaxed">
              {update.releaseNotes}
            </div>
          )}

          {errorMessage && (
            <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-xs text-rose-700 dark:text-rose-300 space-y-1.5">
              <p>{errorMessage}</p>
              <a
                href={update.releasePageUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-1 font-semibold underline"
              >
                <span>Abrir página de descarga en GitHub</span>
                <ExternalLink className="w-3 h-3" />
              </a>
            </div>
          )}
        </div>

        <div className="flex items-center justify-end gap-2 px-5 py-3.5 bg-slate-50 dark:bg-slate-900/60 border-t border-slate-100 dark:border-slate-800">
          <button
            type="button"
            onClick={onClose}
            disabled={installing}
            className="px-3.5 py-2 rounded-xl text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-200/70 dark:hover:bg-slate-800 disabled:opacity-40 transition-colors"
          >
            Más tarde
          </button>
          <button
            type="button"
            onClick={handleInstall}
            disabled={installing}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-teal-600 hover:bg-teal-700 disabled:opacity-70 text-white text-xs font-semibold shadow-sm transition-colors"
          >
            {installing ? (
              <>
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
                <span>Descargando instalador...</span>
              </>
            ) : (
              <>
                <Download className="w-3.5 h-3.5" />
                <span>Actualizar ahora</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
