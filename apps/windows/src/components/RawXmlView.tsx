import { Check, Copy } from 'lucide-react';
import { useMemo, useState } from 'react';

interface RawXmlViewProps {
  rawXml: string;
}

export function RawXmlView({ rawXml }: RawXmlViewProps) {
  const [copied, setCopied] = useState(false);

  const lines = useMemo(() => rawXml.split(/\r?\n/), [rawXml]);

  const handleCopyAll = async () => {
    try {
      await navigator.clipboard.writeText(rawXml);
      setCopied(true);
      setTimeout(() => setCopied(false), 1800);
    } catch {
      // Ignore clipboard failures
    }
  };

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <div className="flex items-center justify-between px-5 py-2.5 border-b border-slate-200 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-900/50">
        <span className="text-xs text-slate-500 dark:text-slate-400">
          {lines.length} líneas
        </span>
        <button
          type="button"
          onClick={handleCopyAll}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-xs font-medium text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
        >
          {copied ? (
            <>
              <Check className="w-3.5 h-3.5 text-emerald-500" />
              <span>Copiado</span>
            </>
          ) : (
            <>
              <Copy className="w-3.5 h-3.5" />
              <span>Copiar XML</span>
            </>
          )}
        </button>
      </div>

      <div className="flex-1 overflow-auto p-4 font-mono text-xs leading-6 bg-white dark:bg-slate-950/60">
        <table className="w-full border-collapse">
          <tbody>
            {lines.map((line, idx) => (
              <tr
                key={idx}
                className="hover:bg-slate-50 dark:hover:bg-slate-900/60"
              >
                <td className="select-none pr-4 text-right text-slate-400 dark:text-slate-600 w-12 align-top">
                  {idx + 1}
                </td>
                <td className="whitespace-pre-wrap break-all text-slate-800 dark:text-slate-200">
                  {line || ' '}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
