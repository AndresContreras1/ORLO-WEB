import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { ColorSwatchesComponent } from '../../../components/color-swatches/color-swatches.component';
import { FieldErrorComponent } from '../../../components/field-error/field-error.component';
import { OptionChipsComponent } from '../../../components/option-chips/option-chips.component';
import {
  CLIMAS,
  COLORES_DE_CABELLO,
  COLORES_DE_OJOS,
  MAX_NOMBRE,
  OCASIONES,
  PRESUPUESTOS,
  RANGOS_DE_EDAD,
  TIPOS_DE_CUERPO,
  TONOS_DE_PIEL,
  TRATO_PRENDAS,
} from '../../../models/profile-options';
import { PerfilService } from '../../../services/app/perfil.service';
import { SesionService } from '../../../services/app/sesion.service';

/**
 * Datos básicos del perfil. Cada dato explica para qué se pide; lo sensible es opcional y solo
 * aparece con consentimiento. En modo "inicial" sigue al cuestionario; en "editar" vuelve a la cuenta.
 */
@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent, OptionChipsComponent, ColorSwatchesComponent],
  templateUrl: './perfil.component.html',
})
export class PerfilComponent implements OnInit {
  private perfilService = inject(PerfilService);
  private sesion = inject(SesionService);
  private router = inject(Router);

  /** Viene de la ruta (data). */
  modo = input<'inicial' | 'editar'>('inicial');

  readonly opciones = {
    edades: RANGOS_DE_EDAD, trato: TRATO_PRENDAS, cuerpos: TIPOS_DE_CUERPO, pieles: TONOS_DE_PIEL,
    cabellos: COLORES_DE_CABELLO, ojos: COLORES_DE_OJOS, climas: CLIMAS, presupuestos: PRESUPUESTOS, ocasiones: OCASIONES,
  };

  cargando = signal(true);
  guardando = signal(false);
  error = signal<string | null>(null);

  form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(MAX_NOMBRE)] }),
    ageRange: new FormControl('', { nonNullable: true }),
    garmentPronoun: new FormControl('', { nonNullable: true }),
    occasions: new FormControl<string[]>([], { nonNullable: true }),
    climate: new FormControl('', { nonNullable: true }),
    budget: new FormControl('', { nonNullable: true }),
    sensitiveDataConsent: new FormControl(false, { nonNullable: true }),
    bodyType: new FormControl('', { nonNullable: true }),
    skinTone: new FormControl('', { nonNullable: true }),
    hairColor: new FormControl('', { nonNullable: true }),
    eyeColor: new FormControl('', { nonNullable: true }),
  });

  async ngOnInit(): Promise<void> {
    const perfil = await firstValueFrom(this.perfilService.perfil$(this.sesion.uidActual()));
    if (perfil) {
      this.form.patchValue({
        name: perfil.name ?? '',
        ageRange: perfil.ageRange ?? '',
        garmentPronoun: perfil.garmentPronoun ?? '',
        occasions: perfil.occasions ?? [],
        climate: perfil.climate ?? '',
        budget: perfil.budget ?? '',
        sensitiveDataConsent: !!perfil.sensitiveDataConsent,
        bodyType: perfil.bodyType ?? '',
        skinTone: perfil.skinTone ?? '',
        hairColor: perfil.hairColor ?? '',
        eyeColor: perfil.eyeColor ?? '',
      });
    }
    this.cargando.set(false);
  }

  async guardar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    try {
      await this.perfilService.actualizarPerfil(this.sesion.uidActual(), this.form.getRawValue());
      await this.router.navigate([this.modo() === 'inicial' ? '/cuestionario' : '/cuenta']);
    } catch {
      this.error.set('No se pudo guardar. Revisa tu conexión e intenta de nuevo.');
    } finally {
      this.guardando.set(false);
    }
  }
}
