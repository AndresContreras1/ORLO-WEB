import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { FieldErrorComponent } from '../../../components/field-error/field-error.component';
import { MAX_NOMBRE, MIN_PASSWORD } from '../../../models/profile-options';
import { PerfilService } from '../../../services/app/perfil.service';
import { SesionService, mensajeAuth } from '../../../services/app/sesion.service';

/** Registro: primero la cuenta de Firebase y con su uid el documento del usuario (como la app). */
@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './registro.component.html',
})
export class RegistroComponent {
  private sesion = inject(SesionService);
  private perfil = inject(PerfilService);
  private router = inject(Router);

  readonly minPassword = MIN_PASSWORD;
  enviando = signal(false);
  error = signal<string | null>(null);
  verPassword = signal(false);

  form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(MAX_NOMBRE)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(MIN_PASSWORD)] }),
    // La autorización de datos va sin marcar: la persona la acepta (Ley 1581)
    terms: new FormControl(false, { nonNullable: true, validators: [Validators.requiredTrue] }),
  });

  async registrar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    try {
      const { name, email, password } = this.form.getRawValue();
      const user = await this.sesion.registrar(email, password);
      await this.perfil.crearUsuario(user.uid, name);
      await this.router.navigate(['/perfil-inicial']);
    } catch (e) {
      this.error.set(mensajeAuth(e));
    } finally {
      this.enviando.set(false);
    }
  }
}
