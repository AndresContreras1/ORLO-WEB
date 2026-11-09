import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from '@angular/fire/auth';
import { from, switchMap } from 'rxjs';

import { environment } from '../../environments/environment';

/**
 * Token de los usuarios de la web: el ID token de Firebase va a las rutas de la app en nuestra API
 * (todas menos /admin). Firebase lo renueva solo cuando está por vencer.
 */
export const appAuthInterceptor: HttpInterceptorFn = (req, next) => {
  const esApp = req.url.startsWith(environment.apiUrl) && !req.url.startsWith(`${environment.apiUrl}/admin/`);
  const user = inject(Auth).currentUser;
  if (!esApp || !user) {
    return next(req);
  }
  return from(user.getIdToken()).pipe(
    switchMap((token) => next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }))),
  );
};
