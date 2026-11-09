// Producción (ng build). Antes de desplegar: apiUrl con la URL HTTPS real del backend y la
// configuración web del proyecto real de Firebase (consola de Firebase → Configuración del proyecto).
// La configuración web de Firebase no es secreta: la seguridad está en las reglas.
export const environment = {
  production: true,
  apiUrl: 'https://api.orlo.co/api/v1',
  firebase: {
    apiKey: 'CAMBIAR',
    authDomain: 'CAMBIAR.firebaseapp.com',
    projectId: 'CAMBIAR',
    storageBucket: 'CAMBIAR.appspot.com',
    appId: 'CAMBIAR',
  },
  emulators: null as { host: string; auth: number; firestore: number; storage: number } | null,
};
