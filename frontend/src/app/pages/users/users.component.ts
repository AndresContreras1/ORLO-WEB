import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { LoadStateComponent } from '../../components/load-state/load-state.component';
import { AppUser } from '../../models/app-user.model';
import { Page } from '../../models/page.model';
import { UserService } from '../../services/user.service';
import { errorMessage } from '../../utils/error-message';

/** Usuarios de la app. Premium se activa a mano mientras no hay cobro dentro de la app. */
@Component({
  selector: 'app-users',
  imports: [ReactiveFormsModule, DatePipe, LoadStateComponent],
  templateUrl: './users.component.html',
})
export class UsersComponent implements OnInit {
  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);

  readonly tamano = 20;
  buscar = new FormControl('', { nonNullable: true });
  pagina = signal<Page<AppUser> | null>(null);
  numeroPagina = signal(0);
  cargando = signal(true);
  error = signal<string | null>(null);
  cambiando = signal<string | null>(null);
  errorAccion = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
    // Buscar mientras se escribe, sin pedir en cada tecla
    this.buscar.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.numeroPagina.set(0);
        this.cargar();
      });
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.userService.list(this.numeroPagina(), this.tamano, this.buscar.value).subscribe({
      next: (page) => {
        this.pagina.set(page);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.cargando.set(false);
      },
    });
  }

  irA(numero: number): void {
    this.numeroPagina.set(numero);
    this.cargar();
  }

  alternarPremium(usuario: AppUser): void {
    this.cambiando.set(usuario.uid);
    this.errorAccion.set(null);
    this.userService.setPremium(usuario.uid, !usuario.premium).subscribe({
      next: (actualizado) => {
        // Se reemplaza el usuario por el que devuelve el backend (no se muta el arreglo)
        this.pagina.update((p) => p && {
          ...p,
          content: p.content.map((u) => (u.uid === actualizado.uid ? actualizado : u)),
        });
        this.cambiando.set(null);
      },
      error: (err) => {
        this.errorAccion.set(errorMessage(err));
        this.cambiando.set(null);
      },
    });
  }
}
