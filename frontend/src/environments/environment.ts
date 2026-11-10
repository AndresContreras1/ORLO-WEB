// Producción (ng build). Antes de desplegar:
// - apiUrl: la URL del servicio orlo-api en Render (si Render le agregó un sufijo al nombre, cópiala del panel).
// - firebase: la configuración web del proyecto real (consola de Firebase → Configuración del proyecto →
//   Tus apps → app web). Cópiala tal cual; no es secreta: la seguridad está en las reglas.
// El build de Render falla a propósito mientras quede algún CAMBIAR.
export const environment = {
  production: true,
  apiUrl: 'https://orlo-api.onrender.com/api/v1',
  firebase: {
    apiKey: 'CAMBIAR',
    authDomain: 'CAMBIAR.firebaseapp.com',
    projectId: 'CAMBIAR',
    storageBucket: 'CAMBIAR.firebasestorage.app',
    appId: 'CAMBIAR',
  },
  emulators: null as { host: string; auth: number; firestore: number; storage: number } | null,
};
