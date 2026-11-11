import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { PremiumGateComponent } from '../../../components/premium-gate/premium-gate.component';

/** Mi armario: prendas, outfits, mi estilo e historial en un solo lugar. */
@Component({
  selector: 'app-armario-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, PremiumGateComponent],
  template: `
    <app-premium-gate funcion="Mi armario">
      <div class="armario-cabecera d-flex flex-wrap align-items-end justify-content-between gap-3 mb-3">
        <div>
          <h1 class="h2 fw-bold mb-1">Mi armario</h1>
          <p class="text-secondary mb-0">Tus prendas, tus outfits y tu estilo. Es privado: solo tú lo ves.</p>
        </div>
      </div>
      <ul class="armario-cabecera nav nav-pills gap-2 mb-4 flex-nowrap overflow-auto pb-1">
        <li class="nav-item"><a class="nav-link text-nowrap" routerLink="prendas" routerLinkActive="active">Prendas</a></li>
        <li class="nav-item"><a class="nav-link text-nowrap" routerLink="outfits" routerLinkActive="active">Outfits</a></li>
        <li class="nav-item"><a class="nav-link text-nowrap" routerLink="estilo" routerLinkActive="active">Mi estilo</a></li>
        <li class="nav-item"><a class="nav-link text-nowrap" routerLink="historial" routerLinkActive="active">Historial</a></li>
      </ul>
      <router-outlet />
    </app-premium-gate>
  `,
})
export class ArmarioLayoutComponent {}
