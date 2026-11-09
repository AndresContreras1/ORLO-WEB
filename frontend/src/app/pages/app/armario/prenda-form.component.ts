import { Component, OnDestroy, OnInit, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { CamaraComponent } from '../../../components/camara/camara.component';
import { ColorSwatchesComponent } from '../../../components/color-swatches/color-swatches.component';
import { FieldErrorComponent } from '../../../components/field-error/field-error.component';
import { OptionChipsComponent } from '../../../components/option-chips/option-chips.component';
import { PrendaDoc } from '../../../models/firestore.model';
import { CATEGORIAS_PRENDA, COLORES_PRENDA, MAX_NOMBRE } from '../../../models/profile-options';
import { ArmarioService } from '../../../services/app/armario.service';
import { SesionService } from '../../../services/app/sesion.service';
import { comprimirFoto } from '../../../utils/imagen';

/** Prendas genéricas para quien no quiere subir foto (atajo, como en la app). */
const SIN_FOTO = [
  { name: 'Camiseta blanca', category: 'arriba', color: 'blanco' },
  { name: 'Camiseta negra', category: 'arriba', color: 'negro' },
  { name: 'Camisa blanca', category: 'arriba', color: 'blanco' },
  { name: 'Jean azul', category: 'abajo', color: 'azul' },
  { name: 'Pantalón negro', category: 'abajo', color: 'negro' },
  { name: 'Pantalón beige', category: 'abajo', color: 'beige' },
  { name: 'Chaqueta de jean', category: 'abrigo', color: 'azul' },
  { name: 'Buzo gris', category: 'abrigo', color: 'gris' },
  { name: 'Tenis blancos', category: 'calzado', color: 'blanco' },
  { name: 'Zapatos negros', category: 'calzado', color: 'negro' },
];

/** Agregar o editar una prenda: foto del computador o de la cámara, categoría y color. */
@Component({
  selector: 'app-prenda-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent, OptionChipsComponent, ColorSwatchesComponent, CamaraComponent],
  templateUrl: './prenda-form.component.html',
})
export class PrendaFormComponent implements OnInit, OnDestroy {
  private armario = inject(ArmarioService);
  private sesion = inject(SesionService);
  private router = inject(Router);

  id = input<string>();

  readonly categorias = CATEGORIAS_PRENDA;
  readonly colores = COLORES_PRENDA;
  readonly genericas = SIN_FOTO;

  private prenda: PrendaDoc | null = null;
  foto = signal<Blob | null>(null);
  vistaPrevia = signal<string | null>(null);
  camara = signal(false);
  guardando = signal(false);
  error = signal<string | null>(null);

  form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(MAX_NOMBRE)] }),
    category: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    color: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  get esEdicion(): boolean {
    return !!this.id();
  }

  async ngOnInit(): Promise<void> {
    if (this.esEdicion) {
      this.prenda = (await firstValueFrom(this.armario.prenda$(this.sesion.uidActual(), this.id()!))) ?? null;
      if (this.prenda) {
        this.form.patchValue({ name: this.prenda.name, category: this.prenda.category, color: this.prenda.color });
        this.vistaPrevia.set(this.prenda.photoUrl ?? null);
      }
    }
  }

  ngOnDestroy(): void {
    this.liberarVistaPrevia();
  }

  async elegirArchivo(event: Event): Promise<void> {
    const archivo = (event.target as HTMLInputElement).files?.[0];
    if (archivo) {
      await this.usarFoto(archivo);
    }
  }

  async usarFoto(foto: Blob): Promise<void> {
    this.camara.set(false);
    this.error.set(null);
    try {
      const comprimida = await comprimirFoto(foto);
      this.liberarVistaPrevia();
      this.foto.set(comprimida);
      this.vistaPrevia.set(URL.createObjectURL(comprimida));
    } catch {
      this.error.set('No pudimos leer esa imagen. Prueba con otra foto (JPG o PNG).');
    }
  }

  usarGenerica(g: { name: string; category: string; color: string }): void {
    this.form.setValue({ name: g.name, category: g.category, color: g.color });
  }

  async guardar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    const uid = this.sesion.uidActual();
    const { name, category, color } = this.form.getRawValue();
    try {
      await this.armario.guardarPrenda(uid, {
        id: this.prenda?.id ?? this.armario.nuevoIdDePrenda(uid),
        name, category, color,
        photoUrl: this.prenda?.photoUrl ?? null,
        favorite: this.prenda?.favorite ?? false,
        createdAt: this.prenda?.createdAt ?? Date.now(),
      }, this.foto());
      await this.router.navigate(['/armario/prendas']);
    } catch {
      this.error.set('No se pudo guardar la prenda. Revisa tu conexión e intenta de nuevo.');
      this.guardando.set(false);
    }
  }

  private liberarVistaPrevia(): void {
    const actual = this.vistaPrevia();
    if (actual?.startsWith('blob:')) {
      URL.revokeObjectURL(actual);
    }
  }
}
