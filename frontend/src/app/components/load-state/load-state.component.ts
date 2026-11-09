import { Component, input, output } from '@angular/core';

/** Estado de carga y de error con botón para reintentar. */
@Component({
  selector: 'app-load-state',
  template: `
    @if (loading()) {
      <div class="d-flex align-items-center gap-2 text-secondary py-4">
        <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
        Cargando…
      </div>
    } @else if (error()) {
      <div class="alert alert-light border d-flex flex-wrap align-items-center justify-content-between gap-2" role="alert">
        <span><i class="bi bi-exclamation-circle text-danger me-2"></i>{{ error() }}</span>
        <button type="button" class="btn btn-sm btn-primary" (click)="retry.emit()">Reintentar</button>
      </div>
    }
  `,
})
export class LoadStateComponent {
  loading = input(false);
  error = input<string | null>(null);
  retry = output<void>();
}
