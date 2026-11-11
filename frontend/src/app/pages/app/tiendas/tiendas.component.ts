import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LoadStateComponent } from '../../../components/load-state/load-state.component';
import { AppStoreDto } from '../../../models/app-api.model';
import { CATEGORIAS_PRENDA, etiqueta } from '../../../models/profile-options';
import { ExploraService } from '../../../services/app/explora.service';
import { errorMessage } from '../../../utils/error-message';

/** Tiendas sugeridas: secundario y sin presión; si hay enlaces de afiliado, se dice. */
@Component({
  selector: 'app-tiendas',
  imports: [RouterLink, LoadStateComponent],
  template: `
    <a routerLink="/explora" class="small text-secondary text-decoration-none d-none d-md-inline"><i class="bi bi-arrow-left me-1"></i>Explora</a>
    <h1 class="h3 fw-bold mt-1 mb-1">Dónde conseguirla</h1>
    <p class="text-secondary">Algunas tiendas{{ categoria() ? ' con ' + nombreCategoria().toLowerCase() : '' }}. Antes de comprar, revisa si ya tienes algo parecido.</p>
    <app-load-state [loading]="cargando()" [error]="error()" (retry)="cargar()" />
    <div class="row g-3">
      @for (t of tiendas(); track t.id) {
        <div class="col-12 col-md-6 col-xl-4">
          <div class="card border-0 h-100">
            <div class="card-body">
              <div class="fw-bold">{{ t.name }}</div>
              <div class="small text-secondary mb-2">{{ t.clothingTypes.join(' · ') }}</div>
              <a [href]="t.url" target="_blank" rel="noopener" class="btn btn-sm btn-outline-secondary">Ir a la tienda <i class="bi bi-box-arrow-up-right ms-1"></i></a>
              @if (t.affiliate) {
                <div class="small text-secondary mt-2">ORLO puede recibir una comisión si compras por este enlace.</div>
              }
            </div>
          </div>
        </div>
      }
    </div>
  `,
})
export class TiendasComponent implements OnInit {
  private explora = inject(ExploraService);
  categoria = input<string>();
  tiendas = signal<AppStoreDto[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);
  nombreCategoria = computed(() => etiqueta(CATEGORIAS_PRENDA, this.categoria()));

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.explora.tiendas(this.categoria()).subscribe({
      next: (t) => { this.tiendas.set(t); this.cargando.set(false); },
      error: (err) => { this.error.set(errorMessage(err)); this.cargando.set(false); },
    });
  }
}
