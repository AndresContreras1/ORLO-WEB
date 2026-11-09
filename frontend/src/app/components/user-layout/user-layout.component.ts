import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { SesionService } from '../../services/app/sesion.service';
import { UsuarioActualService } from '../../services/app/usuario-actual.service';

interface Seccion {
  label: string;
  icon: string;
  route: string;
}

/**
 * Estructura de la web de usuarios: menú arriba en el computador y barra inferior fija con las
 * cuatro secciones en el celular, igual que la app.
 */
@Component({
  selector: 'app-user-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './user-layout.component.html',
  styleUrl: './user-layout.component.scss',
})
export class UserLayoutComponent implements OnInit {
  private sesion = inject(SesionService);
  private router = inject(Router);
  usuario = inject(UsuarioActualService);
  menuCuenta = signal(false);

  readonly secciones: Seccion[] = [
    { label: 'Aprende', icon: 'bi-journal-text', route: '/aprende' },
    { label: 'Asesor IA', icon: 'bi-chat-heart', route: '/asesor' },
    { label: 'Explora', icon: 'bi-compass', route: '/explora' },
    { label: 'Mi armario', icon: 'bi-grid-3x3-gap', route: '/armario' },
  ];

  ngOnInit(): void {
    this.usuario.cargarMe();
  }

  async salir(): Promise<void> {
    this.menuCuenta.set(false);
    await this.sesion.salir();
    this.usuario.limpiar();
    this.router.navigate(['/']);
  }
}
