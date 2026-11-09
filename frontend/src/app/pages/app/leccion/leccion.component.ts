import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { of, switchMap } from 'rxjs';

import { LoadStateComponent } from '../../../components/load-state/load-state.component';
import { LessonDetailDto } from '../../../models/app-api.model';
import { EstadoLeccion } from '../../../models/firestore.model';
import { COLORES_DE_CABELLO, COLORES_DE_OJOS, TONOS_DE_PIEL, etiqueta, hexDe } from '../../../models/profile-options';
import { ContenidoService } from '../../../services/app/contenido.service';
import { PerfilService } from '../../../services/app/perfil.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';
import { errorMessage } from '../../../utils/error-message';

const TONOS_CALIDOS = ['#C8553D', '#D9A066', '#6B8E23', '#B5651D', '#E9C46A'];
const TONOS_FRIOS = ['#3D5A80', '#7B5EA7', '#5E8B7E', '#9E9E9E', '#C0396B'];

/**
 * Una lección: idea principal, ejemplos con imagen, consejo para hoy y repaso. Al abrirla queda
 * "en curso"; con el botón, "completada". La de colorimetría usa los colores del perfil.
 */
@Component({
  selector: 'app-leccion',
  imports: [RouterLink, LoadStateComponent],
  templateUrl: './leccion.component.html',
})
export class LeccionComponent implements OnInit {
  private contenido = inject(ContenidoService);
  private perfilService = inject(PerfilService);
  private sesion = inject(SesionService);
  private router = inject(Router);
  usuario = inject(UsuarioActualService);

  id = input.required<string>();

  leccion = signal<LessonDetailDto | null>(null);
  cargando = signal(true);
  error = signal<string | null>(null);
  respuesta = signal<number | null>(null);
  marcando = signal(false);

  private progreso = toSignal(this.sesion.user$.pipe(switchMap((u) => (u ? this.perfilService.progreso$(u.uid) : of({} as Record<number, EstadoLeccion>)))),
    { initialValue: {} as Record<number, EstadoLeccion> });
  estado = computed<EstadoLeccion>(() => this.progreso()[Number(this.id())] ?? 'sin_empezar');

  /** Colores de la persona para la lección de colorimetría (solo con consentimiento). */
  colores = computed(() => {
    const p = this.usuario.perfil();
    if (!p?.sensitiveDataConsent || !p.skinTone) {
      return null;
    }
    const pielesCalidas = ['piel_media', 'piel_morena', 'piel_oscura', 'piel_muy_oscura'];
    const calida = pielesCalidas.includes(p.skinTone) || p.hairColor === 'cabello_rojizo' || p.eyeColor === 'ojos_miel';
    return {
      piel: { hex: hexDe(TONOS_DE_PIEL, p.skinTone), nombre: etiqueta(TONOS_DE_PIEL, p.skinTone) },
      cabello: { hex: hexDe(COLORES_DE_CABELLO, p.hairColor), nombre: etiqueta(COLORES_DE_CABELLO, p.hairColor) },
      ojos: { hex: hexDe(COLORES_DE_OJOS, p.eyeColor), nombre: etiqueta(COLORES_DE_OJOS, p.eyeColor) },
      sugeridos: calida ? TONOS_CALIDOS : TONOS_FRIOS,
      tipo: calida ? 'cálidos' : 'fríos',
    };
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.contenido.leccion(Number(this.id())).subscribe({
      next: (l) => {
        this.leccion.set(l);
        this.cargando.set(false);
        if (this.estado() === 'sin_empezar') {
          this.perfilService.marcarProgreso(this.sesion.uidActual(), l.id, 'en_curso');
        }
      },
      error: (err) => {
        if (err?.error?.code === 'PREMIUM_REQUIRED') {
          this.router.navigate(['/premium']);
          return;
        }
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  async completar(): Promise<void> {
    const l = this.leccion();
    if (!l) {
      return;
    }
    this.marcando.set(true);
    await this.perfilService.marcarProgreso(this.sesion.uidActual(), l.id, 'completada');
    this.marcando.set(false);
  }
}
