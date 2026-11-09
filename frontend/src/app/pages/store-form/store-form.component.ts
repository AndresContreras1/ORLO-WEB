import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { FieldErrorComponent } from '../../components/field-error/field-error.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { StoreRequest } from '../../models/store.model';
import { StoreService } from '../../services/store.service';
import { errorMessage } from '../../utils/error-message';

@Component({
  selector: 'app-store-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent, LoadStateComponent],
  templateUrl: './store-form.component.html',
})
export class StoreFormComponent implements OnInit {
  private storeService = inject(StoreService);
  private router = inject(Router);

  id = input<string>();

  cargando = signal(false);
  errorCarga = signal<string | null>(null);
  guardando = signal(false);
  errorGuardar = signal<string | null>(null);

  form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
    garmentTypes: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(200)] }),
    url: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/^https?:\/\/.+/)] }),
    logoUrl: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    active: new FormControl(true, { nonNullable: true }),
    affiliate: new FormControl(false, { nonNullable: true }),
  });

  get esEdicion(): boolean {
    return !!this.id();
  }

  ngOnInit(): void {
    if (this.esEdicion) {
      this.cargar();
    }
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.storeService.findById(Number(this.id())).subscribe({
      next: (store) => {
        this.form.patchValue({ ...store, logoUrl: store.logoUrl ?? '' });
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const body: StoreRequest = { ...raw, logoUrl: raw.logoUrl.trim() || null };
    this.guardando.set(true);
    this.errorGuardar.set(null);
    const peticion = this.esEdicion
      ? this.storeService.update(Number(this.id()), body)
      : this.storeService.create(body);
    peticion.subscribe({
      next: () => this.router.navigate(['/admin/stores']),
      error: (err) => {
        this.errorGuardar.set(errorMessage(err));
        this.guardando.set(false);
      },
    });
  }
}
