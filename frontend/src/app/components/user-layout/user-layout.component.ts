import { Location } from '@angular/common';
import { Component, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs/operators';

import { SesionService } from '../../services/app/sesion.service';
import { UsuarioActualService } from '../../services/app/usuario-actual.service';
import { esPestana, limpiarRuta, rutaPadre } from '../../utils/navegacion-movil';

interface Seccion {
  label: string;
  icon: string;
  route: string;
}

interface Pestana extends Seccion {
  /** Ícono relleno cuando la pestaña está activa, como en iOS. */
  iconActivo: string;
}

/** A partir de este desplazamiento el título grande ya salió y la barra muestra el título pequeño. */
const SCROLL_TITULO = 44;

/**
 * Estructura de la web de usuarios. En el computador: menú arriba. En el celular, como una app de
 * iOS: barra de navegación con flecha de volver y título, y barra de pestañas abajo.
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
  private location = inject(Location);
  usuario = inject(UsuarioActualService);
  menuCuenta = signal(false);

  readonly secciones: Seccion[] = [
    { label: 'Aprende', icon: 'bi-journal-text', route: '/aprende' },
    { label: 'Asesor IA', icon: 'bi-chat-heart', route: '/asesor' },
    { label: 'Explora', icon: 'bi-compass', route: '/explora' },
    { label: 'Mi armario', icon: 'bi-grid-3x3-gap', route: '/armario' },
  ];

  readonly pestanas: Pestana[] = [
    { label: 'Inicio', icon: 'bi-house', iconActivo: 'bi-house-fill', route: '/inicio' },
    { label: 'Aprende', icon: 'bi-book', iconActivo: 'bi-book-fill', route: '/aprende' },
    { label: 'Asesor', icon: 'bi-chat-heart', iconActivo: 'bi-chat-heart-fill', route: '/asesor' },
    { label: 'Explora', icon: 'bi-compass', iconActivo: 'bi-compass-fill', route: '/explora' },
    { label: 'Armario', icon: 'bi-grid-3x3-gap', iconActivo: 'bi-grid-3x3-gap-fill', route: '/armario' },
  ];

  private ruta = signal(limpiarRuta(this.router.url));
  private desplazamiento = signal(0);
  /** Título de la pantalla actual, tomado del título de la ruta. */
  titulo = signal('');

  esRaiz = computed(() => esPestana(this.ruta()));
  /** El asesor es un chat a pantalla completa: no tiene título grande que se desplace. */
  esChat = computed(() => this.ruta().startsWith('/asesor'));
  /** Con el chat la barra lleva fondo desde el inicio, igual que en las pantallas internas. */
  conFondo = computed(() => !this.esRaiz() || this.esChat() || this.desplazamiento() > 4);
  tituloVisible = computed(() => !this.esRaiz() || this.esChat() || this.desplazamiento() > SCROLL_TITULO);

  constructor() {
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd), takeUntilDestroyed())
      .subscribe(() => this.actualizarRuta());
  }

  ngOnInit(): void {
    this.usuario.cargarMe();
    this.actualizarRuta();
  }

  @HostListener('window:scroll')
  alDesplazar(): void {
    this.desplazamiento.set(window.scrollY);
  }

  /** Vuelve a la pantalla anterior; si se entró directo por el link, a la pantalla padre. */
  volver(): void {
    if (this.router.lastSuccessfulNavigation?.previousNavigation) {
      this.location.back();
    } else {
      this.router.navigateByUrl(rutaPadre(this.ruta()));
    }
  }

  async salir(): Promise<void> {
    this.menuCuenta.set(false);
    await this.sesion.salir();
    this.usuario.limpiar();
    this.router.navigate(['/']);
  }

  /** Título de la ruta más profunda. El del documento todavía no cambió cuando llega NavigationEnd. */
  private tituloDeRuta(): string {
    let r = this.router.routerState.snapshot.root;
    while (r.firstChild) {
      r = r.firstChild;
    }
    return (r.title ?? '').replace(' · ORLO', '');
  }

  private actualizarRuta(): void {
    const ruta = limpiarRuta(this.router.url);
    this.ruta.set(ruta);
    this.desplazamiento.set(window.scrollY);
    this.titulo.set(ruta.startsWith('/armario') && esPestana(ruta)
      ? 'Mi armario'
      : this.tituloDeRuta());
  }
}
