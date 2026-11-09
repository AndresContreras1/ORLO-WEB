export interface Store {
  id: number;
  name: string;
  logoUrl: string | null;
  garmentTypes: string;
  url: string;
  active: boolean;
  affiliate: boolean;
}

export type StoreRequest = Omit<Store, 'id'>;
