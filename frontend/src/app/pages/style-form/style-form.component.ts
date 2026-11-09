import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { FieldErrorComponent } from '../../components/field-error/field-error.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { Style, StyleRequest } from '../../models/style.model';
import { StyleService } from '../../services/style.service';
import { errorMessage } from '../../utils/error-message';

const HEX = /^#[0-9A-Fa-f]{6}$/;

/** Crear y editar un estilo con el mismo formulario (ruta /new o /:id). */
@Component({
  selector: 'app-style-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent, LoadStateComponent],
  templateUrl: './style-form.component.html',
})
export class StyleFormComponent implements OnInit {
  private styleService = inject(StyleService);
  private router = inject(Router);

  /** Parámetro de la ruta (withComponentInputBinding). Vacío al crear. */
  id = input<string>();

  cargando = signal(false);
  errorCarga = signal<string | null>(null);
  guardando = signal(false);
  errorGuardar = signal<string | null>(null);

  form = new FormGroup({
    code: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/^[a-z_]{2,40}$/)] }),
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(80)] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(1000)] }),
    palette: new FormArray<FormControl<string>>([]),
    gallery: new FormArray<FormControl<string>>([]),
  });

  get esEdicion(): boolean {
    return !!this.id();
  }

  ngOnInit(): void {
    if (this.esEdicion) {
      this.cargar();
    } else {
      this.agregarColor('#E2725B');
    }
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.styleService.findById(Number(this.id())).subscribe({
      next: (style) => {
        this.llenar(style);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  agregarColor(valor = '#CCCCCC'): void {
    this.form.controls.palette.push(
      new FormControl(valor, { nonNullable: true, validators: [Validators.required, Validators.pattern(HEX)] }));
  }

  agregarFoto(valor = ''): void {
    this.form.controls.gallery.push(
      new FormControl(valor, { nonNullable: true, validators: [Validators.required, Validators.maxLength(500)] }));
  }

  quitar(arreglo: FormArray, index: number): void {
    arreglo.removeAt(index);
  }

  guardar(): void {
    if (this.form.invalid || this.form.controls.palette.length === 0) {
      this.form.markAllAsTouched();
      return;
    }
    const body: StyleRequest = this.form.getRawValue();
    this.guardando.set(true);
    this.errorGuardar.set(null);
    const peticion = this.esEdicion
      ? this.styleService.update(Number(this.id()), body)
      : this.styleService.create(body);
    peticion.subscribe({
      next: () => this.router.navigate(['/admin/styles']),
      error: (err) => {
        this.errorGuardar.set(errorMessage(err));
        this.guardando.set(false);
      },
    });
  }

  private llenar(style: Style): void {
    this.form.patchValue({ code: style.code, name: style.name, description: style.description });
    this.form.controls.palette.clear();
    style.palette.forEach((c) => this.agregarColor(c));
    this.form.controls.gallery.clear();
    style.gallery.forEach((g) => this.agregarFoto(g));
  }
}
