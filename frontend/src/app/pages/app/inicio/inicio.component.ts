import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { combineLatest, map, of, switchMap } from 'rxjs';

import { OutfitDelDiaDoc } from '../../../models/firestore.model';
import { ArmarioService, fechaDeHoy } from '../../../services/app/armario.service';
import { AsesorService, armarioParaAsesor, perfilParaAsesor } from '../../../services/app/asesor.service';
import { ContenidoService } from '../../../services/app/contenido.service';
import { PerfilService } from '../../../services/app/perfil.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';
import { errorMessage } from '../../../utils/error-message';

/** Inicio: saludo, tu estilo, el outfit del día y acceso a las cuatro secciones. */
@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  templateUrl: './inicio.component.html',
})
export class InicioComponent {
  private sesion = inject(SesionService);
  private armario = inject(ArmarioService);
  private perfilService = inject(PerfilService);
  private asesor = inject(AsesorService);
  private contenido = inject(ContenidoService);
  usuario = inject(UsuarioActualService);

  private hoy = fechaDeHoy();
  private uid$ = this.sesion.user$.pipe(switchMap((u) => of(u?.uid ?? null)));

  /** El outfit del día en tiempo real: si se pidió en el celular, aparece aquí. */
  outfitDelDia = toSignal(this.uid$.pipe(switchMap((uid) => (uid ? this.armario.outfitDelDia$(uid, this.hoy) : of(undefined)))));
  outfits = toSignal(this.uid$.pipe(switchMap((uid) => (uid ? this.armario.outfits$(uid) : of([])))), { initialValue: [] });
  /** Lecciones publicadas (API) combinadas con el avance en vivo (Firestore). */
  avance = toSignal(this.uid$.pipe(switchMap((uid) => uid
    ? combineLatest([this.contenido.lecciones(), this.perfilService.progreso$(uid)]).pipe(
      map(([lecciones, p]) => ({
        total: lecciones.length,
        completadas: lecciones.filter((l) => p[l.id] === 'completada').length,
      })))
    : of({ total: 0, completadas: 0 }))), { initialValue: { total: 0, completadas: 0 } });

  pidiendo = signal(false);
  errorOutfit = signal<string | null>(null);

  favoritos = computed(() => this.outfits().filter((o) => o.favorite).length);
  saludo = computed(() => {
    const h = new Date().getHours();
    return h < 12 ? 'Buenos días' : h < 19 ? 'Buenas tardes' : 'Buenas noches';
  });

  pedirOutfitDelDia(): void {
    this.pidiendo.set(true);
    this.errorOutfit.set(null);
    const uid = this.sesion.uidActual();
    this.asesor.outfitDelDia(perfilParaAsesor(this.usuario.perfil()), armarioParaAsesor(this.usuario.prendas())).subscribe({
      next: async (o) => {
        const doc: OutfitDelDiaDoc = { title: o.title, items: o.items, why: o.why, createdAt: Date.now() };
        await this.armario.guardarOutfitDelDia(uid, this.hoy, doc);
        this.pidiendo.set(false);
      },
      error: (err) => {
        this.errorOutfit.set(err?.error?.code === 'LIMIT_REACHED'
          ? 'Ya pediste los outfits del día de hoy. Mañana hay uno nuevo.'
          : errorMessage(err));
        this.pidiendo.set(false);
      },
    });
  }

  async guardarComoOutfit(o: OutfitDelDiaDoc): Promise<void> {
    await this.armario.guardarOutfit(this.sesion.uidActual(), {
      name: o.title.slice(0, 40), occasion: 'otra', garmentIds: [], itemsText: o.items, why: o.why,
      origin: 'asesor', favorite: false, createdAt: Date.now(),
    });
    this.guardado.set(true);
  }

  guardado = signal(false);
}
