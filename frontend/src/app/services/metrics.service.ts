import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Metrics } from '../models/metrics.model';

@Injectable({ providedIn: 'root' })
export class MetricsService {
  private http = inject(HttpClient);

  current(): Observable<Metrics> {
    return this.http.get<Metrics>(`${environment.apiUrl}/admin/metrics`);
  }
}
