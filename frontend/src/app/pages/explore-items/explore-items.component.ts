import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { ExploreItem } from '../../models/explore-item.model';
import { CATEGORIAS_PRENDA, etiqueta } from '../../models/profile-options';
import { ExploreItemService } from '../../services/explore-item.service';
import { errorMessage } from '../../utils/error-message';

/** Catálogo de Explora: prendas de ejemplo que ve la persona según su estilo. */
@Component({
  selector: 'app-explore-items',
  imports: [RouterLink, LoadStateComponent, ConfirmDialogComponent],
  templateUrl: './explore-items.component.html',
})
export class ExploreItemsComponent implements OnInit {
  private service = inject(ExploreItemService);

  prendas = signal<ExploreItem[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);
  aBorrar = signal<ExploreItem | null>(null);
  borrando = signal(false);
  errorAccion = signal<string | null>(null);
  buscar = signal('');

  filtradas = computed(() => {
    const texto = this.buscar().trim().toLowerCase();
    return this.prendas().filter((p) => !texto || p.name.toLowerCase().includes(texto)
      || p.styles.some((s) => s.toLowerCase().includes(texto)));
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.service.findAll().subscribe({
      next: (data) => {
        this.prendas.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  categoria(codigo: string): string {
    return etiqueta(CATEGORIAS_PRENDA, codigo);
  }

  confirmarBorrado(): void {
    const prenda = this.aBorrar();
    if (!prenda) {
      return;
    }
    this.borrando.set(true);
    this.service.delete(prenda.id).subscribe({
      next: () => {
        this.prendas.update((lista) => lista.filter((p) => p.id !== prenda.id));
        this.cerrarDialogo();
      },
      error: (err) => {
        this.errorAccion.set(errorMessage(err));
        this.cerrarDialogo();
      },
    });
  }

  cerrarDialogo(): void {
    this.aBorrar.set(null);
    this.borrando.set(false);
  }
}
