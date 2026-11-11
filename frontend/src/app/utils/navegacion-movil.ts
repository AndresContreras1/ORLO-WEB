import { ActivatedRouteSnapshot, ViewTransitionInfo } from '@angular/router';

/** Pantallas raíz de la barra inferior: no llevan botón de volver. */
const PESTANAS = ['/inicio', '/aprende', '/asesor', '/explora', '/armario/prendas', '/armario/outfits', '/armario/estilo', '/armario/historial'];

const ANCHO_CELULAR = '(max-width: 767.98px)';

/** Las transiciones se activan solo si la web abre en un celular: en el computador no se cargan. */
export function esCelular(): boolean {
  return window.matchMedia(ANCHO_CELULAR).matches;
}

export function esPestana(ruta: string): boolean {
  return PESTANAS.includes(ruta);
}

/** Ruta sin parámetros ni fragmento. */
export function limpiarRuta(url: string): string {
  return url.split(/[?#]/)[0];
}

/** A dónde vuelve la flecha cuando la pantalla se abrió directo, sin historial dentro de la app. */
export function rutaPadre(ruta: string): string {
  if (ruta === '/cuenta') {
    return '/inicio';
  }
  if (ruta.startsWith('/explora/')) {
    return '/explora';
  }
  const partes = ruta.split('/').filter(Boolean);
  return partes.length > 1 ? `/${partes.slice(0, -1).join('/')}` : '/inicio';
}

function rutaDe(raiz: ActivatedRouteSnapshot): string {
  const partes: string[] = [];
  for (let r: ActivatedRouteSnapshot | null = raiz; r; r = r.firstChild) {
    partes.push(...r.url.map((s) => s.path));
  }
  return `/${partes.join('/')}`;
}

/** Las pestañas son el primer nivel, aunque su ruta tenga dos partes (/armario/prendas). */
function profundidad(ruta: string): number {
  return esPestana(ruta) ? 1 : ruta.split('/').filter(Boolean).length;
}

/**
 * Animación entre pantallas, solo en el celular. Avanzar empuja desde la derecha, volver desliza
 * hacia la derecha y cambiar de pestaña no anima, igual que en iOS. En el computador se omite.
 */
export function transicionMovil({ transition, from, to }: ViewTransitionInfo): void {
  // Si la transición se omite o la pestaña está oculta, estas promesas se rechazan: no es un error
  transition.ready.catch(() => undefined);
  const animar = esCelular()
    && !window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (!animar) {
    transition.skipTransition();
    return;
  }
  const antes = profundidad(rutaDe(from));
  const despues = profundidad(rutaDe(to));
  const raiz = document.documentElement;
  raiz.dataset['transicion'] = despues > antes ? 'adelante' : despues < antes ? 'atras' : 'igual';
  transition.finished.catch(() => undefined).finally(() => delete raiz.dataset['transicion']);
}
