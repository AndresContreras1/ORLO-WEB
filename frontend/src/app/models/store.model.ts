export interface Store {
  id: number;
  name: string;
  logoUrl: string | null;
  garmentTypes: string;
  url: string;
  active: boolean;
  affiliate: boolean;
  /** Categorías de prenda que vende (códigos de CATEGORIAS_PRENDA); sirven para "¿dónde la encuentro?". */
  categories: string[];
}

export type StoreRequest = Omit<Store, 'id'>;
