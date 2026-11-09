import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { of, switchMap } from 'rxjs';

import { GuardadoDoc } from '../../../models/firestore.model';
import { CATEGORIAS_PRENDA, COLORES_PRENDA, etiqueta } from '../../../models/profile-options';
import { ArmarioService } from '../../../services/app/armario.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';

/** Mi estilo: el resultado actual, su paleta, cómo está el armario y lo guardado en Explora. */
@Component({
  selector: 'app-mi-estilo',
  imports: [RouterLink],
  templateUrl: './mi-estilo.component.html',
})
export class MiEstiloComponent {
  private armario = inject(ArmarioService);
  private sesion = inject(SesionService);
  usuario = inject(UsuarioActualService);

  guardados = toSignal(this.sesion.user$.pipe(switchMap((u) => (u ? this.armario.guardados$(u.uid) : of([])))),
    { initialValue: [] as GuardadoDoc[] });

  /** Cuántas prendas hay por categoría y por color, para ver de un vistazo qué falta. */
  porCategoria = computed(() => CATEGORIAS_PRENDA
    .map((c) => ({ etiqueta: c.etiqueta, total: this.usuario.prendas().filter((p) => p.category === c.codigo).length })));
  coloresTop = computed(() => COLORES_PRENDA
    .map((c) => ({ ...c, total: this.usuario.prendas().filter((p) => p.color === c.codigo).length }))
    .filter((c) => c.total > 0)
    .sort((a, b) => b.total - a.total)
    .slice(0, 6));

  nombreCategoria(codigo: string): string {
    return etiqueta(CATEGORIAS_PRENDA, codigo);
  }

  quitar(g: GuardadoDoc): void {
    this.armario.quitarGuardado(this.sesion.uidActual(), g.itemId);
  }
}
