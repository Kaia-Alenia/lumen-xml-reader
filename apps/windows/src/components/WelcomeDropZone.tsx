import { FileCheck2, FolderOpen, GitFork, Printer, UploadCloud } from 'lucide-react';

interface WelcomeDropZoneProps {
  onOpenFile: () => void;
  isDragging: boolean;
}

export function WelcomeDropZone({ onOpenFile, isDragging }: WelcomeDropZoneProps) {
  return (
    <div className="flex-1 flex items-center justify-center p-8 overflow-y-auto">
      <div className="w-full max-w-xl text-center space-y-8">
        <div className="flex flex-col items-center gap-3">
          <img
            src="/lumen-icon.svg"
            alt="LUMEN"
            className="w-16 h-16 rounded-2xl shadow-md"
          />
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">
              Bienvenido a LUMEN
            </h1>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
              Inspecciona facturas CFDI y cualquier archivo XML con claridad y sin código crudo.
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={onOpenFile}
          className={`w-full group rounded-2xl border-2 border-dashed p-10 transition-all cursor-pointer flex flex-col items-center gap-4 ${
            isDragging
              ? 'border-teal-500 bg-teal-50/70 dark:bg-teal-950/30 scale-[1.01]'
              : 'border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800/40 hover:border-teal-500/70 hover:bg-slate-50/80 dark:hover:bg-slate-800/70'
          }`}
        >
          <div className="w-14 h-14 rounded-2xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center group-hover:scale-105 transition-transform">
            <UploadCloud className="w-7 h-7" />
          </div>
          <div>
            <p className="text-base font-semibold text-slate-800 dark:text-slate-200">
              Arrastra tu archivo .xml aquí
            </p>
            <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">
              o haz clic para seleccionarlo desde tu equipo (Ctrl + O)
            </p>
          </div>
          <span className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-teal-600 group-hover:bg-teal-700 text-white text-xs font-semibold shadow-sm transition-colors">
            <FolderOpen className="w-4 h-4" />
            <span>Explorar archivos</span>
          </span>
        </button>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-left">
          <div className="p-3.5 rounded-xl bg-white dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800">
            <FileCheck2 className="w-4 h-4 text-teal-600 dark:text-teal-400 mb-1.5" />
            <div className="text-xs font-semibold text-slate-800 dark:text-slate-200">
              Facturas CFDI 3.3 / 4.0
            </div>
            <div className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
              Emisor, receptor, conceptos, impuestos y folio fiscal.
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-white dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800">
            <GitFork className="w-4 h-4 text-indigo-600 dark:text-indigo-400 mb-1.5" />
            <div className="text-xs font-semibold text-slate-800 dark:text-slate-200">
              Árbol XML Interactivo
            </div>
            <div className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
              Búsqueda instantánea, namespaces y chips de atributos.
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-white dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800">
            <Printer className="w-4 h-4 text-amber-500 mb-1.5" />
            <div className="text-xs font-semibold text-slate-800 dark:text-slate-200">
              Exportación a PDF
            </div>
            <div className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
              Imprime o guarda cualquier factura limpia con un clic.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
