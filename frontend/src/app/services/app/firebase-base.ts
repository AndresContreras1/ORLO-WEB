import { EnvironmentInjector, inject, runInInjectionContext } from '@angular/core';

/**
 * Base de los servicios que usan Firebase. AngularFire pide que sus funciones corran en un
 * contexto de inyección para manejar bien la detección de cambios; `ctx` lo garantiza.
 */
export abstract class FirebaseBase {
  private readonly injector = inject(EnvironmentInjector);

  protected ctx<T>(fn: () => T): T {
    return runInInjectionContext(this.injector, fn);
  }
}
