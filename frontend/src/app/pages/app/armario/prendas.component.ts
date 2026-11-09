import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ConfirmDialogComponent } from '../../../components/confirm-dialog/confirm-dialog.component';
import { PrendaDoc } from '../../../models/firestore.model';
import { CATEGORIAS_PRENDA, COLORES_PRENDA, etiqueta, hexDe } from '../../../models/profile-options';
import { ArmarioService } from '../../../services/app/armario.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';

/** Cuadrícula de prendas en tiempo real, con filtros por categoría, color y favoritas. */
@Component({
  selector: 'app-prendas',
  imports: [RouterLink, ConfirmDialogComponent],
  templateUrl: './prendas.component.html',
})
export class PrendasComponent {
  private armario = inject(ArmarioService);
  private sesion = inject(SesionService);
  usuario = inject(UsuarioActualService);

  readonly categorias = CATEGORIAS_PRENDA;
  readonly colores = COLORES_PRENDA;
  categoria = signal<string | null>(null);
  color = signal<string | null>(null);
  soloFavoritas = signal(false);
  aBorrar = signal<PrendaDoc | null>(null);
  borrando = signal(false);

  filtradas = computed(() => this.usuario.prendas().filter((p) =>
    (!this.categoria() || p.category === this.categoria())
    && (!this.color() || p.color === this.color())
    && (!this.soloFavoritas() || p.favorite)));
  favoritas = computed(() => this.usuario.prendas().filter((p) => p.favorite).length);
  /** Solo los colores que hay en el armario, para no llenar el filtro. */
  coloresUsados = computed(() => this.colores.filter((c) => this.usuario.prendas().some((p) => p.color === c.codigo)));

  nombreCategoria(codigo: string): string {
    return etiqueta(CATEGORIAS_PRENDA, codigo);
  }

  hex(codigo: string): string | null {
    return hexDe(COLORES_PRENDA, codigo);
  }

  alternarFavorita(p: PrendaDoc): void {
    this.armario.marcarPrendaFavorita(this.sesion.uidActual(), p.id, !p.favorite);
  }

  async confirmarBorrado(): Promise<void> {
    const p = this.aBorrar();
    if (!p) {
      return;
    }
    this.borrando.set(true);
    await this.armario.borrarPrenda(this.sesion.uidActual(), p);
    this.borrando.set(false);
    this.aBorrar.set(null);
  }
}
