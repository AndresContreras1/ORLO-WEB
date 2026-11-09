import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { TokenService } from '../services/token.service';

/**
 * 401 del panel (fuera del login): se borra el token y se vuelve al login del panel.
 * 401 de la app: la sesión de Firebase ya no vale, se vuelve a "Entrar".
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && req.url.startsWith(environment.apiUrl)) {
        if (req.url.startsWith(`${environment.apiUrl}/admin/`)) {
          if (!req.url.endsWith('/admin/auth/login')) {
            tokenService.borrar();
            router.navigate(['/login'], { queryParams: { expirada: 1 } });
          }
        } else {
          router.navigate(['/entrar']);
        }
      }
      return throwError(() => error);
    }),
  );
};
