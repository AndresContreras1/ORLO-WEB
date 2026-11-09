import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Store, StoreRequest } from '../models/store.model';

@Injectable({ providedIn: 'root' })
export class StoreService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/stores`;

  findAll(): Observable<Store[]> {
    return this.http.get<Store[]>(this.url);
  }

  findById(id: number): Observable<Store> {
    return this.http.get<Store>(`${this.url}/${id}`);
  }

  create(store: StoreRequest): Observable<Store> {
    return this.http.post<Store>(this.url, store);
  }

  update(id: number, store: StoreRequest): Observable<Store> {
    return this.http.put<Store>(`${this.url}/${id}`, store);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
