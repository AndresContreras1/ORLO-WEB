import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ExploreItem, ExploreItemRequest } from '../models/explore-item.model';

@Injectable({ providedIn: 'root' })
export class ExploreItemService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/explore-items`;

  findAll(): Observable<ExploreItem[]> {
    return this.http.get<ExploreItem[]>(this.url);
  }

  findById(id: number): Observable<ExploreItem> {
    return this.http.get<ExploreItem>(`${this.url}/${id}`);
  }

  create(item: ExploreItemRequest): Observable<ExploreItem> {
    return this.http.post<ExploreItem>(this.url, item);
  }

  update(id: number, item: ExploreItemRequest): Observable<ExploreItem> {
    return this.http.put<ExploreItem>(`${this.url}/${id}`, item);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
