import { Location } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { UsuarioActualService } from '../../../services/app/usuario-actual.service';

/**
 * Oferta premium: qué incluye, precio, renovación y cómo cancelar, antes de cualquier cobro.
 * El cobro real (tiendas de apps o pasarela) llega en la siguiente versión.
 */
@Component({
  selector: 'app-premium',
  imports: [RouterLink],
  template: `
    <div class="mx-auto text-center" style="max-width: 40rem;">
      <div class="mx-auto mb-3 bg-accent-soft text-accent rounded-circle d-flex align-items-center justify-content-center"
           style="width:4rem;height:4rem"><i class="bi bi-stars fs-2"></i></div>
      <h1 class="h2 fw-bold">ORLO Premium</h1>
      <p class="text-secondary">Aprende, pide asesoría y organiza tu ropa.</p>
      <ul class="list-unstyled text-start card border-0 p-4 d-flex flex-column gap-3 my-4">
        <li><i class="bi bi-check-lg text-success me-2"></i>Todas las lecciones de Aprende, con tu avance</li>
        <li><i class="bi bi-check-lg text-success me-2"></i>Asesor con inteligencia artificial que usa tus prendas</li>
        <li><i class="bi bi-check-lg text-success me-2"></i>Explora: ideas para tu estilo y para completar tu armario</li>
        <li><i class="bi bi-check-lg text-success me-2"></i>Mi armario: prendas, outfits e historial</li>
      </ul>
      <p class="fs-4 fw-bold mb-0">$10.000 COP al mes</p>
      <p class="small text-secondary">Se renueva cada mes. Puedes cancelar cuando quieras desde "Mi cuenta" y tus datos se conservan.</p>
      @if (usuario.premium()) {
        <div class="alert alert-light border">Ya tienes premium activo.</div>
        <a routerLink="/inicio" class="btn btn-primary btn-lg">Ir al inicio</a>
      } @else {
        <div class="d-flex flex-wrap gap-2 justify-content-center">
          <button type="button" class="btn btn-primary btn-lg" disabled title="Muy pronto">Suscribirme (muy pronto)</button>
          <button type="button" class="btn btn-outline-secondary btn-lg" (click)="ahoraNo()">Ahora no</button>
        </div>
        <p class="small text-secondary mt-3">El pago dentro de la app llega en la siguiente versión.</p>
      }
    </div>
  `,
})
export class PremiumComponent {
  usuario = inject(UsuarioActualService);
  private location = inject(Location);

  ahoraNo(): void {
    this.location.back();
  }
}
