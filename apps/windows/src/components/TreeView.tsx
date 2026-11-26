import {
  Check,
  ChevronDown,
  ChevronRight,
  ChevronsDownUp,
  ChevronsUpDown,
  Copy,
  Search,
  X,
} from 'lucide-react';
import { useMemo, useState } from 'react';
import { XmlNode } from '../domain/types';

interface TreeViewProps {
  rootNode: XmlNode;
}

function collectAllNodeIds(node: XmlNode): Set<string> {
  const ids = new Set<string>([node.id]);
  for (const child of node.children) {
    for (const id of collectAllNodeIds(child)) {
      ids.add(id);
    }
  }
  return ids;
}

function collectInitialExpanded(node: XmlNode, maxDepth = 2): Set<string> {
  const ids = new Set<string>();
  function walk(n: XmlNode) {
    if (n.level <= maxDepth && n.children.length > 0) {
      ids.add(n.id);
      for (const child of n.children) {
        walk(child);
      }
    }
  }
  walk(node);
  return ids;
}

function nodeMatchesQuery(node: XmlNode, lowerQuery: string): boolean {
  if (node.name.toLowerCase().includes(lowerQuery)) return true;
  if (node.textValue && node.textValue.toLowerCase().includes(lowerQuery)) return true;
  for (const [k, v] of Object.entries(node.attributes)) {
    if (k.toLowerCase().includes(lowerQuery) || v.toLowerCase().includes(lowerQuery)) {
      return true;
    }
  }
  return false;
}

function collectMatchingSubtreeIds(
  node: XmlNode,
  lowerQuery: string,
  matches: Set<string>,
  ancestors: Set<string>
): boolean {
  const selfMatch = nodeMatchesQuery(node, lowerQuery);
  if (selfMatch) matches.add(node.id);

  let childMatched = false;
  for (const child of node.children) {
    if (collectMatchingSubtreeIds(child, lowerQuery, matches, ancestors)) {
      childMatched = true;
    }
  }

  if (childMatched) {
    ancestors.add(node.id);
  }
  return selfMatch || childMatched;
}

interface TreeNodeRowProps {
  node: XmlNode;
  expandedIds: Set<string>;
  selectedId: string | null;
  matchIds: Set<string> | null;
  visibleIds: Set<string> | null;
  onToggleExpand: (id: string) => void;
  onSelectNode: (node: XmlNode) => void;
}

function TreeNodeRow({
  node,
  expandedIds,
  selectedId,
  matchIds,
  visibleIds,
  onToggleExpand,
  onSelectNode,
}: TreeNodeRowProps) {
  if (visibleIds && !visibleIds.has(node.id)) {
    return null;
  }

  const hasChildren = node.children.length > 0;
  const isExpanded = expandedIds.has(node.id) || (visibleIds !== null && hasChildren);
  const isSelected = selectedId === node.id;
  const isMatched = matchIds?.has(node.id) ?? false;
  const attrEntries = Object.entries(node.attributes);

  return (
    <div>
      <div
        onClick={() => onSelectNode(node)}
        style={{ paddingLeft: `${node.level * 18 + 8}px` }}
        className={`group flex items-start gap-1.5 py-1.5 pr-3 rounded-lg cursor-pointer transition-colors text-xs ${
          isSelected
            ? 'bg-teal-50 dark:bg-teal-950/50 ring-1 ring-teal-500/40'
            : isMatched
              ? 'bg-amber-50/90 dark:bg-amber-950/30'
              : 'hover:bg-slate-100/80 dark:hover:bg-slate-800/60'
        }`}
      >
        {/* Expand/collapse button */}
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            if (hasChildren) onToggleExpand(node.id);
          }}
          className={`mt-0.5 w-4 h-4 flex items-center justify-center rounded shrink-0 ${
            hasChildren
              ? 'text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-200/70 dark:hover:bg-slate-700'
              : 'opacity-0 pointer-events-none'
          }`}
        >
          {isExpanded ? (
            <ChevronDown className="w-3.5 h-3.5" />
          ) : (
            <ChevronRight className="w-3.5 h-3.5" />
          )}
        </button>

        {/* Tag, attributes, text */}
        <div className="min-w-0 flex-1 flex flex-wrap items-center gap-1.5 leading-relaxed">
          {node.namespacePrefix && (
            <span className="px-1.5 py-0.5 rounded bg-sky-100 dark:bg-sky-950 text-sky-700 dark:text-sky-300 font-mono text-[10px] font-semibold">
              {node.namespacePrefix}
            </span>
          )}
          <span className="font-mono font-semibold text-teal-700 dark:text-teal-300">
            {node.namespacePrefix ? node.localName : node.name}
          </span>

          {attrEntries.slice(0, 4).map(([key, value]) => (
            <span
              key={key}
              className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded bg-slate-100 dark:bg-slate-800 border border-slate-200/70 dark:border-slate-700/70 font-mono text-[11px] max-w-[260px]"
            >
              <span className="text-sky-700 dark:text-sky-400 shrink-0">{key}=</span>
              <span className="text-amber-700 dark:text-amber-300 truncate">
                &quot;{value}&quot;
              </span>
            </span>
          ))}

          {attrEntries.length > 4 && (
            <span className="text-[10px] text-slate-400 font-medium">
              +{attrEntries.length - 4} más
            </span>
          )}

          {node.textValue && (
            <span className="text-slate-600 dark:text-slate-300 font-sans truncate max-w-[360px] bg-slate-50 dark:bg-slate-900/60 px-1.5 py-0.5 rounded">
              {node.textValue}
            </span>
          )}
        </div>
      </div>

      {hasChildren && isExpanded && (
        <div className="relative">
          <div
            style={{ left: `${node.level * 18 + 15}px` }}
            className="absolute top-0 bottom-0 w-px bg-slate-200 dark:bg-slate-800 pointer-events-none"
          />
          {node.children.map((child) => (
            <TreeNodeRow
              key={child.id}
              node={child}
              expandedIds={expandedIds}
              selectedId={selectedId}
              matchIds={matchIds}
              visibleIds={visibleIds}
              onToggleExpand={onToggleExpand}
              onSelectNode={onSelectNode}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export function TreeView({ rootNode }: TreeViewProps) {
  const [expandedIds, setExpandedIds] = useState<Set<string>>(() =>
    collectInitialExpanded(rootNode)
  );
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedNode, setSelectedNode] = useState<XmlNode | null>(null);
  const [copiedKey, setCopiedKey] = useState<string | null>(null);

  const searchResult = useMemo(() => {
    const trimmed = searchQuery.trim().toLowerCase();
    if (!trimmed) return null;

    const matches = new Set<string>();
    const ancestors = new Set<string>();
    collectMatchingSubtreeIds(rootNode, trimmed, matches, ancestors);

    const visible = new Set<string>([...matches, ...ancestors]);
    return { matches, visible };
  }, [rootNode, searchQuery]);

  const handleToggleExpand = (id: string) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const handleExpandAll = () => {
    setExpandedIds(collectAllNodeIds(rootNode));
  };

  const handleCollapseAll = () => {
    setExpandedIds(new Set([rootNode.id]));
  };

  const copyText = async (label: string, text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedKey(label);
      setTimeout(() => setCopiedKey(null), 1600);
    } catch {
      // Ignore clipboard failures
    }
  };

  return (
    <div className="flex-1 flex flex-col min-h-0">
      {/* Toolbar */}
      <div className="flex flex-wrap items-center justify-between gap-2 px-5 py-2.5 border-b border-slate-200 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-900/50">
        <div className="relative flex-1 min-w-[220px] max-w-md">
          <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Buscar etiqueta, atributo o valor..."
            className="w-full pl-8 pr-8 py-1.5 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-xs text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-500/40"
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => setSearchQuery('')}
              className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        <div className="flex items-center gap-2">
          {searchResult && (
            <span className="text-[11px] font-medium text-amber-600 dark:text-amber-400">
              {searchResult.matches.size} coincidencia(s)
            </span>
          )}
          <button
            type="button"
            onClick={handleExpandAll}
            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-200/70 dark:hover:bg-slate-800 transition-colors"
          >
            <ChevronsUpDown className="w-3.5 h-3.5" />
            <span>Expandir</span>
          </button>
          <button
            type="button"
            onClick={handleCollapseAll}
            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-200/70 dark:hover:bg-slate-800 transition-colors"
          >
            <ChevronsDownUp className="w-3.5 h-3.5" />
            <span>Contraer</span>
          </button>
        </div>
      </div>

      {/* Main split: Tree + optional Node Inspector */}
      <div className="flex-1 flex min-h-0">
        <div className="flex-1 overflow-auto p-3">
          {searchResult && searchResult.matches.size === 0 ? (
            <div className="py-12 text-center text-xs text-slate-400">
              No se encontraron nodos que coincidan con &quot;{searchQuery}&quot;.
            </div>
          ) : (
            <TreeNodeRow
              node={rootNode}
              expandedIds={expandedIds}
              selectedId={selectedNode?.id ?? null}
              matchIds={searchResult?.matches ?? null}
              visibleIds={searchResult?.visible ?? null}
              onToggleExpand={handleToggleExpand}
              onSelectNode={setSelectedNode}
            />
          )}
        </div>

        {selectedNode && (
          <aside className="w-80 shrink-0 border-l border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-y-auto p-4 space-y-4">
            <div className="flex items-start justify-between gap-2">
              <div className="min-w-0">
                <div className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">
                  Inspector de Nodo
                </div>
                <div className="font-mono text-sm font-bold text-teal-700 dark:text-teal-300 break-all mt-0.5">
                  &lt;{selectedNode.name}&gt;
                </div>
              </div>
              <button
                type="button"
                onClick={() => setSelectedNode(null)}
                className="p-1 rounded-md text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {selectedNode.textValue && (
              <div className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] font-semibold text-slate-500 dark:text-slate-400">
                    Texto interno
                  </span>
                  <button
                    type="button"
                    onClick={() => copyText('text', selectedNode.textValue ?? '')}
                    className="flex items-center gap-1 text-[11px] text-teal-600 dark:text-teal-400 hover:underline"
                  >
                    {copiedKey === 'text' ? (
                      <>
                        <Check className="w-3 h-3" />
                        <span>Copiado</span>
                      </>
                    ) : (
                      <>
                        <Copy className="w-3 h-3" />
                        <span>Copiar</span>
                      </>
                    )}
                  </button>
                </div>
                <div className="p-2.5 rounded-lg bg-slate-50 dark:bg-slate-800/70 border border-slate-200 dark:border-slate-700 text-xs font-mono break-words">
                  {selectedNode.textValue}
                </div>
              </div>
            )}

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-semibold text-slate-500 dark:text-slate-400">
                  Atributos ({Object.keys(selectedNode.attributes).length})
                </span>
                {Object.keys(selectedNode.attributes).length > 0 && (
                  <button
                    type="button"
                    onClick={() =>
                      copyText('json', JSON.stringify(selectedNode.attributes, null, 2))
                    }
                    className="flex items-center gap-1 text-[11px] text-teal-600 dark:text-teal-400 hover:underline"
                  >
                    {copiedKey === 'json' ? (
                      <>
                        <Check className="w-3 h-3" />
                        <span>Copiado</span>
                      </>
                    ) : (
                      <>
                        <Copy className="w-3 h-3" />
                        <span>Copiar JSON</span>
                      </>
                    )}
                  </button>
                )}
              </div>

              {Object.keys(selectedNode.attributes).length === 0 ? (
                <div className="text-xs text-slate-400">Sin atributos</div>
              ) : (
                <div className="space-y-2">
                  {Object.entries(selectedNode.attributes).map(([key, value]) => (
                    <div
                      key={key}
                      className="p-2.5 rounded-lg bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/70 space-y-1"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span className="font-mono text-[11px] font-semibold text-sky-700 dark:text-sky-400 break-all">
                          {key}
                        </span>
                        <button
                          type="button"
                          onClick={() => copyText(key, value)}
                          title="Copiar valor"
                          className="text-slate-400 hover:text-teal-600 dark:hover:text-teal-400 shrink-0"
                        >
                          {copiedKey === key ? (
                            <Check className="w-3 h-3 text-emerald-500" />
                          ) : (
                            <Copy className="w-3 h-3" />
                          )}
                        </button>
                      </div>
                      <div className="font-mono text-xs text-amber-700 dark:text-amber-300 break-all">
                        {value}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </aside>
        )}
      </div>
    </div>
  );
}
