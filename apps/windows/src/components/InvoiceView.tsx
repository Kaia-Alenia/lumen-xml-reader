import { Building2, Check, Copy, Info, ReceiptText, UserCheck } from 'lucide-react';
import { useState } from 'react';
import { InvoiceData } from '../domain/types';

interface InvoiceViewProps {
  invoice: InvoiceData;
}

export function InvoiceView({ invoice }: InvoiceViewProps) {
  const [copiedUuid, setCopiedUuid] = useState(false);

  const handleCopyUuid = async () => {
    if (!invoice.uuid) return;
    try {
      await navigator.clipboard.writeText(invoice.uuid);
      setCopiedUuid(true);
      setTimeout(() => setCopiedUuid(false), 1800);
    } catch {
      // Ignore clipboard failures
    }
  };

  return (
    <div className="print-area flex-1 overflow-y-auto p-6">
      <div className="max-w-4xl mx-auto space-y-5">
        {!invoice.isCfdi && (
          <div className="flex items-start gap-3 p-4 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-800 dark:text-rose-200 text-xs">
            <Info className="w-4 h-4 mt-0.5 shrink-0" />
            <span>
              Este archivo XML no contiene una estructura CFDI estándar. Se muestran los campos que
              pudieron extraerse automáticamente.
            </span>
          </div>
        )}

        {/* Header summary card */}
        <section className="print-card rounded-2xl bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/80 p-5 shadow-sm">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="space-y-1.5">
              <div className="flex items-center gap-2.5">
                <span className="px-2.5 py-0.5 rounded-md bg-teal-600 text-white text-[11px] font-bold tracking-wide">
                  FACTURA SAT
                </span>
                {invoice.serie && (
                  <span className="text-xs font-mono text-slate-500 dark:text-slate-400">
                    Serie: {invoice.serie}
                  </span>
                )}
              </div>
              <h3 className="text-xl font-bold text-slate-900 dark:text-slate-100">
                Folio: {invoice.folio}
              </h3>
              {invoice.uuid && (
                <div className="flex items-center gap-2 pt-1">
                  <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                    UUID:
                  </span>
                  <code className="text-xs font-mono text-slate-600 dark:text-slate-300 bg-slate-100 dark:bg-slate-900/70 px-2 py-0.5 rounded">
                    {invoice.uuid}
                  </code>
                  <button
                    type="button"
                    onClick={handleCopyUuid}
                    title="Copiar UUID"
                    className="no-print p-1 rounded hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-400 hover:text-teal-600 dark:hover:text-teal-400 transition-colors"
                  >
                    {copiedUuid ? (
                      <Check className="w-3.5 h-3.5 text-emerald-500" />
                    ) : (
                      <Copy className="w-3.5 h-3.5" />
                    )}
                  </button>
                </div>
              )}
            </div>

            <div className="text-right space-y-1">
              {invoice.fecha && (
                <div className="text-xs font-mono text-slate-500 dark:text-slate-400">
                  {invoice.fecha}
                </div>
              )}
              <div className="text-sm font-semibold text-teal-700 dark:text-teal-400">
                Moneda: {invoice.moneda}
              </div>
              {(invoice.formaPago || invoice.metodoPago) && (
                <div className="text-[11px] text-slate-500 dark:text-slate-400">
                  {invoice.metodoPago && <span>Método: {invoice.metodoPago}</span>}
                  {invoice.metodoPago && invoice.formaPago && <span> • </span>}
                  {invoice.formaPago && <span>Forma: {invoice.formaPago}</span>}
                </div>
              )}
            </div>
          </div>
        </section>

        {/* Emisor & Receptor */}
        <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="print-card rounded-2xl bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/80 p-5 shadow-sm">
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-teal-700 dark:text-teal-400 mb-3">
              <Building2 className="w-4 h-4" />
              <span>Emisor</span>
            </div>
            <div className="text-sm font-bold text-slate-900 dark:text-slate-100">
              {invoice.emisorNombre}
            </div>
            <div className="mt-2 space-y-1 text-xs text-slate-600 dark:text-slate-300">
              <div>
                <span className="text-slate-400">RFC: </span>
                <span className="font-mono font-semibold">{invoice.emisorRfc}</span>
              </div>
              <div>
                <span className="text-slate-400">Régimen Fiscal: </span>
                <span>{invoice.emisorRegimen}</span>
              </div>
            </div>
          </div>

          <div className="print-card rounded-2xl bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/80 p-5 shadow-sm">
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-indigo-600 dark:text-indigo-400 mb-3">
              <UserCheck className="w-4 h-4" />
              <span>Receptor</span>
            </div>
            <div className="text-sm font-bold text-slate-900 dark:text-slate-100">
              {invoice.receptorNombre}
            </div>
            <div className="mt-2 space-y-1 text-xs text-slate-600 dark:text-slate-300">
              <div>
                <span className="text-slate-400">RFC: </span>
                <span className="font-mono font-semibold">{invoice.receptorRfc}</span>
              </div>
              <div className="flex flex-wrap gap-x-4 gap-y-1">
                <span>
                  <span className="text-slate-400">Uso CFDI: </span>
                  {invoice.receptorUsoCfdi}
                </span>
                <span>
                  <span className="text-slate-400">C.P.: </span>
                  {invoice.receptorDomicilio}
                </span>
              </div>
            </div>
          </div>
        </section>

        {/* Conceptos */}
        <section className="print-card rounded-2xl bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/80 overflow-hidden shadow-sm">
          <div className="flex items-center justify-between px-5 py-3.5 border-b border-slate-100 dark:border-slate-700/80">
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-200">
              <ReceiptText className="w-4 h-4 text-teal-600 dark:text-teal-400" />
              <span>Conceptos ({invoice.conceptos.length})</span>
            </div>
          </div>

          {invoice.conceptos.length === 0 ? (
            <div className="p-6 text-center text-xs text-slate-400">
              No se encontraron conceptos detallados en el documento.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-50 dark:bg-slate-900/50 text-[11px] uppercase tracking-wider text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-700/80">
                    <th className="py-2.5 px-4 font-semibold">Clave</th>
                    <th className="py-2.5 px-3 font-semibold text-right">Cant.</th>
                    <th className="py-2.5 px-3 font-semibold">Unidad</th>
                    <th className="py-2.5 px-4 font-semibold">Descripción</th>
                    <th className="py-2.5 px-4 font-semibold text-right">P. Unitario</th>
                    <th className="py-2.5 px-4 font-semibold text-right">Importe</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {invoice.conceptos.map((item, idx) => (
                    <tr
                      key={`${item.clave}-${idx}`}
                      className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40 transition-colors"
                    >
                      <td className="py-3 px-4 font-mono text-[11px] text-slate-500 dark:text-slate-400 whitespace-nowrap">
                        {item.clave}
                      </td>
                      <td className="py-3 px-3 text-right tabular-nums text-slate-700 dark:text-slate-300">
                        {item.cantidad}
                      </td>
                      <td className="py-3 px-3 text-slate-500 dark:text-slate-400 whitespace-nowrap">
                        {item.unidad}
                      </td>
                      <td className="py-3 px-4 text-slate-800 dark:text-slate-200 font-medium">
                        {item.descripcion}
                      </td>
                      <td className="py-3 px-4 text-right font-mono tabular-nums text-slate-700 dark:text-slate-300 whitespace-nowrap">
                        ${item.valorUnitario}
                      </td>
                      <td className="py-3 px-4 text-right font-mono tabular-nums font-semibold text-slate-900 dark:text-slate-100 whitespace-nowrap">
                        ${item.importe}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        {/* Totals */}
        <section className="print-card flex justify-end">
          <div className="w-full sm:w-80 rounded-2xl bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/80 p-5 shadow-sm space-y-2.5">
            <div className="flex items-center justify-between text-xs text-slate-600 dark:text-slate-300">
              <span>Subtotal</span>
              <span className="font-mono tabular-nums font-medium">
                ${invoice.subtotal} {invoice.moneda}
              </span>
            </div>
            <div className="flex items-center justify-between text-xs text-slate-600 dark:text-slate-300">
              <span>IVA / Impuestos</span>
              <span className="font-mono tabular-nums font-medium">
                ${invoice.iva} {invoice.moneda}
              </span>
            </div>
            <div className="pt-2.5 border-t border-slate-200 dark:border-slate-700 flex items-center justify-between rounded-xl px-3 py-2.5 bg-amber-50 dark:bg-amber-950/40 border border-amber-200/70 dark:border-amber-800/60">
              <span className="text-xs font-bold uppercase tracking-wider text-amber-900 dark:text-amber-200">
                Total
              </span>
              <span className="text-base font-bold font-mono tabular-nums text-amber-900 dark:text-amber-200">
                ${invoice.total} {invoice.moneda}
              </span>
            </div>
          </div>
        </section>

        <footer className="text-center text-[11px] text-slate-400 dark:text-slate-500 pt-2 pb-4">
          Visualizado con LUMEN — Alenia Studios
        </footer>
      </div>
    </div>
  );
}
