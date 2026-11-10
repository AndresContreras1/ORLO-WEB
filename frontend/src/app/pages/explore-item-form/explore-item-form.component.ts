import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { FieldErrorComponent } from '../../components/field-error/field-error.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { CombineIdea, ExploreItemRequest } from '../../models/explore-item.model';
import { CATEGORIAS_PRENDA, COLORES_PRENDA } from '../../models/profile-options';
import { Style } from '../../models/style.model';
import { ExploreItemService } from '../../services/explore-item.service';
import { StyleService } from '../../services/style.service';
import { errorMessage } from '../../utils/error-message';

const CONTEXTOS: { codigo: CombineIdea['context']; etiqueta: string }[] = [
  { codigo: 'casual', etiqueta: 'Casual' },
  { codigo: 'universidad', etiqueta: 'Universidad' },
  { codigo: 'elegante', etiqueta: 'Elegante' },
];

/** Crear o editar una prenda del catálogo de Explora. */
@Component({
  selector: 'app-explore-item-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent, LoadStateComponent],
  templateUrl: './explore-item-form.component.html',
})
export class ExploreItemFormComponent implements OnInit {
  private service = inject(ExploreItemService);
  private styleService = inject(StyleService);
  private router = inject(Router);

  id = input<string>();

  readonly categorias = CATEGORIAS_PRENDA;
  readonly colores = COLORES_PRENDA;
  readonly contextos = CONTEXTOS;
  estilos = signal<Style[]>([]);
  estilosElegidos = signal<string[]>([]);
  cargando = signal(false);
  errorCarga = signal<string | null>(null);
  guardando = signal(false);
  errorGuardar = signal<string | null>(null);

  form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(80)] }),
    imageUrl: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/^https?:\/\/.+/), Validators.maxLength(500)] }),
    imageCredit: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(200)] }),
    category: new FormControl('arriba', { nonNullable: true, validators: [Validators.required] }),
    color: new FormControl('negro', { nonNullable: true, validators: [Validators.required] }),
    whyItFits: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(500)] }),
    trend: new FormControl(false, { nonNullable: true }),
    accessory: new FormControl(false, { nonNullable: true }),
    active: new FormControl(true, { nonNullable: true }),
    // Una prenda por línea en cada contexto
    casual: new FormControl('', { nonNullable: true }),
    universidad: new FormControl('', { nonNullable: true }),
    elegante: new FormControl('', { nonNullable: true }),
  });

  get esEdicion(): boolean {
    return !!this.id();
  }

  ngOnInit(): void {
    this.styleService.findAll().subscribe({
      next: (data) => this.estilos.set(data),
      error: (err) => this.errorCarga.set(errorMessage(err)),
    });
    if (this.esEdicion) {
      this.cargar();
    }
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.service.findById(Number(this.id())).subscribe({
      next: (item) => {
        const idea = (c: string) => item.combineIdeas.find((i) => i.context === c)?.items.join('\n') ?? '';
        this.form.patchValue({
          ...item,
          imageCredit: item.imageCredit ?? '',
          casual: idea('casual'),
          universidad: idea('universidad'),
          elegante: idea('elegante'),
        });
        this.estilosElegidos.set(item.styles);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  alternarEstilo(code: string): void {
    this.estilosElegidos.update((s) => (s.includes(code) ? s.filter((x) => x !== code) : [...s, code]));
  }

  guardar(): void {
    if (this.form.invalid || !this.estilosElegidos().length) {
      this.form.markAllAsTouched();
      if (!this.estilosElegidos().length) {
        this.errorGuardar.set('Elige al menos un estilo.');
      }
      return;
    }
    const raw = this.form.getRawValue();
    const ideas: CombineIdea[] = CONTEXTOS
      .map((c) => ({ context: c.codigo, items: raw[c.codigo].split('\n').map((l) => l.trim()).filter(Boolean) }))
      .filter((i) => i.items.length);
    const body: ExploreItemRequest = {
      name: raw.name.trim(),
      imageUrl: raw.imageUrl.trim(),
      imageCredit: raw.imageCredit.trim() || null,
      category: raw.category,
      color: raw.color,
      styles: this.estilosElegidos(),
      whyItFits: raw.whyItFits.trim(),
      trend: raw.trend,
      accessory: raw.accessory,
      active: raw.active,
      combineIdeas: ideas,
    };
    this.guardando.set(true);
    this.errorGuardar.set(null);
    const peticion = this.esEdicion ? this.service.update(Number(this.id()), body) : this.service.create(body);
    peticion.subscribe({
      next: () => this.router.navigate(['/admin/explore']),
      error: (err) => {
        this.errorGuardar.set(errorMessage(err));
        this.guardando.set(false);
      },
    });
  }
}
