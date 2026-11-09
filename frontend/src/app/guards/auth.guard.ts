import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { TokenService } from '../services/token.service';

/** Sin token vigente no se entra al panel. */
export const authGuard: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const router = inject(Router);
  return tokenService.hayToken() ? true : router.createUrlTree(['/login']);
};
