import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AppStoreDto,
  ExploreItemDetailDto,
  ExploreItemDto,
  ExploreSection,
  WardrobeSuggestionDto,
} from '../../models/app-api.model';

/** Explora y tiendas sugeridas de la API ORLO. */
@Injectable({ providedIn: 'root' })
export class ExploraService {
  private http = inject(HttpClient);
  private url = environment.apiUrl;

  items(section: ExploreSection, style?: string | null): Observable<ExploreItemDto[]> {
    let params = new HttpParams().set('section', section);
    if (style) {
      params = params.set('style', style);
    }
    return this.http.get<ExploreItemDto[]>(`${this.url}/explore/items`, { params });
  }

  item(id: number): Observable<ExploreItemDetailDto> {
    return this.http.get<ExploreItemDetailDto>(`${this.url}/explore/items/${id}`);
  }

  /** Con categoría y color (códigos) de cada prenda: no hace falta enviar más. */
  completarArmario(primaryStyle: string | undefined, wardrobe: { category: string; color: string }[]): Observable<WardrobeSuggestionDto[]> {
    return this.http.post<WardrobeSuggestionDto[]>(`${this.url}/explore/complete-wardrobe`, { primaryStyle, wardrobe });
  }

  tiendas(category?: string | null): Observable<AppStoreDto[]> {
    let params = new HttpParams();
    if (category) {
      params = params.set('category', category);
    }
    return this.http.get<AppStoreDto[]>(`${this.url}/stores`, { params });
  }
}
