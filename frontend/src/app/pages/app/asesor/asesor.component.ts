import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, ElementRef, computed, effect, inject, input, signal, viewChild } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { combineLatest, of, switchMap } from 'rxjs';

import { ConfirmDialogComponent } from '../../../components/confirm-dialog/confirm-dialog.component';
import { PremiumGateComponent } from '../../../components/premium-gate/premium-gate.component';
import { OutfitSuggestion, QuickAction } from '../../../models/app-api.model';
import { MensajeDoc } from '../../../models/firestore.model';
import { ArmarioService } from '../../../services/app/armario.service';
import { AsesorService, armarioParaAsesor, perfilParaAsesor } from '../../../services/app/asesor.service';
import { ConversacionService } from '../../../services/app/conversacion.service';
import { SesionService } from '../../../services/app/sesion.service';
import { UsuarioActualService } from '../../../services/app/usuario-actual.service';
import { errorMessage } from '../../../utils/error-message';

const HISTORIAL = 10;

/**
 * Chat con el asesor. Cada mensaje se guarda en Firestore (la conversación se ve igual en el
 * celular) y la respuesta llega de la API con 2 o 3 outfits que se pueden guardar.
 */
@Component({
  selector: 'app-asesor',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, PremiumGateComponent, ConfirmDialogComponent],
  templateUrl: './asesor.component.html',
  styleUrl: './asesor.component.scss',
})
export class AsesorComponent {
  private sesion = inject(SesionService);
  private conversacionService = inject(ConversacionService);
  private asesor = inject(AsesorService);
  private armario = inject(ArmarioService);
  private router = inject(Router);
  usuario = inject(UsuarioActualService);

  /** Id de la conversación abierta (parámetro opcional de la ruta). */
  id = input<string>();

  readonly ejemplos = [
    '¿Qué me puedo poner para ir a la universidad?',
    '¿Cómo combino un pantalón que ya tengo?',
    '¿Qué uso para una salida con amigos?',
    '¿Cómo hago un outfit elegante sin perder mi estilo?',
    '¿Qué colores combinan con una camisa azul?',
    '¿Cómo le doy otro aire a una combinación básica?',
  ];

  private uid$ = this.sesion.user$;
  conversaciones = toSignal(this.uid$.pipe(switchMap((u) => (u ? this.conversacionService.conversaciones$(u.uid) : of([])))),
    { initialValue: [] });
  mensajes = toSignal(combineLatest([this.uid$, toObservable(this.id)]).pipe(
    switchMap(([u, id]) => (u && id ? this.conversacionService.mensajes$(u.uid, id) : of([] as MensajeDoc[])))),
  { initialValue: [] as MensajeDoc[] });

  pregunta = new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(1000)] });
  pensando = signal(false);
  error = signal<string | null>(null);
  guardados = signal<Set<string>>(new Set());
  aBorrar = signal<string | null>(null);
  borrando = signal(false);
  listaAbierta = signal(false);

  ultimoDelAsesor = computed(() => {
    const m = this.mensajes();
    return m.length && m[m.length - 1].role === 'assistant' ? m[m.length - 1] : null;
  });
  tituloActual = computed(() => this.conversaciones().find((c) => c.id === this.id())?.title ?? 'Nueva conversación');

  private final = viewChild<ElementRef<HTMLElement>>('final');

  constructor() {
    // Baja al último mensaje cuando llega uno nuevo
    effect(() => {
      this.mensajes();
      this.pensando();
      queueMicrotask(() => this.final()?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'end' }));
    });
  }

  enviarEjemplo(texto: string): void {
    this.pregunta.setValue(texto);
    this.enviar();
  }

  enviarRapido(accion: QuickAction, texto: string): void {
    this.enviar(accion, texto);
  }

  async enviar(quickAction?: QuickAction, textoRapido?: string): Promise<void> {
    const texto = (textoRapido ?? this.pregunta.value).trim();
    if (!texto || this.pensando()) {
      return;
    }
    const uid = this.sesion.uidActual();
    this.pensando.set(true);
    this.error.set(null);
    const historial = this.mensajes().slice(-HISTORIAL).map((m) => ({
      role: m.role === 'assistant' ? 'ASSISTANT' as const : 'USER' as const,
      text: m.role === 'assistant' ? this.textoConOutfits(m) : m.text,
    }));
    try {
      let conversacionId = this.id();
      if (!conversacionId) {
        conversacionId = await this.conversacionService.crear(uid, texto);
        await this.router.navigate(['/asesor', conversacionId], { replaceUrl: true });
      }
      await this.conversacionService.agregarMensaje(uid, conversacionId, {
        role: 'user', text: texto, outfits: [], followUpQuestion: null, createdAt: Date.now(),
      });
      this.pregunta.reset('');
      this.asesor.enviar({
        message: texto,
        quickAction,
        profile: perfilParaAsesor(this.usuario.perfil()),
        wardrobe: armarioParaAsesor(this.usuario.prendas()),
        history: historial,
      }).subscribe({
        next: async (r) => {
          await this.conversacionService.agregarMensaje(uid, conversacionId!, {
            role: 'assistant', text: r.reply, outfits: r.outfits, followUpQuestion: r.followUpQuestion ?? null, createdAt: Date.now(),
          });
          this.usuario.actualizarMensajesRestantes(r.remainingMessages);
          this.pensando.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(this.mensajeDeError(err));
          this.pensando.set(false);
        },
      });
    } catch {
      this.error.set('No se pudo enviar. Revisa tu conexión e intenta de nuevo.');
      this.pensando.set(false);
    }
  }

  async guardarOutfit(mensaje: MensajeDoc, outfit: OutfitSuggestion, indice: number): Promise<void> {
    await this.armario.guardarOutfit(this.sesion.uidActual(), {
      name: outfit.title.slice(0, 40), occasion: 'otra', garmentIds: [], itemsText: outfit.items, why: outfit.why,
      origin: 'asesor', favorite: false, createdAt: Date.now(),
    });
    this.guardados.update((s) => new Set(s).add(`${mensaje.id}-${indice}`));
  }

  estaGuardado(mensajeId: string, indice: number): boolean {
    return this.guardados().has(`${mensajeId}-${indice}`);
  }

  async confirmarBorrado(): Promise<void> {
    const id = this.aBorrar();
    if (!id) {
      return;
    }
    this.borrando.set(true);
    await this.conversacionService.borrar(this.sesion.uidActual(), id);
    this.borrando.set(false);
    this.aBorrar.set(null);
    if (id === this.id()) {
      this.router.navigate(['/asesor']);
    }
  }

  private textoConOutfits(m: MensajeDoc): string {
    const outfits = m.outfits?.map((o) => `${o.title}: ${o.items.join(', ')}`).join('; ');
    return outfits ? `${m.text} (Propuestas: ${outfits})` : m.text;
  }

  private mensajeDeError(err: HttpErrorResponse): string {
    const code = err.error?.code;
    if (code === 'LIMIT_REACHED') {
      return 'Llegaste al límite de mensajes de hoy. Mañana puedes seguir preguntando.';
    }
    if (code === 'AI_UNAVAILABLE') {
      return 'El asesor no está disponible en este momento. Intenta de nuevo en un rato.';
    }
    if (code === 'PREMIUM_REQUIRED') {
      return 'El asesor es parte de ORLO Premium.';
    }
    return errorMessage(err);
  }
}
