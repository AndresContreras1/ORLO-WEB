// Producción (ng build).
// - apiUrl: la URL del servicio orlo-api en Render (si Render le agregó un sufijo al nombre, cópiala del panel).
// - firebase: la configuración web del proyecto real orlo-app-7b8c4 (consola de Firebase → Configuración del
//   proyecto → Tus apps → ORLO Web). No es secreta: la seguridad está en las reglas de Firestore y Storage.
// El build de Render se detiene si encuentra en este archivo el marcador de ejemplo (render.yaml),
// así que ese marcador no debe aparecer ni en los comentarios.
export const environment = {
  production: true,
  apiUrl: 'https://orlo-api.onrender.com/api/v1',
  firebase: {
    apiKey: 'AIzaSyBfYnroN1WbQxOwU4ZM7Okh2RQ-MVeZWxs',
    authDomain: 'orlo-app-7b8c4.firebaseapp.com',
    projectId: 'orlo-app-7b8c4',
    storageBucket: 'orlo-app-7b8c4.firebasestorage.app',
    appId: '1:65369091593:web:7e2b3d3452ec40bd0b12d8',
  },
  emulators: null as { host: string; auth: number; firestore: number; storage: number } | null,
};
