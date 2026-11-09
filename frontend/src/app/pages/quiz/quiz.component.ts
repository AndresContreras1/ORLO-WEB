import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { QuizQuestion } from '../../models/quiz.model';
import { Style } from '../../models/style.model';
import { QuizService } from '../../services/quiz.service';
import { StyleService } from '../../services/style.service';
import { errorMessage } from '../../utils/error-message';

@Component({
  selector: 'app-quiz',
  imports: [RouterLink, LoadStateComponent, ConfirmDialogComponent],
  templateUrl: './quiz.component.html',
})
export class QuizComponent implements OnInit {
  private quizService = inject(QuizService);
  private styleService = inject(StyleService);

  preguntas = signal<QuizQuestion[]>([]);
  estilos = signal<Style[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);
  aBorrar = signal<QuizQuestion | null>(null);
  borrando = signal(false);
  errorAccion = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  /** Preguntas y estilos son independientes: se piden en paralelo. */
  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    forkJoin({ preguntas: this.quizService.findAll(), estilos: this.styleService.findAll() }).subscribe({
      next: ({ preguntas, estilos }) => {
        this.preguntas.set(preguntas);
        this.estilos.set(estilos);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  /** Resumen de la fila de puntos de una opción: "Urbano 3 · Casual 1". */
  resumenPuntos(scores: Record<number, number>): string {
    return this.estilos()
      .filter((s) => (scores[s.id] ?? 0) > 0)
      .map((s) => `${s.name} ${scores[s.id]}`)
      .join(' · ') || 'Sin puntos';
  }

  confirmarBorrado(): void {
    const pregunta = this.aBorrar();
    if (!pregunta) {
      return;
    }
    this.borrando.set(true);
    this.quizService.delete(pregunta.id).subscribe({
      next: () => {
        this.preguntas.update((lista) => lista.filter((q) => q.id !== pregunta.id));
        this.cerrarDialogo();
      },
      error: (err) => {
        this.errorAccion.set(errorMessage(err));
        this.cerrarDialogo();
      },
    });
  }

  cerrarDialogo(): void {
    this.aBorrar.set(null);
    this.borrando.set(false);
  }
}
