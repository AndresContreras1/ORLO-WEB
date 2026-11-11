import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { FieldErrorComponent } from '../../../components/field-error/field-error.component';
import { MAX_NOMBRE, MAX_PASSWORD, MIN_PASSWORD } from '../../../models/profile-options';
import { PerfilService } from '../../../services/app/perfil.service';
import { SesionService, mensajeAuth } from '../../../services/app/sesion.service';
import { normalizarCorreo, sugerirCorreo } from '../../../utils/correo';

/** Validador del formulario: la contraseña y su confirmación deben ser iguales. */
function passwordsIguales(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmacion = group.get('confirmPassword')?.value;
  return confirmacion && password !== confirmacion ? { passwordsDistintas: true } : null;
}

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
  /** Correo corregido cuando el dominio parece mal escrito (gmil.com → gmail.com). */
  sugerencia = signal<string | null>(null);

  form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(MAX_NOMBRE)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email, Validators.maxLength(254)] }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(MIN_PASSWORD), Validators.maxLength(MAX_PASSWORD)],
    }),
    confirmPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    // La autorización de datos va sin marcar: la persona la acepta (Ley 1581)
    terms: new FormControl(false, { nonNullable: true, validators: [Validators.requiredTrue] }),
  }, { validators: passwordsIguales });

  revisarCorreo(): void {
    this.sugerencia.set(sugerirCorreo(this.form.controls.email.value));
  }

  usarSugerencia(): void {
    const correo = this.sugerencia();
    if (correo) {
      this.form.controls.email.setValue(correo);
      this.sugerencia.set(null);
    }
  }

  async registrar(): Promise<void> {
    this.revisarCorreo();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    try {
      const { name, email, password } = this.form.getRawValue();
      const user = await this.sesion.registrar(normalizarCorreo(email), password);
      await this.perfil.crearUsuario(user.uid, name);
      await this.router.navigate(['/perfil-inicial']);
    } catch (e) {
      this.error.set(mensajeAuth(e));
    } finally {
      this.enviando.set(false);
    }
  }
}
