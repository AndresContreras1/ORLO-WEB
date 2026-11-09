import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { LoadStateComponent } from '../../../components/load-state/load-state.component';
import { QuizDto, QuizQuestionDto } from '../../../models/app-api.model';
import { ContenidoService } from '../../../services/app/contenido.service';
import { CuestionarioEstadoService } from '../../../services/app/cuestionario-estado.service';
import { errorMessage } from '../../../utils/error-message';

/** Una pregunta por pantalla, con imágenes, barra de progreso y botón para volver. */
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
