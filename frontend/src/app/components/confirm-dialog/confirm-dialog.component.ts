import { Component, input, output } from '@angular/core';

/** Ventana de confirmación para acciones que no se pueden deshacer (borrar). */
@Component({
  selector: 'app-confirm-dialog',
  template: `
    @if (open()) {
      <div class="modal d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content border-0">
            <div class="modal-header border-0 pb-0">
              <h2 class="modal-title fs-5">{{ heading() }}</h2>
            </div>
            <div class="modal-body">{{ message() }}</div>
            <div class="modal-footer border-0 pt-0">
              <button type="button" class="btn btn-outline-secondary" (click)="cancelled.emit()">Cancelar</button>
              <button type="button" class="btn btn-danger" id="btnConfirmar" [disabled]="busy()"
                      (click)="confirmed.emit()">
                {{ busy() ? 'Borrando…' : confirmLabel() }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop show"></div>
    }
  `,
})
export class ConfirmDialogComponent {
  open = input(false);
  busy = input(false);
  heading = input('¿Seguro?');
  message = input('');
  confirmLabel = input('Borrar');
  confirmed = output<void>();
  cancelled = output<void>();
}
