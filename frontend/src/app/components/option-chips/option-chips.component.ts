import { Component, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

import { Opcion } from '../../models/profile-options';

/**
 * Opciones para tocar (en vez de un select). Funciona con formularios reactivos: el valor es el
 * código (una opción) o la lista de códigos (varias).
 */
@Component({
  selector: 'app-option-chips',
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => OptionChipsComponent), multi: true }],
  template: `
    <div class="d-flex flex-wrap gap-2" role="group" [attr.aria-label]="etiqueta()">
      @for (o of opciones(); track o.codigo) {
        <button type="button" class="btn btn-sm chip" [class.chip-on]="estaElegida(o.codigo)"
                [attr.aria-pressed]="estaElegida(o.codigo)" [disabled]="deshabilitado()" (click)="alternar(o.codigo)">
          {{ o.etiqueta }}
        </button>
      }
    </div>
  `,
  styles: [`
    .chip { border: 1px solid var(--bs-border-color); border-radius: 50rem; background: #fff; padding: .35rem .9rem; }
    .chip-on { background: var(--bs-primary); border-color: var(--bs-primary); color: #fff; }
  `],
})
export class OptionChipsComponent implements ControlValueAccessor {
  opciones = input.required<Opcion[]>();
  multiple = input(false);
  etiqueta = input('');

  private valor = signal<string[]>([]);
  deshabilitado = signal(false);
  private onChange: (v: string | string[]) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  estaElegida(codigo: string): boolean {
    return this.valor().includes(codigo);
  }

  alternar(codigo: string): void {
    if (this.multiple()) {
      this.valor.update((v) => (v.includes(codigo) ? v.filter((c) => c !== codigo) : [...v, codigo]));
      this.onChange(this.valor());
    } else {
      this.valor.set([codigo]);
      this.onChange(codigo);
    }
    this.onTouched();
  }

  writeValue(v: string | string[] | null): void {
    this.valor.set(Array.isArray(v) ? [...v] : v ? [v] : []);
  }

  registerOnChange(fn: (v: string | string[]) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.deshabilitado.set(disabled);
  }
}
