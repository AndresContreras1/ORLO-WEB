import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { of, switchMap } from 'rxjs';

import { LoadStateComponent } from '../../../components/load-state/load-state.component';
import { LessonSummaryDto } from '../../../models/app-api.model';
import { EstadoLeccion } from '../../../models/firestore.model';
import { ContenidoService } from '../../../services/app/contenido.service';
import { PerfilService } from '../../../services/app/perfil.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';
import { errorMessage } from '../../../utils/error-message';

/** Biblioteca de lecciones cortas con su estado y el avance total ("3 de 6"). */
@Component({
  selector: 'app-aprende',
  imports: [RouterLink, LoadStateComponent],
  templateUrl: './aprende.component.html',
})
export class AprendeComponent {
  private contenido = inject(ContenidoService);
  private perfil = inject(PerfilService);
  private sesion = inject(SesionService);
  usuario = inject(UsuarioActualService);

  lecciones = signal<LessonSummaryDto[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);

  /** Avance en tiempo real (si se completa una lección en el celular, se ve aquí). */
  progreso = toSignal(this.sesion.user$.pipe(switchMap((u) => (u ? this.perfil.progreso$(u.uid) : of({} as Record<number, EstadoLeccion>)))),
    { initialValue: {} as Record<number, EstadoLeccion> });
  completadas = computed(() => this.lecciones().filter((l) => this.progreso()[l.id] === 'completada').length);
  porcentaje = computed(() => (this.lecciones().length ? Math.round((this.completadas() / this.lecciones().length) * 100) : 0));

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.contenido.lecciones().subscribe({
      next: (l) => {
        this.lecciones.set(l);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  estado(id: number): EstadoLeccion {
    return this.progreso()[id] ?? 'sin_empezar';
  }

  /** Sin premium solo se abre la muestra gratis; las demás llevan a la oferta. */
  bloqueada(l: LessonSummaryDto): boolean {
    return !l.freeSample && !this.usuario.premium();
  }
}
