import { HttpErrorResponse } from '@angular/common/http';

import { Problem } from '../models/problem.model';

/** Traduce un error HTTP a un mensaje en palabras simples. Nunca muestra el error técnico. */
export function errorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Algo salió mal. Intenta de nuevo.';
  }
  if (error.status === 0) {
    // Sin respuesta: sin internet, CORS o el servidor despertando (el plan gratis se duerme tras 15 min)
    return 'No pudimos conectar con el servidor. Revisa tu internet e intenta de nuevo en un momento.';
  }
  const problem = error.error as Problem | null;
  if (problem?.errors && Object.keys(problem.errors).length > 0) {
    return Object.values(problem.errors).join('. ');
  }
  if (problem?.detail && error.status < 500) {
    return problem.detail;
  }
  if (error.status === 403) {
    return 'No tienes permiso para esta acción.';
  }
  return 'Ocurrió un error en el servidor. Intenta de nuevo en un momento.';
}
