import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import localeEsCo from '@angular/common/locales/es-CO';
import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { initializeApp, provideFirebaseApp } from '@angular/fire/app';
import { connectAuthEmulator, getAuth, provideAuth } from '@angular/fire/auth';
import { connectFirestoreEmulator, getFirestore, provideFirestore } from '@angular/fire/firestore';
import { connectStorageEmulator, getStorage, provideStorage } from '@angular/fire/storage';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';

import { environment } from '../environments/environment';
import { routes } from './app.routes';
import { appAuthInterceptor } from './interceptors/app-auth.interceptor';
import { authInterceptor } from './interceptors/auth.interceptor';
import { errorInterceptor } from './interceptors/error.interceptor';

// Fechas y números en español de Colombia
registerLocaleData(localeEsCo);

const emu = environment.emulators;

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding(), withInMemoryScrolling({ scrollPositionRestoration: 'top' })),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor, appAuthInterceptor, errorInterceptor])),
    { provide: LOCALE_ID, useValue: 'es-CO' },
    // Firebase (externo al vault): la misma cuenta y los mismos datos que la app móvil
    provideFirebaseApp(() => initializeApp(environment.firebase)),
    provideAuth(() => {
      const auth = getAuth();
      if (emu) {
        connectAuthEmulator(auth, `http://${emu.host}:${emu.auth}`, { disableWarnings: true });
      }
      return auth;
    }),
    provideFirestore(() => {
      const db = getFirestore();
      if (emu) {
        connectFirestoreEmulator(db, emu.host, emu.firestore);
      }
      return db;
    }),
    provideStorage(() => {
      const storage = getStorage();
      if (emu) {
        connectStorageEmulator(storage, emu.host, emu.storage);
      }
      return storage;
    }),
  ],
};
