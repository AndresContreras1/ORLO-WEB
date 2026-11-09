import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Feature {
  icon: string;
  title: string;
  text: string;
}

interface PlanRow {
  label: string;
  free: boolean;
  premium: boolean;
}

/** Página pública de ORLO. */
@Component({
  selector: 'app-landing',
  imports: [RouterLink],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  menuAbierto = signal(false);

  readonly features: Feature[] = [
    { icon: 'bi-ui-checks', title: 'Descubre tu estilo', text: 'Responde unas preguntas tocando fotos. En menos de 3 minutos sabes cuál es tu estilo y qué colores te acompañan.' },
    { icon: 'bi-journal-text', title: 'Aprende a vestirte', text: 'Lecciones cortas y visuales: cómo combinar colores, proporciones y los detalles que cambian un outfit.' },
    { icon: 'bi-chat-heart', title: 'Pregúntale al asesor', text: '¿Qué me pongo para la universidad? El asesor te propone opciones con la ropa que ya tienes.' },
    { icon: 'bi-grid-3x3-gap', title: 'Organiza tu armario', text: 'Guarda tus prendas y los outfits que te funcionaron para no perderlos.' },
  ];

  readonly principios: string[] = [
    'Ningún estilo es mejor que otro: casual, urbano, deportivo, clásico o formal.',
    'Te orientamos, no te imponemos. Tú decides.',
    'Palabras simples. Si hay un término técnico, lo explicamos en una línea.',
    'Primero usamos lo que ya tienes. Comprar es opcional.',
    'Los cuerpos no se juzgan.',
  ];

  readonly plan: PlanRow[] = [
    { label: 'Cuestionario y resultado de tu estilo', free: true, premium: true },
    { label: 'Galería de referencias', free: true, premium: true },
    { label: 'Primera lección de muestra', free: true, premium: true },
    { label: 'Todas las lecciones con tu avance', free: false, premium: true },
    { label: 'Asesor con inteligencia artificial', free: false, premium: true },
    { label: 'Mi armario: prendas, outfits e historial', free: false, premium: true },
  ];

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }
}
