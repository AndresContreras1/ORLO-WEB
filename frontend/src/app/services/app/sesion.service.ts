import { Injectable, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  Auth,
  authState,
  createUserWithEmailAndPassword,
  sendPasswordResetEmail,
  signInWithEmailAndPassword,
  signOut,
  User,
} from '@angular/fire/auth';
import { Observable, firstValueFrom } from 'rxjs';
import { filter, take } from 'rxjs/operators';

import { FirebaseBase } from './firebase-base';

/** Sesión del usuario de la web con Firebase Authentication (la misma cuenta que en la app). */
@Injectable({ providedIn: 'root' })
export class SesionService extends FirebaseBase {
  private auth = inject(Auth);

  /** undefined mientras Firebase revisa si hay sesión guardada; null si no hay sesión. */
  readonly user$: Observable<User | null> = this.ctx(() => authState(this.auth));
  readonly usuario = toSignal(this.user$);
  readonly uid = computed(() => this.usuario()?.uid ?? null);
  readonly correo = computed(() => this.usuario()?.email ?? null);

  /** Espera a que Firebase diga si hay sesión (para los guards). */
  usuarioActual(): Promise<User | null> {
    return firstValueFrom(this.user$.pipe(filter((u) => u !== undefined), take(1)));
  }

  registrar(correo: string, password: string): Promise<User> {
    return this.ctx(() => createUserWithEmailAndPassword(this.auth, correo.trim(), password)).then((c) => c.user);
  }

  entrar(correo: string, password: string): Promise<User> {
    return this.ctx(() => signInWithEmailAndPassword(this.auth, correo.trim(), password)).then((c) => c.user);
  }

  recuperarPassword(correo: string): Promise<void> {
    return this.ctx(() => sendPasswordResetEmail(this.auth, correo.trim()));
  }

  salir(): Promise<void> {
    return this.ctx(() => signOut(this.auth));
  }

  /** uid de la sesión o error: las pantallas que lo usan están detrás del guard. */
  uidActual(): string {
    const uid = this.auth.currentUser?.uid;
    if (!uid) {
      throw new Error('No hay sesión');
    }
    return uid;
  }
}

/** Mensaje en palabras simples para los errores de Firebase Authentication. */
export function mensajeAuth(error: unknown): string {
  const code = (error as { code?: string })?.code ?? '';
  switch (code) {
    case 'auth/invalid-credential':
    case 'auth/wrong-password':
    case 'auth/user-not-found':
    case 'auth/invalid-login-credentials':
      return 'Correo o contraseña incorrectos.';
    case 'auth/email-already-in-use':
      return 'Ya existe una cuenta con ese correo. Entra con tu contraseña.';
    case 'auth/weak-password':
      return 'La contraseña debe tener al menos 6 caracteres.';
    case 'auth/invalid-email':
      return 'El correo no es válido.';
    case 'auth/too-many-requests':
      return 'Hubo muchos intentos. Espera un momento y vuelve a intentar.';
    case 'auth/network-request-failed':
      return 'No hay conexión. Revisa tu internet e intenta de nuevo.';
    default:
      return 'No se pudo completar. Intenta de nuevo.';
  }
}
