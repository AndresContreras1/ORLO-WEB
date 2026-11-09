import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { Store } from '../../models/store.model';
import { StoreService } from '../../services/store.service';
import { errorMessage } from '../../utils/error-message';

@Component({
  selector: 'app-stores',
  imports: [RouterLink, LoadStateComponent, ConfirmDialogComponent],
  templateUrl: './stores.component.html',
})
export class StoresComponent implements OnInit {
  private storeService = inject(StoreService);

  tiendas = signal<Store[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);
  aBorrar = signal<Store | null>(null);
  borrando = signal(false);
  errorAccion = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.storeService.findAll().subscribe({
      next: (data) => {
        this.tiendas.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  confirmarBorrado(): void {
    const tienda = this.aBorrar();
    if (!tienda) {
      return;
    }
    this.borrando.set(true);
    this.storeService.delete(tienda.id).subscribe({
      next: () => {
        this.tiendas.update((lista) => lista.filter((t) => t.id !== tienda.id));
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
