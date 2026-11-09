import { Component, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

import { OpcionColor } from '../../models/profile-options';

/** Paleta para elegir un color tocando el círculo (piel, cabello, ojos, color de una prenda). */
@Component({
  selector: 'app-color-swatches',
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ColorSwatchesComponent), multi: true }],
  template: `
    <div class="d-flex flex-wrap gap-2" role="radiogroup" [attr.aria-label]="etiqueta()">
      @for (o of opciones(); track o.codigo) {
        <button type="button" class="swatch-btn" role="radio" [attr.aria-checked]="valor() === o.codigo"
                [class.on]="valor() === o.codigo" [title]="o.etiqueta" [attr.aria-label]="o.etiqueta" (click)="elegir(o.codigo)">
          <span class="swatch" [style.background]="o.codigo === 'estampado' ? patron : o.hex"></span>
          @if (mostrarEtiqueta()) {
            <span class="small">{{ o.etiqueta }}</span>
          }
        </button>
      }
    </div>
  `,
  styles: [`
    .swatch-btn { display: inline-flex; flex-direction: column; align-items: center; gap: .25rem; border: 0; background: none; padding: .15rem; border-radius: .75rem; min-width: 3.4rem; }
    .swatch { width: 2.2rem; height: 2.2rem; border-radius: 50%; border: 1px solid rgba(0,0,0,.12); box-shadow: 0 0 0 3px #fff; }
    .on .swatch { box-shadow: 0 0 0 3px #fff, 0 0 0 5px var(--bs-primary); }
    .on .small { font-weight: 600; }
  `],
})
export class ColorSwatchesComponent implements ControlValueAccessor {
  opciones = input.required<OpcionColor[]>();
  etiqueta = input('');
  mostrarEtiqueta = input(true);

  readonly patron = 'repeating-linear-gradient(45deg,#E2725B 0 6px,#98C1D9 6px 12px,#E9C46A 12px 18px)';
  valor = signal<string>('');
  private onChange: (v: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  elegir(codigo: string): void {
    this.valor.set(codigo);
    this.onChange(codigo);
    this.onTouched();
  }

  writeValue(v: string | null): void {
    this.valor.set(v ?? '');
  }

  registerOnChange(fn: (v: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }
}
