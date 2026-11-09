import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { TokenService } from '../services/token.service';

/** Si la sesión venció (401 fuera del login), se borra el token y se vuelve al login. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.endsWith('/admin/auth/login')) {
        tokenService.borrar();
        router.navigate(['/login'], { queryParams: { expirada: 1 } });
      }
      return throwError(() => error);
    }),
  );
};
