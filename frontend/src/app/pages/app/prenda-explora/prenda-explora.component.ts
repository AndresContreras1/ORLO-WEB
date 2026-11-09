import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { of, switchMap } from 'rxjs';

import { LoadStateComponent } from '../../../components/load-state/load-state.component';
import { PremiumGateComponent } from '../../../components/premium-gate/premium-gate.component';
import { ExploreItemDetailDto } from '../../../models/app-api.model';
import { CATEGORIAS_PRENDA, COLORES_PRENDA, CONTEXTOS_COMBINAR, etiqueta } from '../../../models/profile-options';
import { ArmarioService } from '../../../services/app/armario.service';
import { ExploraService } from '../../../services/app/explora.service';
import { SesionService } from '../../../services/app/sesion.service';
import { errorMessage } from '../../../utils/error-message';

/** Detalle de una prenda de Explora: por qué encaja, "Ya la tengo" (cómo combinarla) y "Dónde conseguirla". */
@Component({
  selector: 'app-prenda-explora',
  imports: [RouterLink, LoadStateComponent, PremiumGateComponent],
  templateUrl: './prenda-explora.component.html',
})
export class PrendaExploraComponent implements OnInit {
  private explora = inject(ExploraService);
  private armario = inject(ArmarioService);
  private sesion = inject(SesionService);

  id = input.required<string>();
  // Sin ?pestana= el router deja el input en undefined
  pestanaUrl = input<string | undefined>(undefined, { alias: 'pestana' });
  pestana = computed(() => this.pestanaUrl() ?? 'estilo');

  item = signal<ExploreItemDetailDto | null>(null);
  cargando = signal(true);
  error = signal<string | null>(null);
  yaLaTengo = signal(false);
  agregada = signal(false);
  agregando = signal(false);

  readonly contextos = CONTEXTOS_COMBINAR;
  guardados = toSignal(this.sesion.user$.pipe(switchMap((u) => (u ? this.armario.guardados$(u.uid) : of([])))), { initialValue: [] });
  guardada = computed(() => this.guardados().some((g) => g.itemId === Number(this.id())));
  categoria = computed(() => etiqueta(CATEGORIAS_PRENDA, this.item()?.category));
  color = computed(() => etiqueta(COLORES_PRENDA, this.item()?.color));

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.explora.item(Number(this.id())).subscribe({
      next: (i) => { this.item.set(i); this.cargando.set(false); },
      error: (err) => { this.error.set(errorMessage(err)); this.cargando.set(false); },
    });
  }

  async alternarGuardado(): Promise<void> {
    const i = this.item();
    if (!i) {
      return;
    }
    const uid = this.sesion.uidActual();
    if (this.guardada()) {
      await this.armario.quitarGuardado(uid, i.id);
    } else {
      await this.armario.guardar(uid, i);
    }
  }

  /** "Ya la tengo": se agrega a Mi armario con la foto de referencia (se puede cambiar por la propia). */
  async agregarAlArmario(): Promise<void> {
    const i = this.item();
    if (!i) {
      return;
    }
    this.agregando.set(true);
    const uid = this.sesion.uidActual();
    await this.armario.guardarPrenda(uid, {
      id: this.armario.nuevoIdDePrenda(uid), name: i.name.slice(0, 40), category: i.category, color: i.color,
      photoUrl: i.imageUrl, favorite: false, createdAt: Date.now(),
    });
    this.agregando.set(false);
    this.agregada.set(true);
  }
}
