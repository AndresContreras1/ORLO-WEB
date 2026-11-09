import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { environment } from '../../environments/environment';
import { TokenService } from '../services/token.service';

/** Token del panel: solo a las rutas /admin de nuestra API (nunca a dominios ajenos). */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(TokenService).obtener();
  if (!token || !req.url.startsWith(`${environment.apiUrl}/admin/`)) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
