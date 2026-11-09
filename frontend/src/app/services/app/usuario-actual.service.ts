import { Injectable, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, of, switchMap } from 'rxjs';

import { Me } from '../../models/app-api.model';
import { PrendaDoc, UsuarioDoc } from '../../models/firestore.model';
import { ArmarioService } from './armario.service';
import { ContenidoService } from './contenido.service';
import { PerfilService } from './perfil.service';
import { SesionService } from './sesion.service';

/**
 * Lo que varias pantallas necesitan de la persona: su perfil y sus prendas en tiempo real
 * (si cambian en la app móvil, cambian aquí) y su estado premium según el backend.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioActualService {
  private sesion = inject(SesionService);
  private perfilService = inject(PerfilService);
  private armarioService = inject(ArmarioService);
  private contenido = inject(ContenidoService);

  private porUsuario<T>(fuente: (uid: string) => Observable<T>, vacio: T): Observable<T> {
    return this.sesion.user$.pipe(switchMap((u) => (u ? fuente(u.uid) : of(vacio))));
  }

  /** undefined mientras carga; null sin sesión o sin documento. */
  readonly perfil = toSignal(this.porUsuario<UsuarioDoc | undefined | null>((uid) => this.perfilService.perfil$(uid), null),
    { initialValue: undefined });
  readonly prendas = toSignal(this.porUsuario<PrendaDoc[]>((uid) => this.armarioService.prendas$(uid), []),
    { initialValue: [] as PrendaDoc[] });

  readonly nombre = computed(() => this.perfil()?.name ?? '');
  readonly primerNombre = computed(() => this.nombre().split(/\s+/)[0] ?? '');
  readonly resultado = computed(() => this.perfil()?.currentResult ?? null);

  private readonly meSignal = signal<Me | null>(null);
  readonly me = this.meSignal.asReadonly();
  /** Mientras no se sepa, no se bloquea nada (el backend vuelve a validar en cada petición). */
  readonly premium = computed(() => this.meSignal()?.premium ?? true);

  cargarMe(): void {
    this.contenido.me().subscribe({ next: (me) => this.meSignal.set(me), error: () => undefined });
  }

  actualizarMensajesRestantes(restantes: number): void {
    this.meSignal.update((me) => (me ? { ...me, remainingMessages: restantes } : me));
  }

  limpiar(): void {
    this.meSignal.set(null);
  }
}
