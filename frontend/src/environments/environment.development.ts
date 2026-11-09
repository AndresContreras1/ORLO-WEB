// Desarrollo (ng serve): backend local y emuladores de Firebase con el proyecto de prueba demo-orlo,
// el mismo que usa la app móvil. Así la cuenta y los datos son los mismos en el celular y en la web.
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8090/api/v1',
  firebase: {
    apiKey: 'demo-api-key-solo-para-el-emulador',
    authDomain: 'demo-orlo.firebaseapp.com',
    projectId: 'demo-orlo',
    storageBucket: 'demo-orlo.appspot.com',
    appId: '1:000000000000:web:0000000000000000',
  },
  emulators: {
    host: 'localhost',
    auth: 9099,
    firestore: 8080,
    storage: 9199,
  } as { host: string; auth: number; firestore: number; storage: number } | null,
};
