import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { of, switchMap } from 'rxjs';

import { LoadStateComponent } from '../../../components/load-state/load-state.component';
import { PremiumGateComponent } from '../../../components/premium-gate/premium-gate.component';
import { ExploreItemDto, ExploreSection, WardrobeSuggestionDto } from '../../../models/app-api.model';
import { CATEGORIAS_PRENDA, etiqueta } from '../../../models/profile-options';
import { ArmarioService } from '../../../services/app/armario.service';
import { ExploraService } from '../../../services/app/explora.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';
import { errorMessage } from '../../../utils/error-message';

type Pestana = 'estilo' | 'completa' | 'tendencias' | 'complementos' | 'guardados';

const SECCION: Partial<Record<Pestana, ExploreSection>> = {
  estilo: 'FOR_YOUR_STYLE',
  tendencias: 'TRENDS',
  complementos: 'ACCESSORIES',
};

/** Inspiración conectada al perfil. Enseña a aprovechar lo que ya se tiene; comprar es opcional. */
@Component({
  selector: 'app-explora',
  imports: [RouterLink, NgTemplateOutlet, LoadStateComponent, PremiumGateComponent],
  templateUrl: './explora.component.html',
})
export class ExploraComponent {
  private explora = inject(ExploraService);
  private armario = inject(ArmarioService);
  private sesion = inject(SesionService);
  private router = inject(Router);
  usuario = inject(UsuarioActualService);

  /** Pestaña en la URL (?pestana=), para volver a la misma desde el detalle. */
  // Sin ?pestana= el router deja el input en undefined: por eso el valor por defecto va en el computed
  pestanaUrl = input<Pestana | undefined>(undefined, { alias: 'pestana' });
  pestana = computed<Pestana>(() => this.pestanaUrl() ?? 'estilo');

  readonly pestanas: { id: Pestana; label: string }[] = [
    { id: 'estilo', label: 'Para tu estilo' },
    { id: 'completa', label: 'Completa tu armario' },
    { id: 'tendencias', label: 'Tendencias' },
    { id: 'complementos', label: 'Complementos' },
    { id: 'guardados', label: 'Guardados' },
  ];

  items = signal<ExploreItemDto[]>([]);
  sugerencias = signal<WardrobeSuggestionDto[]>([]);
  cargando = signal(false);
  error = signal<string | null>(null);

  guardados = toSignal(this.sesion.user$.pipe(switchMap((u) => (u ? this.armario.guardados$(u.uid) : of([])))), { initialValue: [] });
  idsGuardados = computed(() => new Set(this.guardados().map((g) => g.itemId)));

  constructor() {
    // Se vuelve a cargar solo si cambia la pestaña o llega el estilo del perfil
    effect(() => {
      const p = this.pestana();
      this.usuario.resultado();
      untracked(() => this.cargar(p));
    });
  }

  ir(p: Pestana): void {
    this.router.navigate([], { queryParams: { pestana: p }, replaceUrl: true });
  }

  cargar(p: Pestana = this.pestana()): void {
    if (p === 'guardados') {
      return;
    }
    this.cargando.set(true);
    this.error.set(null);
    const estilo = this.usuario.resultado()?.primary?.code;
    if (p === 'completa') {
      const armario = this.usuario.prendas().map((x) => ({ category: x.category, color: x.color }));
      this.explora.completarArmario(estilo, armario).subscribe({
        next: (s) => { this.sugerencias.set(s); this.cargando.set(false); },
        error: (err) => { this.error.set(errorMessage(err)); this.cargando.set(false); },
      });
      return;
    }
    this.explora.items(SECCION[p]!, estilo).subscribe({
      next: (i) => { this.items.set(i); this.cargando.set(false); },
      error: (err) => { this.error.set(errorMessage(err)); this.cargando.set(false); },
    });
  }

  async alternarGuardado(item: ExploreItemDto, event: Event): Promise<void> {
    event.preventDefault();
    event.stopPropagation();
    const uid = this.sesion.uidActual();
    if (this.idsGuardados().has(item.id)) {
      await this.armario.quitarGuardado(uid, item.id);
    } else {
      await this.armario.guardar(uid, item);
    }
  }

  categoria(codigo: string): string {
    return etiqueta(CATEGORIAS_PRENDA, codigo);
  }
}
