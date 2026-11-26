import { invoke } from '@tauri-apps/api/core';
import { getCurrentWebview } from '@tauri-apps/api/webview';
import { open } from '@tauri-apps/plugin-dialog';

export interface LoadedXmlPayload {
  path: string;
  fileName: string;
  sizeBytes: number;
  content: string;
}

interface RustLoadedXmlFile {
  path: string;
  file_name: string;
  size_bytes: number;
  content: string;
}

export function isTauriEnvironment(): boolean {
  return typeof window !== 'undefined' && '__TAURI_INTERNALS__' in window;
}

export async function readXmlFromPath(path: string): Promise<LoadedXmlPayload> {
  if (!isTauriEnvironment()) {
    throw new Error('Lectura por ruta disponible únicamente en la aplicación de escritorio.');
  }

  const result = await invoke<RustLoadedXmlFile>('read_xml_file', { path });
  return {
    path: result.path,
    fileName: result.file_name,
    sizeBytes: result.size_bytes,
    content: result.content,
  };
}

export async function getLaunchXmlPath(): Promise<string | null> {
  if (!isTauriEnvironment()) return null;
  try {
    return await invoke<string | null>('get_launch_file');
  } catch {
    return null;
  }
}

export async function openXmlFileDialog(): Promise<LoadedXmlPayload | null> {
  if (isTauriEnvironment()) {
    const selected = await open({
      multiple: false,
      title: 'Abrir archivo XML o Factura CFDI',
      filters: [
        {
          name: 'Archivos XML / CFDI',
          extensions: ['xml'],
        },
        {
          name: 'Todos los archivos',
          extensions: ['*'],
        },
      ],
    });

    if (!selected || Array.isArray(selected)) {
      return null;
    }

    return readXmlFromPath(selected);
  }

  return new Promise((resolve) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.xml,text/xml,application/xml';
    input.onchange = async () => {
      const file = input.files?.[0];
      if (!file) {
        resolve(null);
        return;
      }
      const content = await file.text();
      resolve({
        path: file.name,
        fileName: file.name,
        sizeBytes: file.size,
        content,
      });
    };
    input.click();
  });
}

export async function subscribeToTauriDrop(
  onDropPaths: (paths: string[]) => void,
  onHoverChange?: (hovering: boolean) => void
): Promise<() => void> {
  if (!isTauriEnvironment()) {
    return () => {};
  }

  try {
    const unlisten = await getCurrentWebview().onDragDropEvent((event) => {
      if (event.payload.type === 'enter' || event.payload.type === 'over') {
        onHoverChange?.(true);
      } else if (event.payload.type === 'leave') {
        onHoverChange?.(false);
      } else if (event.payload.type === 'drop') {
        onHoverChange?.(false);
        if (event.payload.paths.length > 0) {
          onDropPaths(event.payload.paths);
        }
      }
    });
    return unlisten;
  } catch {
    return () => {};
  }
}
