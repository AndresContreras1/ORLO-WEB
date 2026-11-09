import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { FieldErrorComponent } from '../../components/field-error/field-error.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { AiConfigService } from '../../services/ai-config.service';
import { errorMessage } from '../../utils/error-message';

interface ModelOption {
  id: string;
  label: string;
}

/** Instrucciones, modelo y límites del asesor IA, editables sin tocar el código. */
@Component({
  selector: 'app-ai-config',
  imports: [ReactiveFormsModule, FieldErrorComponent, LoadStateComponent],
  templateUrl: './ai-config.component.html',
})
export class AiConfigComponent implements OnInit {
  private aiConfigService = inject(AiConfigService);

  readonly modelos: ModelOption[] = [
    { id: 'claude-haiku-5-5', label: 'Claude Haiku 5.5 · el más económico' },
    { id: 'claude-sonnet-5-5', label: 'Claude Sonnet 5.5 · más detallado, unas 20 veces más caro' },
    { id: 'claude-opus-5-5', label: 'Claude Opus 5.5 · el más capaz, unas 40 veces más caro' },
  ];

  cargando = signal(true);
  errorCarga = signal<string | null>(null);
  guardando = signal(false);
  errorGuardar = signal<string | null>(null);
  guardado = signal(false);

  form = new FormGroup({
    systemPrompt: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(8000)] }),
    model: new FormControl('claude-haiku-5-5', { nonNullable: true, validators: [Validators.required] }),
    dailyLimit: new FormControl(20, { nonNullable: true, validators: [Validators.required, Validators.min(1), Validators.max(500)] }),
    maxOutputTokens: new FormControl(1200, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(300), Validators.max(4000)],
    }),
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.aiConfigService.get().subscribe({
      next: (config) => {
        this.form.setValue(config);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    this.guardando.set(true);
    this.errorGuardar.set(null);
    this.guardado.set(false);
    this.aiConfigService.update({
      ...raw,
      dailyLimit: Number(raw.dailyLimit),
      maxOutputTokens: Number(raw.maxOutputTokens),
    }).subscribe({
      next: (config) => {
        this.form.setValue(config);
        this.form.markAsPristine();
        this.guardando.set(false);
        this.guardado.set(true);
      },
      error: (err) => {
        this.errorGuardar.set(errorMessage(err));
        this.guardando.set(false);
      },
    });
  }
}
