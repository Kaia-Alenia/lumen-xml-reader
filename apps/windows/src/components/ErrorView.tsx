import { AlertTriangle, FolderOpen } from 'lucide-react';

interface ErrorViewProps {
  message: string;
  line: number;
  column: number;
  onOpenFile: () => void;
}

export function ErrorView({ message, line, column, onOpenFile }: ErrorViewProps) {
  return (
    <div className="flex-1 flex items-center justify-center p-8">
      <div className="max-w-md w-full rounded-2xl bg-white dark:bg-slate-800/70 border border-rose-200 dark:border-rose-900 p-6 text-center space-y-4 shadow-sm">
        <div className="w-12 h-12 mx-auto rounded-2xl bg-rose-100 dark:bg-rose-950 text-rose-600 dark:text-rose-400 flex items-center justify-center">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <div>
          <h3 className="text-base font-bold text-slate-900 dark:text-slate-100">
            No se pudo leer el archivo XML
          </h3>
          <p className="text-xs text-slate-600 dark:text-slate-300 mt-1.5 break-words">
            {message}
          </p>
          {(line > 0 || column > 0) && (
            <p className="text-[11px] font-mono text-rose-600 dark:text-rose-400 mt-2">
              Línea {line}, Columna {column}
            </p>
          )}
        </div>
        <button
          type="button"
          onClick={onOpenFile}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-teal-600 hover:bg-teal-700 text-white text-xs font-semibold transition-colors"
        >
          <FolderOpen className="w-4 h-4" />
          <span>Abrir otro archivo</span>
        </button>
      </div>
    </div>
  );
}
