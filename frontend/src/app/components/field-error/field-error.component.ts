import { Component, input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

/**
 * Mensaje de error de un campo. Solo aparece después de tocar el campo
 * y dice qué está mal, en palabras simples.
 */
@Component({
  selector: 'app-field-error',
  template: `
    @if (control() && control()!.touched && control()!.invalid) {
      <div class="invalid-feedback d-block">{{ mensaje() }}</div>
    }
  `,
})
export class FieldErrorComponent {
  control = input<AbstractControl | null>(null);

  mensaje(): string {
    const errors = this.control()?.errors;
    if (!errors) {
      return '';
    }
    if (errors['required']) {
      return 'Este campo es obligatorio.';
    }
    if (errors['email']) {
      return 'Escribe un correo válido.';
    }
    if (errors['minlength']) {
      return `Mínimo ${errors['minlength'].requiredLength} caracteres.`;
    }
    if (errors['maxlength']) {
      return `Máximo ${errors['maxlength'].requiredLength} caracteres.`;
    }
    if (errors['min']) {
      return `El valor mínimo es ${errors['min'].min}.`;
    }
    if (errors['max']) {
      return `El valor máximo es ${errors['max'].max}.`;
    }
    if (errors['pattern']) {
      return 'El formato no es válido.';
    }
    return 'Revisa este campo.';
  }
}
