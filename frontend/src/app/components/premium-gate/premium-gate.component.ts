import { Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { UsuarioActualService } from '../../services/app/usuario-actual.service';

/**
 * Bloqueo premium: si la persona no es premium, en lugar del contenido muestra qué incluye y el
 * precio, con "Suscribirse" y "Ahora no". Nunca un error ni la función escondida.
 */
@Component({
  selector: 'app-premium-gate',
  imports: [RouterLink],
  template: `
    @if (usuario.premium()) {
      <ng-content />
    } @else {
      <div class="card border-0 text-center p-4 p-lg-5 mx-auto" style="max-width: 34rem;" id="premiumGate">
        <div class="mx-auto mb-3 bg-accent-soft text-accent rounded-circle d-flex align-items-center justify-content-center"
             style="width:3.5rem;height:3.5rem"><i class="bi bi-stars fs-3"></i></div>
        <h2 class="h4 fw-bold">{{ funcion() }} es parte de ORLO Premium</h2>
        <p class="text-secondary">Con premium aprendes con todas las lecciones, le preguntas al asesor, exploras ideas y organizas tu armario.</p>
        <div class="d-flex flex-wrap gap-2 justify-content-center">
          <a routerLink="/premium" class="btn btn-primary">Ver qué incluye</a>
          <a routerLink="/inicio" class="btn btn-outline-secondary">Ahora no</a>
        </div>
      </div>
    }
  `,
})
export class PremiumGateComponent {
  usuario = inject(UsuarioActualService);
  funcion = input('Esta función');
}
