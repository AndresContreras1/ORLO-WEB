import { Component, ElementRef, OnDestroy, output, signal, viewChild } from '@angular/core';

/**
 * Cámara del navegador (computador o celular) para tomar la foto de una prenda.
 * Si el navegador no da acceso, se avisa y se puede subir un archivo.
 */
@Component({
  selector: 'app-camara',
  template: `
    <div class="modal d-block" tabindex="-1" role="dialog" aria-modal="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0">
          <div class="modal-header border-0 pb-0"><h2 class="modal-title fs-5">Tomar foto</h2></div>
          <div class="modal-body">
            @if (error()) {
              <div class="alert alert-light border small mb-0">{{ error() }}</div>
            } @else {
              <video #video autoplay playsinline muted class="w-100 rounded-3 bg-dark" style="aspect-ratio: 4/3; object-fit: cover"></video>
              <p class="small text-secondary mt-2 mb-0">Pon la prenda sobre un fondo liso y con buena luz.</p>
            }
          </div>
          <div class="modal-footer border-0 pt-0">
            <button type="button" class="btn btn-outline-secondary" (click)="cerrar()">Cancelar</button>
            @if (!error()) {
              <button type="button" class="btn btn-primary" (click)="capturar()" [disabled]="!lista()"><i class="bi bi-camera me-1"></i>Tomar foto</button>
            }
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop show"></div>
  `,
})
export class CamaraComponent implements OnDestroy {
  foto = output<Blob>();
  cancelada = output<void>();

  error = signal<string | null>(null);
  lista = signal(false);
  private video = viewChild<ElementRef<HTMLVideoElement>>('video');
  private stream: MediaStream | null = null;

  constructor() {
    this.iniciar();
  }

  private async iniciar(): Promise<void> {
    if (!navigator.mediaDevices?.getUserMedia) {
      this.error.set('Este navegador no permite usar la cámara. Sube una foto desde tus archivos.');
      return;
    }
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' }, audio: false });
      queueMicrotask(() => {
        const v = this.video()?.nativeElement;
        if (v) {
          v.srcObject = this.stream;
          v.onloadedmetadata = () => this.lista.set(true);
        }
      });
    } catch {
      this.error.set('No pudimos usar la cámara (permiso negado o sin cámara). Sube una foto desde tus archivos.');
    }
  }

  capturar(): void {
    const v = this.video()?.nativeElement;
    if (!v) {
      return;
    }
    const canvas = document.createElement('canvas');
    canvas.width = v.videoWidth;
    canvas.height = v.videoHeight;
    canvas.getContext('2d')?.drawImage(v, 0, 0);
    canvas.toBlob((blob) => {
      if (blob) {
        this.foto.emit(blob);
      }
      this.detener();
    }, 'image/jpeg', 0.9);
  }

  cerrar(): void {
    this.detener();
    this.cancelada.emit();
  }

  ngOnDestroy(): void {
    this.detener();
  }

  private detener(): void {
    this.stream?.getTracks().forEach((t) => t.stop());
    this.stream = null;
  }
}
