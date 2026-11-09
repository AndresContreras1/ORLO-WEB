import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { TokenService } from '../../services/token.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

/** Estructura del panel: menú lateral fijo en escritorio y desplegable en el celular. */
@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.scss',
})
export class AdminLayoutComponent {
  private authService = inject(AuthService);
  correo = inject(TokenService).correo();
  menuAbierto = signal(false);

  readonly items: NavItem[] = [
    { label: 'Resumen', icon: 'bi-speedometer2', route: '/admin/dashboard' },
    { label: 'Estilos', icon: 'bi-palette', route: '/admin/styles' },
    { label: 'Cuestionario', icon: 'bi-ui-checks', route: '/admin/quiz' },
    { label: 'Lecciones', icon: 'bi-journal-text', route: '/admin/lessons' },
    { label: 'Explora', icon: 'bi-compass', route: '/admin/explore' },
    { label: 'Tiendas', icon: 'bi-shop', route: '/admin/stores' },
    { label: 'Asesor IA', icon: 'bi-chat-heart', route: '/admin/ai' },
    { label: 'Usuarios', icon: 'bi-people', route: '/admin/users' },
  ];

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  salir(): void {
    this.authService.logout();
  }
}
