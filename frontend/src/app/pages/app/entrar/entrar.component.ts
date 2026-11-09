import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { FieldErrorComponent } from '../../../components/field-error/field-error.component';
import { SesionService, mensajeAuth } from '../../../services/app/sesion.service';

@Component({
  selector: 'app-entrar',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './entrar.component.html',
})
export class EntrarComponent {
  private sesion = inject(SesionService);
  private router = inject(Router);

  enviando = signal(false);
  error = signal<string | null>(null);
  aviso = signal<string | null>(null);
  verPassword = signal(false);

  form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  async entrar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    try {
      const { email, password } = this.form.getRawValue();
      await this.sesion.entrar(email, password);
      // El guard del recorrido lleva al paso que falte (perfil o cuestionario)
      await this.router.navigate(['/inicio']);
    } catch (e) {
      this.error.set(mensajeAuth(e));
    } finally {
      this.enviando.set(false);
    }
  }

  async recuperar(): Promise<void> {
    const email = this.form.controls.email;
    if (email.invalid) {
      email.markAsTouched();
      this.error.set('Escribe tu correo para enviarte el enlace.');
      return;
    }
    try {
      await this.sesion.recuperarPassword(email.value);
      this.error.set(null);
      this.aviso.set('Si el correo tiene cuenta, te llegará un enlace para cambiar la contraseña.');
    } catch (e) {
      this.error.set(mensajeAuth(e));
    }
  }
}
