// Documentos de Firestore, con los nombres de campo exactos de docs/modelo-de-datos.md
// (los mismos DTO que usa la app móvil). Las fechas son milisegundos.

export interface EstiloDoc {
  id: number;
  code: string;
  name: string;
  description: string;
  palette: string[];
  gallery: string[];
}

export interface ResultadoDoc {
  id: string;
  primary: EstiloDoc;
  secondary: EstiloDoc;
  createdAt: number;
}

export interface UsuarioDoc {
  id?: string;
  name: string;
  ageRange?: string;
  garmentPronoun?: string;
  bodyType?: string;
  skinTone?: string;
  hairColor?: string;
  eyeColor?: string;
  climate?: string;
  budget?: string;
  occasions?: string[];
  sensitiveDataConsent?: boolean;
  currentResult?: ResultadoDoc | null;
  photoUrl?: string;
  garmentsCount?: number;
  acceptedTermsAt?: number;
  createdAt?: number;
}

/** Campos del perfil que la persona edita (sin los que escribe el servidor). */
export type PerfilCampos = Pick<UsuarioDoc, 'name' | 'ageRange' | 'garmentPronoun' | 'bodyType' | 'skinTone'
  | 'hairColor' | 'eyeColor' | 'climate' | 'budget' | 'occasions' | 'sensitiveDataConsent'>;

export type EstadoLeccion = 'sin_empezar' | 'en_curso' | 'completada';

export interface ProgresoLeccionDoc {
  lessonId: number;
  status: EstadoLeccion;
  updatedAt: number;
}

export interface PrendaDoc {
  id: string;
  name: string;
  category: string;
  color: string;
  photoUrl?: string | null;
  favorite?: boolean;
  createdAt: number;
}

export interface OutfitDoc {
  id: string;
  name: string;
  occasion: string;
  garmentIds: string[];
  itemsText: string[];
  why: string;
  origin: 'asesor' | 'manual';
  favorite: boolean;
  createdAt: number;
}

export interface ConversacionDoc {
  id: string;
  title: string;
  updatedAt: number;
}

export interface OutfitPropuestoDoc {
  title: string;
  items: string[];
  why: string;
}

export interface MensajeDoc {
  id: string;
  role: 'user' | 'assistant';
  text: string;
  outfits: OutfitPropuestoDoc[];
  followUpQuestion?: string | null;
  createdAt: number;
}

export interface GuardadoDoc {
  itemId: number;
  name: string;
  imageUrl: string;
  category: string;
  savedAt: number;
}

export interface OutfitDelDiaDoc {
  title: string;
  items: string[];
  why: string;
  createdAt: number;
}
