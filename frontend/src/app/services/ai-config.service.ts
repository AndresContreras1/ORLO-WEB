import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { AiConfig } from '../models/ai-config.model';

@Injectable({ providedIn: 'root' })
export class AiConfigService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/ai-config`;

  get(): Observable<AiConfig> {
    return this.http.get<AiConfig>(this.url);
  }

  update(config: AiConfig): Observable<AiConfig> {
    return this.http.put<AiConfig>(this.url, config);
  }
}
