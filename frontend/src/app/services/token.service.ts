import { Injectable } from '@angular/core';

const TOKEN_KEY = 'orlo.admin.token';
const EXPIRES_KEY = 'orlo.admin.expiresAt';
const EMAIL_KEY = 'orlo.admin.email';

/**
 * Guarda el token del panel en localStorage. Solo strings; la expiración se guarda
 * para no mandar un token vencido.
 */
@Injectable({ providedIn: 'root' })
export class TokenService {

  guardar(token: string, expiresInSeconds: number, email: string): void {
    const expiresAt = Date.now() + expiresInSeconds * 1000;
    this.safe(() => {
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(EXPIRES_KEY, String(expiresAt));
      localStorage.setItem(EMAIL_KEY, email);
    });
  }

  obtener(): string | null {
    return this.safe(() => {
      const token = localStorage.getItem(TOKEN_KEY);
      const expiresAt = Number(localStorage.getItem(EXPIRES_KEY) ?? 0);
      if (!token || Date.now() >= expiresAt) {
        return null;
      }
      return token;
    }) ?? null;
  }

  correo(): string | null {
    return this.safe(() => localStorage.getItem(EMAIL_KEY)) ?? null;
  }

  hayToken(): boolean {
    return this.obtener() !== null;
  }

  borrar(): void {
    this.safe(() => {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(EXPIRES_KEY);
      localStorage.removeItem(EMAIL_KEY);
    });
  }

  /** localStorage lanza excepción en modo privado o con el almacenamiento bloqueado. */
  private safe<T>(fn: () => T): T | undefined {
    try {
      return fn();
    } catch {
      return undefined;
    }
  }
}
