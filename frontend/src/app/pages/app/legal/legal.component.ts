import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Términos de uso y política de privacidad. Texto base: un abogado debe revisar la versión final
 * antes de publicar (Ley 1581 de 2012).
 */
@Component({
  selector: 'app-legal',
  imports: [RouterLink],
  template: `
    <main class="container py-5" style="max-width: 46rem;">
      <a routerLink="/" class="small text-secondary text-decoration-none"><i class="bi bi-arrow-left me-1"></i>Inicio</a>
      @if (esPrivacidad()) {
        <h1 class="h3 fw-bold mt-2">Política de privacidad</h1>
        <p class="text-secondary small">Versión de prueba. La versión final la revisa un abogado antes de publicar.</p>
        <h2 class="h6 fw-bold mt-4">Qué datos guardamos</h2>
        <p>Tu nombre, tu correo (solo en el sistema de inicio de sesión), tus respuestas del cuestionario, tus preferencias, las fotos de tus prendas, tus outfits y tus conversaciones con el asesor.</p>
        <h2 class="h6 fw-bold mt-4">Datos sensibles</h2>
        <p>El tipo de cuerpo y los colores de piel, cabello y ojos son opcionales. Solo se guardan si das tu consentimiento, y la app funciona sin ellos.</p>
        <h2 class="h6 fw-bold mt-4">Inteligencia artificial</h2>
        <p>Las preguntas al asesor se procesan con un servicio externo de IA. Solo enviamos lo necesario para recomendarte: tu primer nombre, tu estilo, tus preferencias y tus prendas. Nunca tu correo ni tus apellidos.</p>
        <h2 class="h6 fw-bold mt-4">Tus derechos</h2>
        <p>Desde "Mi cuenta" puedes ver y descargar tus datos, corregirlos y eliminar tu cuenta con todo lo que guardamos. Tus fotos son privadas: nadie más las ve.</p>
      } @else {
        <h1 class="h3 fw-bold mt-2">Términos de uso</h1>
        <p class="text-secondary small">Versión de prueba. La versión final la revisa un abogado antes de publicar.</p>
        <h2 class="h6 fw-bold mt-4">Qué es ORLO</h2>
        <p>Un asesor de imagen personal: te ayuda a descubrir tu estilo, aprender y organizar tu ropa. Las recomendaciones son sugerencias; tú decides.</p>
        <h2 class="h6 fw-bold mt-4">Plan premium</h2>
        <p>Cuesta $10.000 COP al mes. Antes de cobrar te mostramos el precio, la renovación y cómo cancelar. Si cancelas, tus datos se conservan.</p>
        <h2 class="h6 fw-bold mt-4">Edad mínima</h2>
        <p>Por definir con asesoría legal.</p>
      }
    </main>
  `,
})
export class LegalComponent {
  tipo = input<'terminos' | 'privacidad'>('terminos');
  esPrivacidad = computed(() => this.tipo() === 'privacidad');
}
