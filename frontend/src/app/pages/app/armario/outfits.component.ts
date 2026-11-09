import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { of, switchMap } from 'rxjs';

import { ConfirmDialogComponent } from '../../../components/confirm-dialog/confirm-dialog.component';
import { OutfitDoc, PrendaDoc } from '../../../models/firestore.model';
import { OCASIONES_OUTFIT, etiqueta } from '../../../models/profile-options';
import { ArmarioService } from '../../../services/app/armario.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';

/** Outfits guardados (a mano o desde el asesor), en tiempo real, con favoritos. */
@Component({
  selector: 'app-outfits',
  imports: [RouterLink, DatePipe, ConfirmDialogComponent],
  templateUrl: './outfits.component.html',
})
export class OutfitsComponent {
  private armario = inject(ArmarioService);
  private sesion = inject(SesionService);
  private usuario = inject(UsuarioActualService);

  // undefined mientras carga
  outfits = toSignal(this.sesion.user$.pipe(switchMap((u) => (u ? this.armario.outfits$(u.uid) : of([] as OutfitDoc[])))));
  soloFavoritos = signal(false);
  aBorrar = signal<OutfitDoc | null>(null);
  borrando = signal(false);

  private prendasPorId = computed(() => new Map(this.usuario.prendas().map((p) => [p.id, p])));
  favoritos = computed(() => (this.outfits() ?? []).filter((o) => o.favorite).length);
  visibles = computed(() => (this.outfits() ?? []).filter((o) => !this.soloFavoritos() || o.favorite));

  /** Prendas del armario que siguen existiendo (la Cloud Function quita las borradas). */
  prendasDe(o: OutfitDoc): PrendaDoc[] {
    return o.garmentIds.map((id) => this.prendasPorId().get(id)).filter((p): p is PrendaDoc => !!p);
  }

  ocasion(codigo: string): string {
    return etiqueta(OCASIONES_OUTFIT, codigo);
  }

  alternarFavorito(o: OutfitDoc): void {
    this.armario.marcarOutfitFavorito(this.sesion.uidActual(), o.id, !o.favorite);
  }

  duplicar(o: OutfitDoc): void {
    const { id: _id, ...resto } = o;
    this.armario.guardarOutfit(this.sesion.uidActual(), {
      ...resto, name: `${o.name} (copia)`.slice(0, 40), favorite: false, createdAt: Date.now(),
    });
  }

  async confirmarBorrado(): Promise<void> {
    const o = this.aBorrar();
    if (!o) {
      return;
    }
    this.borrando.set(true);
    await this.armario.borrarOutfit(this.sesion.uidActual(), o.id);
    this.borrando.set(false);
    this.aBorrar.set(null);
  }
}
