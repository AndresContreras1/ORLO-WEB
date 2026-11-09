import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { Lesson } from '../../models/lesson.model';
import { LessonService } from '../../services/lesson.service';
import { errorMessage } from '../../utils/error-message';

@Component({
  selector: 'app-lessons',
  imports: [RouterLink, LoadStateComponent, ConfirmDialogComponent],
  templateUrl: './lessons.component.html',
})
export class LessonsComponent implements OnInit {
  private lessonService = inject(LessonService);

  lecciones = signal<Lesson[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);
  aBorrar = signal<Lesson | null>(null);
  borrando = signal(false);
  errorAccion = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.lessonService.findAll().subscribe({
      next: (data) => {
        this.lecciones.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  confirmarBorrado(): void {
    const leccion = this.aBorrar();
    if (!leccion) {
      return;
    }
    this.borrando.set(true);
    this.lessonService.delete(leccion.id).subscribe({
      next: () => {
        this.lecciones.update((lista) => lista.filter((l) => l.id !== leccion.id));
        this.cerrarDialogo();
      },
      error: (err) => {
        this.errorAccion.set(errorMessage(err));
        this.cerrarDialogo();
      },
    });
  }

  cerrarDialogo(): void {
    this.aBorrar.set(null);
    this.borrando.set(false);
  }
}
