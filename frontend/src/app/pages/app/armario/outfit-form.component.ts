import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { FieldErrorComponent } from '../../../components/field-error/field-error.component';
import { OptionChipsComponent } from '../../../components/option-chips/option-chips.component';
import { OutfitDoc } from '../../../models/firestore.model';
import { CATEGORIAS_PRENDA, MAX_NOMBRE, OCASIONES_OUTFIT } from '../../../models/profile-options';
import { ArmarioService } from '../../../services/app/armario.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';

/** Crear o editar un outfit eligiendo prendas del armario. */
@Component({
  selector: 'app-outfit-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent, OptionChipsComponent],
  templateUrl: './outfit-form.component.html',
})
export class OutfitFormComponent implements OnInit {
  private armario = inject(ArmarioService);
  private sesion = inject(SesionService);
  private router = inject(Router);
  usuario = inject(UsuarioActualService);

  id = input<string>();

  readonly ocasiones = OCASIONES_OUTFIT;
  private outfit: OutfitDoc | null = null;
  seleccion = signal<string[]>([]);
  guardando = signal(false);
  error = signal<string | null>(null);

  /** Prendas agrupadas por categoría para elegir más fácil. */
  grupos = computed(() => CATEGORIAS_PRENDA
    .map((c) => ({ categoria: c.etiqueta, prendas: this.usuario.prendas().filter((p) => p.category === c.codigo) }))
    .filter((g) => g.prendas.length));

  form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(MAX_NOMBRE)] }),
    occasion: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    why: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(300)] }),
  });

  get esEdicion(): boolean {
    return !!this.id();
  }

  async ngOnInit(): Promise<void> {
    if (this.esEdicion) {
      this.outfit = (await firstValueFrom(this.armario.outfit$(this.sesion.uidActual(), this.id()!))) ?? null;
      if (this.outfit) {
        this.form.patchValue({ name: this.outfit.name, occasion: this.outfit.occasion, why: this.outfit.why });
        this.seleccion.set([...this.outfit.garmentIds]);
      }
    }
  }

  alternar(id: string): void {
    this.seleccion.update((s) => (s.includes(id) ? s.filter((x) => x !== id) : [...s, id]));
  }

  async guardar(): Promise<void> {
    if (this.form.invalid || this.seleccion().length < 2) {
      this.form.markAllAsTouched();
      if (this.seleccion().length < 2) {
        this.error.set('Elige al menos 2 prendas.');
      }
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    const { name, occasion, why } = this.form.getRawValue();
    try {
      await this.armario.guardarOutfit(this.sesion.uidActual(), {
        id: this.outfit?.id,
        name, occasion, why: why.trim(),
        garmentIds: this.seleccion(),
        itemsText: this.outfit?.itemsText ?? [],
        origin: this.outfit?.origin ?? 'manual',
        favorite: this.outfit?.favorite ?? false,
        createdAt: this.outfit?.createdAt ?? Date.now(),
      });
      await this.router.navigate(['/armario/outfits']);
    } catch {
      this.error.set('No se pudo guardar el outfit. Intenta de nuevo.');
      this.guardando.set(false);
    }
  }
}
