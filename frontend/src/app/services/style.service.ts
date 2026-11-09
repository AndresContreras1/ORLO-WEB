import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Style, StyleRequest } from '../models/style.model';

@Injectable({ providedIn: 'root' })
export class StyleService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/styles`;

  findAll(): Observable<Style[]> {
    return this.http.get<Style[]>(this.url);
  }

  findById(id: number): Observable<Style> {
    return this.http.get<Style>(`${this.url}/${id}`);
  }

  create(style: StyleRequest): Observable<Style> {
    return this.http.post<Style>(this.url, style);
  }

  update(id: number, style: StyleRequest): Observable<Style> {
    return this.http.put<Style>(`${this.url}/${id}`, style);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
