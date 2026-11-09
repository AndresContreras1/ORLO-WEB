import { Injectable, signal } from '@angular/core';

import { QuizScoreResponse } from '../../models/app-api.model';

/** Resultado recién calculado, mientras la persona decide guardarlo en su perfil. */
@Injectable({ providedIn: 'root' })
export class CuestionarioEstadoService {
  readonly pendiente = signal<QuizScoreResponse | null>(null);
}
