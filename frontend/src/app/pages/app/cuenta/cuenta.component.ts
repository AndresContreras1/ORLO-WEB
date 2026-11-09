import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { ConfirmDialogComponent } from '../../../components/confirm-dialog/confirm-dialog.component';
import { CLIMAS, OCASIONES, PRESUPUESTOS, RANGOS_DE_EDAD, etiqueta } from '../../../models/profile-options';
import { ContenidoService } from '../../../services/app/contenido.service';
import { PerfilService } from '../../../services/app/perfil.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';
import { comprimirFoto } from '../../../utils/imagen';

/** Cuenta: foto, datos del perfil, descargar mis datos, eliminar la cuenta y cerrar sesión. */
@Component({
  selector: 'app-cuenta',
  imports: [RouterLink, ConfirmDialogComponent],
  templateUrl: './cuenta.component.html',
})
export class CuentaComponent {
  private perfilService = inject(PerfilService);
  private contenido = inject(ContenidoService);
  private router = inject(Router);
  sesion = inject(SesionService);
  usuario = inject(UsuarioActualService);

  subiendoFoto = signal(false);
  descargando = signal(false);
  confirmarEliminar = signal(false);
  eliminando = signal(false);
  error = signal<string | null>(null);

  edad = () => etiqueta(RANGOS_DE_EDAD, this.usuario.perfil()?.ageRange);
  clima = () => etiqueta(CLIMAS, this.usuario.perfil()?.climate);
  presupuesto = () => etiqueta(PRESUPUESTOS, this.usuario.perfil()?.budget);
  ocasiones = () => (this.usuario.perfil()?.occasions ?? []).map((o) => etiqueta(OCASIONES, o)).join(', ');

  async cambiarFoto(event: Event): Promise<void> {
    const archivo = (event.target as HTMLInputElement).files?.[0];
    if (!archivo) {
      return;
    }
    this.subiendoFoto.set(true);
    this.error.set(null);
    try {
      await this.perfilService.subirFotoPerfil(this.sesion.uidActual(), await comprimirFoto(archivo, 512));
    } catch {
      this.error.set('No se pudo cambiar la foto. Prueba con otra imagen.');
    }
    this.subiendoFoto.set(false);
  }

  /** Descarga un JSON con todo lo que ORLO guarda de la persona. */
  async descargarMisDatos(): Promise<void> {
    this.descargando.set(true);
    try {
      const datos = await this.perfilService.exportarMisDatos(this.sesion.uidActual());
      const blob = new Blob([JSON.stringify({ correo: this.sesion.correo(), ...datos }, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'mis-datos-orlo.json';
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      this.error.set('No se pudieron descargar tus datos. Intenta de nuevo.');
    }
    this.descargando.set(false);
  }

  /**
   * Igual que la app: primero se borran los datos de Firestore y Storage, después el backend
   * borra la cuenta de Authentication y su uso, y al final se cierra la sesión.
   */
  async eliminarCuenta(): Promise<void> {
    this.eliminando.set(true);
    this.error.set(null);
    try {
      const uid = this.sesion.uidActual();
      await this.perfilService.borrarMisDatos(uid, this.usuario.prendas().map((p) => p.id));
      await firstValueFrom(this.contenido.eliminarCuenta());
      await this.salir();
    } catch {
      this.eliminando.set(false);
      this.confirmarEliminar.set(false);
      this.error.set('No se pudo eliminar la cuenta. Intenta de nuevo; si sigue fallando, escríbenos.');
    }
  }

  async salir(): Promise<void> {
    this.usuario.limpiar();
    await this.sesion.salir();
    await this.router.navigate(['/']);
  }
}
