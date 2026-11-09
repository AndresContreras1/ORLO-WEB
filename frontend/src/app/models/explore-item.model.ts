export interface CombineIdea {
  context: 'casual' | 'universidad' | 'elegante';
  items: string[];
}

/** Prenda del catálogo de Explora, como la devuelve /admin/explore-items. */
export interface ExploreItem {
  id: number;
  name: string;
  imageUrl: string;
  imageCredit: string | null;
  category: string;
  color: string;
  styles: string[];
  whyItFits: string;
  trend: boolean;
  accessory: boolean;
  active: boolean;
  combineIdeas: CombineIdea[];
}

export type ExploreItemRequest = Omit<ExploreItem, 'id'>;
