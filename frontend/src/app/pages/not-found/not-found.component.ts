import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <main class="min-vh-100 d-flex flex-column align-items-center justify-content-center text-center p-3">
      <p class="display-6 fw-bold mb-2">Esta página no existe</p>
      <p class="text-secondary">Revisa el enlace o vuelve al inicio.</p>
      <a routerLink="/" class="btn btn-primary">Ir al inicio</a>
    </main>
  `,
})
export class NotFoundComponent {}
