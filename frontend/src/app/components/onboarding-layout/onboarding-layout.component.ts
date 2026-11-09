import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

/** Recorrido inicial (perfil, cuestionario, resultado, oferta): sin menú, una acción por pantalla. */
@Component({
  selector: 'app-onboarding-layout',
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="container-xl py-3">
      <a routerLink="/" class="d-inline-flex align-items-center gap-2 text-decoration-none text-body fw-bold fs-5">
        <img src="logo-simbolo.svg" alt="" width="30" height="30">ORLO
      </a>
    </header>
    <main class="container-xl pb-5">
      <router-outlet />
    </main>
  `,
})
export class OnboardingLayoutComponent {}
