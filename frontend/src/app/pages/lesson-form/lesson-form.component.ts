import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { FieldErrorComponent } from '../../components/field-error/field-error.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { Lesson, LessonExample, LessonRequest } from '../../models/lesson.model';
import { LessonService } from '../../services/lesson.service';
import { errorMessage } from '../../utils/error-message';

interface ExampleForm {
  imageUrl: FormControl<string>;
  text: FormControl<string>;
}

@Component({
  selector: 'app-lesson-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent, LoadStateComponent],
  templateUrl: './lesson-form.component.html',
})
export class LessonFormComponent implements OnInit {
  private lessonService = inject(LessonService);
  private router = inject(Router);

  id = input<string>();

  cargando = signal(false);
  errorCarga = signal<string | null>(null);
  guardando = signal(false);
  errorGuardar = signal<string | null>(null);

  form = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(120)] }),
    summary: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(300)] }),
    mainIdea: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(2000)] }),
    tip: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(600)] }),
    order: new FormControl(1, { nonNullable: true, validators: [Validators.required, Validators.min(1)] }),
    minutes: new FormControl(4, { nonNullable: true, validators: [Validators.required, Validators.min(1), Validators.max(15)] }),
    coverUrl: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    freeSample: new FormControl(false, { nonNullable: true }),
    usesColorProfile: new FormControl(false, { nonNullable: true }),
    published: new FormControl(true, { nonNullable: true }),
    reviewQuestion: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(300)] }),
    reviewOptions: new FormArray<FormControl<string>>([]),
    reviewCorrectIndex: new FormControl(0, { nonNullable: true }),
    examples: new FormArray<FormGroup<ExampleForm>>([]),
  });

  get esEdicion(): boolean {
    return !!this.id();
  }

  get ejemplos(): FormArray<FormGroup<ExampleForm>> {
    return this.form.controls.examples;
  }

  get opcionesRepaso(): FormArray<FormControl<string>> {
    return this.form.controls.reviewOptions;
  }

  ngOnInit(): void {
    if (this.esEdicion) {
      this.cargar();
    } else {
      this.agregarEjemplo();
    }
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.lessonService.findById(Number(this.id())).subscribe({
      next: (lesson) => {
        this.llenar(lesson);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  agregarEjemplo(ejemplo?: LessonExample): void {
    this.ejemplos.push(new FormGroup<ExampleForm>({
      imageUrl: new FormControl(ejemplo?.imageUrl ?? '', { nonNullable: true, validators: [Validators.required, Validators.maxLength(500)] }),
      text: new FormControl(ejemplo?.text ?? '', { nonNullable: true, validators: [Validators.required, Validators.maxLength(500)] }),
    }));
  }

  agregarOpcionRepaso(valor = ''): void {
    this.opcionesRepaso.push(new FormControl(valor, { nonNullable: true, validators: [Validators.required, Validators.maxLength(200)] }));
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const conRepaso = raw.reviewQuestion.trim().length > 0;
    if (conRepaso && raw.reviewOptions.length < 2) {
      this.errorGuardar.set('La pregunta de repaso necesita al menos 2 opciones.');
      return;
    }
    const body: LessonRequest = {
      ...raw,
      order: Number(raw.order),
      minutes: Number(raw.minutes),
      coverUrl: raw.coverUrl.trim() || null,
      reviewQuestion: conRepaso ? raw.reviewQuestion : null,
      reviewOptions: conRepaso ? raw.reviewOptions : [],
      reviewCorrectIndex: conRepaso ? Number(raw.reviewCorrectIndex) : null,
    };
    this.guardando.set(true);
    this.errorGuardar.set(null);
    const peticion = this.esEdicion
      ? this.lessonService.update(Number(this.id()), body)
      : this.lessonService.create(body);
    peticion.subscribe({
      next: () => this.router.navigate(['/admin/lessons']),
      error: (err) => {
        this.errorGuardar.set(errorMessage(err));
        this.guardando.set(false);
      },
    });
  }

  private llenar(lesson: Lesson): void {
    this.form.patchValue({
      title: lesson.title,
      summary: lesson.summary,
      mainIdea: lesson.mainIdea,
      tip: lesson.tip,
      order: lesson.order,
      minutes: lesson.minutes,
      coverUrl: lesson.coverUrl ?? '',
      freeSample: lesson.freeSample,
      usesColorProfile: lesson.usesColorProfile,
      published: lesson.published,
      reviewQuestion: lesson.reviewQuestion ?? '',
      reviewCorrectIndex: lesson.reviewCorrectIndex ?? 0,
    });
    this.ejemplos.clear();
    lesson.examples.forEach((e) => this.agregarEjemplo(e));
    this.opcionesRepaso.clear();
    lesson.reviewOptions.forEach((o) => this.agregarOpcionRepaso(o));
  }
}
