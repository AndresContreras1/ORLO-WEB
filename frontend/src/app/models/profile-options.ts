// Opciones del perfil y del armario: el código es lo que se guarda en Firestore (igual que la app
// móvil, OpcionesDelPerfil.kt y ArmarioUi.kt); la etiqueta es lo que se ve; paraAsesor describe a
// la persona ante el asesor de IA.

export interface Opcion {
  codigo: string;
  etiqueta: string;
  paraAsesor: string;
}

export interface OpcionColor extends Opcion {
  hex: string;
}

export const RANGOS_DE_EDAD: Opcion[] = [
  { codigo: 'menos_18', etiqueta: 'Menos de 18', paraAsesor: 'menos de 18 años' },
  { codigo: '18_24', etiqueta: '18 a 24', paraAsesor: 'entre 18 y 24 años' },
  { codigo: '25_34', etiqueta: '25 a 34', paraAsesor: 'entre 25 y 34 años' },
  { codigo: '35_44', etiqueta: '35 a 44', paraAsesor: 'entre 35 y 44 años' },
  { codigo: '45_59', etiqueta: '45 a 59', paraAsesor: 'entre 45 y 59 años' },
  { codigo: '60_mas', etiqueta: '60 o más', paraAsesor: '60 años o más' },
];

export const TRATO_PRENDAS: Opcion[] = [
  { codigo: 'femenino', etiqueta: 'Femenino', paraAsesor: 'femenino' },
  { codigo: 'masculino', etiqueta: 'Masculino', paraAsesor: 'masculino' },
  { codigo: 'neutro', etiqueta: 'Neutro', paraAsesor: 'neutro' },
];

export const TIPOS_DE_CUERPO: Opcion[] = [
  { codigo: 'rectangulo', etiqueta: 'Hombros y cadera parecidos', paraAsesor: 'hombros y cadera de ancho parecido' },
  { codigo: 'triangulo', etiqueta: 'Cadera más ancha', paraAsesor: 'cadera más ancha que los hombros' },
  { codigo: 'triangulo_invertido', etiqueta: 'Hombros más anchos', paraAsesor: 'hombros más anchos que la cadera' },
  { codigo: 'reloj_de_arena', etiqueta: 'Cintura marcada', paraAsesor: 'hombros y cadera parejos con cintura marcada' },
  { codigo: 'ovalo', etiqueta: 'Volumen en el centro', paraAsesor: 'volumen concentrado en el centro' },
  { codigo: 'prefiero_no_decir', etiqueta: 'Prefiero no decir', paraAsesor: '' },
];

export const TONOS_DE_PIEL: OpcionColor[] = [
  { codigo: 'piel_muy_clara', hex: '#F6DCC8', etiqueta: 'Muy clara', paraAsesor: 'muy clara' },
  { codigo: 'piel_clara', hex: '#E8C1A0', etiqueta: 'Clara', paraAsesor: 'clara' },
  { codigo: 'piel_media', hex: '#C99A72', etiqueta: 'Media', paraAsesor: 'media' },
  { codigo: 'piel_morena', hex: '#9C6B47', etiqueta: 'Morena', paraAsesor: 'morena' },
  { codigo: 'piel_oscura', hex: '#6B4429', etiqueta: 'Oscura', paraAsesor: 'oscura' },
  { codigo: 'piel_muy_oscura', hex: '#3F2717', etiqueta: 'Muy oscura', paraAsesor: 'muy oscura' },
];

export const COLORES_DE_CABELLO: OpcionColor[] = [
  { codigo: 'cabello_negro', hex: '#1F1B18', etiqueta: 'Negro', paraAsesor: 'negro' },
  { codigo: 'cabello_castano_oscuro', hex: '#4A3022', etiqueta: 'Castaño oscuro', paraAsesor: 'castaño oscuro' },
  { codigo: 'cabello_castano_claro', hex: '#8A5A3B', etiqueta: 'Castaño claro', paraAsesor: 'castaño claro' },
  { codigo: 'cabello_rubio', hex: '#D9B26F', etiqueta: 'Rubio', paraAsesor: 'rubio' },
  { codigo: 'cabello_rojizo', hex: '#A9442B', etiqueta: 'Rojizo', paraAsesor: 'rojizo' },
  { codigo: 'cabello_gris', hex: '#B8B4AE', etiqueta: 'Gris o canoso', paraAsesor: 'gris o canoso' },
];

export const COLORES_DE_OJOS: OpcionColor[] = [
  { codigo: 'ojos_negros', hex: '#231A15', etiqueta: 'Negros', paraAsesor: 'negros' },
  { codigo: 'ojos_cafes', hex: '#5E3B22', etiqueta: 'Cafés', paraAsesor: 'cafés' },
  { codigo: 'ojos_miel', hex: '#A6762E', etiqueta: 'Miel', paraAsesor: 'miel' },
  { codigo: 'ojos_verdes', hex: '#5F7D4E', etiqueta: 'Verdes', paraAsesor: 'verdes' },
  { codigo: 'ojos_azules', hex: '#4F7BA8', etiqueta: 'Azules', paraAsesor: 'azules' },
  { codigo: 'ojos_grises', hex: '#8A9099', etiqueta: 'Grises', paraAsesor: 'grises' },
];

export const CLIMAS: Opcion[] = [
  { codigo: 'calido', etiqueta: 'Cálido', paraAsesor: 'cálido' },
  { codigo: 'templado', etiqueta: 'Templado', paraAsesor: 'templado' },
  { codigo: 'frio', etiqueta: 'Frío', paraAsesor: 'frío' },
  { codigo: 'variable', etiqueta: 'Variable', paraAsesor: 'variable' },
];

export const PRESUPUESTOS: Opcion[] = [
  { codigo: 'bajo', etiqueta: 'Bajo', paraAsesor: 'bajo' },
  { codigo: 'medio', etiqueta: 'Medio', paraAsesor: 'medio' },
  { codigo: 'alto', etiqueta: 'Alto', paraAsesor: 'alto' },
  { codigo: 'prefiero_no_decir', etiqueta: 'Prefiero no decir', paraAsesor: '' },
];

export const OCASIONES: Opcion[] = [
  { codigo: 'estudio', etiqueta: 'Estudio', paraAsesor: 'estudio' },
  { codigo: 'trabajo', etiqueta: 'Trabajo', paraAsesor: 'trabajo' },
  { codigo: 'salidas', etiqueta: 'Salidas', paraAsesor: 'salidas' },
  { codigo: 'casa', etiqueta: 'Casa', paraAsesor: 'casa' },
  { codigo: 'deporte', etiqueta: 'Deporte', paraAsesor: 'deporte' },
  { codigo: 'eventos', etiqueta: 'Eventos', paraAsesor: 'eventos' },
];

export const CATEGORIAS_PRENDA: Opcion[] = [
  { codigo: 'arriba', etiqueta: 'Parte de arriba', paraAsesor: 'parte de arriba' },
  { codigo: 'abajo', etiqueta: 'Parte de abajo', paraAsesor: 'parte de abajo' },
  { codigo: 'abrigo', etiqueta: 'Abrigo', paraAsesor: 'abrigo' },
  { codigo: 'calzado', etiqueta: 'Calzado', paraAsesor: 'calzado' },
  { codigo: 'accesorio', etiqueta: 'Accesorio', paraAsesor: 'accesorio' },
  { codigo: 'otro', etiqueta: 'Otro', paraAsesor: 'otro' },
];

export const COLORES_PRENDA: OpcionColor[] = [
  { codigo: 'negro', hex: '#1F1F1F', etiqueta: 'Negro', paraAsesor: 'negro' },
  { codigo: 'blanco', hex: '#F7F7F7', etiqueta: 'Blanco', paraAsesor: 'blanco' },
  { codigo: 'gris', hex: '#9E9E9E', etiqueta: 'Gris', paraAsesor: 'gris' },
  { codigo: 'beige', hex: '#D9C3A5', etiqueta: 'Beige', paraAsesor: 'beige' },
  { codigo: 'cafe', hex: '#7A5230', etiqueta: 'Café', paraAsesor: 'café' },
  { codigo: 'azul', hex: '#3D5A80', etiqueta: 'Azul', paraAsesor: 'azul' },
  { codigo: 'celeste', hex: '#98C1D9', etiqueta: 'Celeste', paraAsesor: 'celeste' },
  { codigo: 'verde', hex: '#5C7F5A', etiqueta: 'Verde', paraAsesor: 'verde' },
  { codigo: 'rojo', hex: '#C0392B', etiqueta: 'Rojo', paraAsesor: 'rojo' },
  { codigo: 'rosado', hex: '#E8A0B4', etiqueta: 'Rosado', paraAsesor: 'rosado' },
  { codigo: 'amarillo', hex: '#E9C46A', etiqueta: 'Amarillo', paraAsesor: 'amarillo' },
  { codigo: 'morado', hex: '#7B5EA7', etiqueta: 'Morado', paraAsesor: 'morado' },
  { codigo: 'estampado', hex: '#BDB2A7', etiqueta: 'Estampado', paraAsesor: 'estampado' },
];

export const OCASIONES_OUTFIT: Opcion[] = [
  { codigo: 'universidad', etiqueta: 'Universidad', paraAsesor: 'universidad' },
  { codigo: 'trabajo', etiqueta: 'Trabajo', paraAsesor: 'trabajo' },
  { codigo: 'salida', etiqueta: 'Salida', paraAsesor: 'salida' },
  { codigo: 'otra', etiqueta: 'Otra', paraAsesor: 'otra' },
];

export const CONTEXTOS_COMBINAR: Record<string, string> = {
  casual: 'Casual',
  universidad: 'Universidad',
  elegante: 'Elegante',
};

export const MIN_PASSWORD = 6;
export const MAX_NOMBRE = 40;

/** Etiqueta de un código, o el código si no está en la lista. */
export function etiqueta(opciones: Opcion[], codigo: string | undefined | null): string {
  return opciones.find((o) => o.codigo === codigo)?.etiqueta ?? codigo ?? '';
}

export function hexDe(opciones: OpcionColor[], codigo: string | undefined | null): string | null {
  return opciones.find((o) => o.codigo === codigo)?.hex ?? null;
}

/** Texto para el asesor; undefined si no se eligió nada o la opción no tiene texto. */
export function paraAsesor(opciones: Opcion[], codigo: string | undefined | null): string | undefined {
  const texto = opciones.find((o) => o.codigo === codigo)?.paraAsesor;
  return texto ? texto : undefined;
}
