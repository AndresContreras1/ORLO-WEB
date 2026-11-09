import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';

import { FieldErrorComponent } from '../../components/field-error/field-error.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { QuestionRequest, QuizOption, QuizQuestion, ScoreRow } from '../../models/quiz.model';
import { Style } from '../../models/style.model';
import { QuizService } from '../../services/quiz.service';
import { StyleService } from '../../services/style.service';
import { errorMessage } from '../../utils/error-message';

type ScoresGroup = FormGroup<Record<string, FormControl<number>>>;

interface OptionForm {
  text: FormControl<string>;
  imageUrl: FormControl<string>;
  scores: ScoresGroup;
}

/** Pregunta con sus opciones y la tabla de puntos (filas: opciones, columnas: estilos). */
@Component({
  selector: 'app-quiz-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent, LoadStateComponent],
  templateUrl: './quiz-form.component.html',
  styleUrl: './quiz-form.component.scss',
})
export class QuizFormComponent implements OnInit {
  private quizService = inject(QuizService);
  private styleService = inject(StyleService);
  private router = inject(Router);

  id = input<string>();

  estilos = signal<Style[]>([]);
  cargando = signal(true);
  errorCarga = signal<string | null>(null);
  guardando = signal(false);
  errorGuardar = signal<string | null>(null);

  form = new FormGroup({
    text: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(300)] }),
    helpText: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(300)] }),
    multiple: new FormControl(false, { nonNullable: true }),
    order: new FormControl(1, { nonNullable: true, validators: [Validators.required, Validators.min(1)] }),
    active: new FormControl(true, { nonNullable: true }),
    options: new FormArray<FormGroup<OptionForm>>([]),
  });

  get esEdicion(): boolean {
    return !!this.id();
  }

  get opciones(): FormArray<FormGroup<OptionForm>> {
    return this.form.controls.options;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    forkJoin({
      estilos: this.styleService.findAll(),
      pregunta: this.esEdicion ? this.quizService.findById(Number(this.id())) : of(null),
      todas: this.esEdicion ? of([]) : this.quizService.findAll(),
    }).subscribe({
      next: ({ estilos, pregunta, todas }) => {
        this.estilos.set(estilos);
        if (pregunta) {
          this.llenar(pregunta);
        } else {
          this.form.controls.order.setValue(todas.length + 1);
          this.agregarOpcion();
          this.agregarOpcion();
        }
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  agregarOpcion(opcion?: QuizOption): void {
    const scores: ScoresGroup = new FormGroup<Record<string, FormControl<number>>>({});
    for (const estilo of this.estilos()) {
      scores.addControl(String(estilo.id), new FormControl(opcion?.scores[estilo.id] ?? 0, {
        nonNullable: true,
        validators: [Validators.required, Validators.min(0), Validators.max(10)],
      }));
    }
    this.opciones.push(new FormGroup<OptionForm>({
      text: new FormControl(opcion?.text ?? '', { nonNullable: true, validators: [Validators.required, Validators.maxLength(200)] }),
      imageUrl: new FormControl(opcion?.imageUrl ?? '', { nonNullable: true, validators: [Validators.maxLength(500)] }),
      scores,
    }));
  }

  quitarOpcion(index: number): void {
    this.opciones.removeAt(index);
  }

  guardar(): void {
    if (this.form.invalid || this.opciones.length < 2) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const body: QuestionRequest = {
      text: raw.text,
      helpText: raw.helpText.trim() || null,
      multiple: raw.multiple,
      order: Number(raw.order),
      active: raw.active,
      options: raw.options.map((o) => ({
        text: o.text,
        imageUrl: o.imageUrl.trim() || null,
        scores: this.aFilaDePuntos(o.scores),
      })),
    };
    this.guardando.set(true);
    this.errorGuardar.set(null);
    const peticion = this.esEdicion
      ? this.quizService.update(Number(this.id()), body)
      : this.quizService.create(body);
    peticion.subscribe({
      next: () => this.router.navigate(['/admin/quiz']),
      error: (err) => {
        this.errorGuardar.set(errorMessage(err));
        this.guardando.set(false);
      },
    });
  }

  private aFilaDePuntos(scores: Record<string, number>): ScoreRow {
    const fila: ScoreRow = {};
    for (const [styleId, puntos] of Object.entries(scores)) {
      fila[Number(styleId)] = Number(puntos);
    }
    return fila;
  }

  private llenar(pregunta: QuizQuestion): void {
    this.form.patchValue({
      text: pregunta.text,
      helpText: pregunta.helpText ?? '',
      multiple: pregunta.multiple,
      order: pregunta.order,
      active: pregunta.active,
    });
    this.opciones.clear();
    pregunta.options.forEach((o) => this.agregarOpcion(o));
  }
}
