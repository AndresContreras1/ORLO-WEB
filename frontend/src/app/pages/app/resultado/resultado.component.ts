import { LowerCasePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { StyleDto } from '../../../models/app-api.model';
import { CuestionarioEstadoService } from '../../../services/app/cuestionario-estado.service';
import { PerfilService } from '../../../services/app/perfil.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';

/**
 * Resultado del diagnóstico: estilo principal · secundario, descripción, paleta y galería.
 * Si se llega sin un resultado nuevo, muestra el que ya está guardado.
 */
@Component({
  selector: 'app-resultado',
  imports: [RouterLink, LowerCasePipe],
  templateUrl: './resultado.component.html',
})
export class ResultadoComponent {
  private estado = inject(CuestionarioEstadoService);
  private perfil = inject(PerfilService);
  private sesion = inject(SesionService);
  private router = inject(Router);
  private usuario = inject(UsuarioActualService);

  guardando = signal(false);
  error = signal<string | null>(null);

  nuevo = this.estado.pendiente;
  principal = computed<StyleDto | null>(() => this.nuevo()?.primary ?? this.usuario.resultado()?.primary ?? null);
  secundario = computed<StyleDto | null>(() => this.nuevo()?.secondary ?? this.usuario.resultado()?.secondary ?? null);
  /** Paleta sugerida: la del estilo principal y dos colores del secundario. */
  paleta = computed(() => [...(this.principal()?.palette ?? []), ...(this.secundario()?.palette ?? []).slice(0, 2)]);

  async guardar(): Promise<void> {
    const r = this.nuevo();
    if (!r) {
      this.router.navigate(['/inicio']);
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    try {
      await this.perfil.guardarResultado(this.sesion.uidActual(), r.primary, r.secondary);
      this.estado.pendiente.set(null);
      await this.router.navigate(['/inicio']);
    } catch {
      this.error.set('No se pudo guardar. Revisa tu conexión e intenta de nuevo.');
      this.guardando.set(false);
    }
  }
}
