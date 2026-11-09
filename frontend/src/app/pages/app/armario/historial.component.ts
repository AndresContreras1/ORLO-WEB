import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { of, switchMap } from 'rxjs';

import { ConfirmDialogComponent } from '../../../components/confirm-dialog/confirm-dialog.component';
import { ConversacionDoc, OutfitDoc, ResultadoDoc } from '../../../models/firestore.model';
import { ArmarioService } from '../../../services/app/armario.service';
import { ConversacionService } from '../../../services/app/conversacion.service';
import { PerfilService } from '../../../services/app/perfil.service';
import { SesionService } from '../../../services/app/sesion.service';

type Tipo = 'todo' | 'resultados' | 'outfits' | 'conversaciones';

interface Evento {
  tipo: Exclude<Tipo, 'todo'>;
  fecha: number;
  titulo: string;
  detalle: string;
  enlace: string | null;
  conversacion?: ConversacionDoc;
}

/** Historial: resultados del cuestionario, outfits guardados y conversaciones, por fecha. */
@Component({
  selector: 'app-historial',
  imports: [RouterLink, DatePipe, ConfirmDialogComponent],
  templateUrl: './historial.component.html',
})
export class HistorialComponent {
  private sesion = inject(SesionService);
  private perfil = inject(PerfilService);
  private armario = inject(ArmarioService);
  private conversaciones = inject(ConversacionService);

  private resultados = toSignal(this.sesion.user$.pipe(switchMap((u) => (u ? this.perfil.resultados$(u.uid) : of([])))),
    { initialValue: [] as ResultadoDoc[] });
  private outfits = toSignal(this.sesion.user$.pipe(switchMap((u) => (u ? this.armario.outfits$(u.uid) : of([])))),
    { initialValue: [] as OutfitDoc[] });
  private chats = toSignal(this.sesion.user$.pipe(switchMap((u) => (u ? this.conversaciones.conversaciones$(u.uid) : of([])))),
    { initialValue: [] as ConversacionDoc[] });

  filtro = signal<Tipo>('todo');
  aBorrar = signal<ConversacionDoc | null>(null);
  borrando = signal(false);

  readonly filtros: { id: Tipo; label: string }[] = [
    { id: 'todo', label: 'Todo' },
    { id: 'resultados', label: 'Cuestionarios' },
    { id: 'outfits', label: 'Outfits' },
    { id: 'conversaciones', label: 'Conversaciones' },
  ];

  /** Eventos agrupados por día, del más reciente al más antiguo. */
  dias = computed(() => {
    const eventos: Evento[] = [
      ...this.resultados().map((r) => ({
        tipo: 'resultados' as const, fecha: r.createdAt, titulo: `Resultado: ${r.primary.name}`,
        detalle: `con toques de ${r.secondary.name}`, enlace: null,
      })),
      ...this.outfits().map((o) => ({
        tipo: 'outfits' as const, fecha: o.createdAt, titulo: o.name,
        detalle: o.origin === 'asesor' ? 'Outfit guardado del asesor' : 'Outfit creado por ti', enlace: `/armario/outfits/${o.id}`,
      })),
      ...this.chats().map((c) => ({
        tipo: 'conversaciones' as const, fecha: c.updatedAt, titulo: c.title,
        detalle: 'Conversación con el asesor', enlace: `/asesor/${c.id}`, conversacion: c,
      })),
    ].filter((e) => this.filtro() === 'todo' || e.tipo === this.filtro())
      .sort((a, b) => b.fecha - a.fecha);

    const grupos = new Map<string, Evento[]>();
    for (const e of eventos) {
      const d = new Date(e.fecha);
      const clave = `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
      grupos.set(clave, [...(grupos.get(clave) ?? []), e]);
    }
    return [...grupos.values()].map((lista) => ({ fecha: lista[0].fecha, eventos: lista }));
  });

  icono(tipo: Evento['tipo']): string {
    return { resultados: 'bi-stars', outfits: 'bi-collection', conversaciones: 'bi-chat-dots' }[tipo];
  }

  async confirmarBorrado(): Promise<void> {
    const c = this.aBorrar();
    if (!c) {
      return;
    }
    this.borrando.set(true);
    await this.conversaciones.borrar(this.sesion.uidActual(), c.id);
    this.borrando.set(false);
    this.aBorrar.set(null);
  }
}
