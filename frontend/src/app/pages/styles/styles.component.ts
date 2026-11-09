import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { Style } from '../../models/style.model';
import { StyleService } from '../../services/style.service';
import { errorMessage } from '../../utils/error-message';

@Component({
  selector: 'app-styles',
  imports: [RouterLink, LoadStateComponent, ConfirmDialogComponent],
  templateUrl: './styles.component.html',
})
export class StylesComponent implements OnInit {
  private styleService = inject(StyleService);

  estilos = signal<Style[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);
  aBorrar = signal<Style | null>(null);
  borrando = signal(false);
  errorAccion = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.styleService.findAll().subscribe({
      next: (data) => {
        this.estilos.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  confirmarBorrado(): void {
    const estilo = this.aBorrar();
    if (!estilo) {
      return;
    }
    this.borrando.set(true);
    this.styleService.delete(estilo.id).subscribe({
      next: () => {
        // Se quita de la lista cuando el backend confirma
        this.estilos.update((lista) => lista.filter((s) => s.id !== estilo.id));
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
