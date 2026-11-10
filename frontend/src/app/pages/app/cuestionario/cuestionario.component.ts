import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { LoadStateComponent } from '../../../components/load-state/load-state.component';
import { QuizDto, QuizQuestionDto, StyleDto } from '../../../models/app-api.model';
import { ContenidoService } from '../../../services/app/contenido.service';
import { CuestionarioEstadoService } from '../../../services/app/cuestionario-estado.service';
import { errorMessage } from '../../../utils/error-message';

/**
 * Una pregunta por pantalla, con imágenes, barra de progreso y botón para volver.
 * También se puede saltar: la persona elige su estilo principal y uno secundario de la lista de estilos,
 * y el cuestionario queda para después (Mi armario → Mi estilo).
 */
@Component({
  selector: 'app-cuestionario',
  imports: [LoadStateComponent],
  templateUrl: './cuestionario.component.html',
  styleUrl: './cuestionario.component.scss',
})
export class CuestionarioComponent implements OnInit {
  private contenido = inject(ContenidoService);
  private estado = inject(CuestionarioEstadoService);
  private router = inject(Router);

  quiz = signal<QuizDto | null>(null);
  indice = signal(0);
  /** id de pregunta → opciones elegidas */
  respuestas = signal<Record<number, number[]>>({});
  cargando = signal(true);
  error = signal<string | null>(null);
  calculando = signal(false);

  pregunta = computed<QuizQuestionDto | null>(() => this.quiz()?.questions[this.indice()] ?? null);
  total = computed(() => this.quiz()?.questions.length ?? 0);
  progreso = computed(() => (this.total() ? Math.round(((this.indice() + 1) / this.total()) * 100) : 0));
  elegidas = computed(() => this.respuestas()[this.pregunta()?.id ?? -1] ?? []);
  esUltima = computed(() => this.indice() === this.total() - 1);

  /** 'elegir': se saltó el cuestionario y se escoge el estilo de la lista. */
  modo = signal<'preguntas' | 'elegir'>('preguntas');
  estilos = signal<StyleDto[]>([]);
  cargandoEstilos = signal(false);
  errorEstilos = signal<string | null>(null);
  /** El primero que se toca es el principal; el segundo, el secundario. */
  eleccion = signal<StyleDto[]>([]);
  /** Foto de cada estilo: la primera de su galería que no haya usado otro estilo de la lista. */
  portadas = computed(() => {
    const usadas = new Set<string>();
    const portadas: Record<number, string | undefined> = {};
    for (const estilo of this.estilos()) {
      const foto = estilo.gallery.find((url) => !usadas.has(url)) ?? estilo.gallery[0];
      if (foto) {
        usadas.add(foto);
      }
      portadas[estilo.id] = foto;
    }
    return portadas;
  });
  textoVerEstilo = computed(() => {
    const faltan = 2 - this.eleccion().length;
    if (faltan === 2) {
      return 'Elige 2 estilos';
    }
    return faltan === 1 ? 'Elige 1 más' : 'Ver mi estilo';
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.contenido.cuestionario().subscribe({
      next: (quiz) => {
        this.quiz.set(quiz);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  elegir(optionId: number): void {
    const p = this.pregunta();
    if (!p) {
      return;
    }
    this.respuestas.update((r) => {
      const actuales = r[p.id] ?? [];
      const nuevas = p.multiple
        ? (actuales.includes(optionId) ? actuales.filter((id) => id !== optionId) : [...actuales, optionId])
        : [optionId];
      return { ...r, [p.id]: nuevas };
    });
    // En las de una sola opción se avanza solo, salvo en la última
    if (!p.multiple && !this.esUltima()) {
      setTimeout(() => this.siguiente(), 220);
    }
  }

  anterior(): void {
    this.indice.update((i) => Math.max(0, i - 1));
  }

  siguiente(): void {
    if (this.elegidas().length === 0) {
      return;
    }
    if (this.esUltima()) {
      this.terminar();
    } else {
      this.indice.update((i) => i + 1);
    }
  }

  saltar(): void {
    this.modo.set('elegir');
    if (this.estilos().length === 0) {
      this.cargarEstilos();
    }
  }

  volverAPreguntas(): void {
    this.modo.set('preguntas');
  }

  cargarEstilos(): void {
    this.cargandoEstilos.set(true);
    this.errorEstilos.set(null);
    this.contenido.estilos().subscribe({
      next: (estilos) => {
        this.estilos.set(estilos);
        this.cargandoEstilos.set(false);
      },
      error: (err) => {
        this.errorEstilos.set(errorMessage(err));
        this.cargandoEstilos.set(false);
      },
    });
  }

  /** 0 = principal, 1 = secundario, -1 = sin elegir. */
  posicion(estilo: StyleDto): number {
    return this.eleccion().findIndex((e) => e.id === estilo.id);
  }

  elegirEstilo(estilo: StyleDto): void {
    this.eleccion.update((actual) => {
      if (actual.some((e) => e.id === estilo.id)) {
        return actual.filter((e) => e.id !== estilo.id);
      }
      return actual.length < 2 ? [...actual, estilo] : actual;
    });
  }

  /** Mismo camino que al terminar el cuestionario: el resultado se revisa y se guarda en /resultado. */
  verEstiloElegido(): void {
    const [principal, secundario] = this.eleccion();
    if (!principal || !secundario) {
      return;
    }
    this.estado.pendiente.set({ primary: principal, secondary: secundario, scores: {} });
    this.router.navigate(['/resultado']);
  }

  private terminar(): void {
    const answers = Object.entries(this.respuestas())
      .filter(([, ids]) => ids.length > 0)
      .map(([questionId, optionIds]) => ({ questionId: Number(questionId), optionIds }));
    this.calculando.set(true);
    this.contenido.puntaje(answers).subscribe({
      next: (resultado) => {
        this.estado.pendiente.set(resultado);
        this.router.navigate(['/resultado']);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.calculando.set(false);
      },
    });
  }
}
