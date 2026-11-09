/** Igual a StyleDto del backend. */
export interface Style {
  id: number;
  code: string;
  name: string;
  description: string;
  palette: string[];
  gallery: string[];
}

/** Igual a StyleRequest del backend (sin id: lo decide el servidor). */
export type StyleRequest = Omit<Style, 'id'>;
