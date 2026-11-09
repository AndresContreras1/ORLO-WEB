import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  LessonDetailDto,
  LessonSummaryDto,
  Me,
  QuizAnswer,
  QuizDto,
  QuizScoreResponse,
  StyleDto,
} from '../../models/app-api.model';

/** Contenido que sirve la API ORLO: estilos, cuestionario, lecciones y estado premium. */
@Injectable({ providedIn: 'root' })
export class ContenidoService {
  private http = inject(HttpClient);
  private url = environment.apiUrl;

  /** Los estilos casi no cambian: se piden una vez por sesión de la página. */
  private estilosCache$?: Observable<StyleDto[]>;

  me(): Observable<Me> {
    return this.http.get<Me>(`${this.url}/me`);
  }

  /** El backend responde 200 con {"deleted": true}. */
  eliminarCuenta(): Observable<{ deleted: boolean }> {
    return this.http.delete<{ deleted: boolean }>(`${this.url}/me`);
  }

  estilos(): Observable<StyleDto[]> {
    this.estilosCache$ ??= this.http.get<StyleDto[]>(`${this.url}/styles`).pipe(shareReplay(1));
    return this.estilosCache$;
  }

  cuestionario(): Observable<QuizDto> {
    return this.http.get<QuizDto>(`${this.url}/quiz/active`);
  }

  puntaje(answers: QuizAnswer[]): Observable<QuizScoreResponse> {
    return this.http.post<QuizScoreResponse>(`${this.url}/quiz/score`, { answers });
  }

  lecciones(): Observable<LessonSummaryDto[]> {
    return this.http.get<LessonSummaryDto[]>(`${this.url}/lessons`);
  }

  leccion(id: number): Observable<LessonDetailDto> {
    return this.http.get<LessonDetailDto>(`${this.url}/lessons/${id}`);
  }
}
