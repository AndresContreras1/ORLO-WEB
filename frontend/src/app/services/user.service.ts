import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { AppUser } from '../models/app-user.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/users`;

  list(page: number, size: number, email: string): Observable<Page<AppUser>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (email.trim()) {
      params = params.set('email', email.trim());
    }
    return this.http.get<Page<AppUser>>(this.url, { params });
  }

  setPremium(uid: string, premium: boolean): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.url}/${encodeURIComponent(uid)}/premium`, { premium });
  }
}
