import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { PerfilService } from '../services/app/perfil.service';
import { SesionService } from '../services/app/sesion.service';

/** Sin sesión de Firebase no se entra a la app: va a "Entrar". */
export const usuarioGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const user = await inject(SesionService).usuarioActual();
  return user ? true : router.createUrlTree(['/entrar']);
};

/** Con sesión, la bienvenida, el registro y el ingreso llevan al inicio. */
export const invitadoGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const user = await inject(SesionService).usuarioActual();
  return user ? router.createUrlTree(['/inicio']) : true;
};

/**
 * Las secciones de la app piden el recorrido completo, igual que en el celular:
 * registro → datos del perfil → cuestionario. Si falta un paso, se lleva a ese paso.
 */
export const recorridoGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const perfilService = inject(PerfilService);
  const user = await inject(SesionService).usuarioActual();
  if (!user) {
    return router.createUrlTree(['/entrar']);
  }
  const perfil = await firstValueFrom(perfilService.perfil$(user.uid));
  if (!perfil || perfil.occasions === undefined) {
    return router.createUrlTree(['/perfil-inicial']);
  }
  if (!perfil.currentResult?.primary?.code) {
    return router.createUrlTree(['/cuestionario']);
  }
  return true;
};
