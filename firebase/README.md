# Firebase de ORLO

Configuración compartida por la **app móvil**, la **web de usuarios** y el **backend**: una sola cuenta y los mismos datos en el celular y en el computador. El modelo de datos está en [`../docs/modelo-de-datos.md`](../docs/modelo-de-datos.md).

| Archivo | Qué es |
|---|---|
| `firebase.json` · `.firebaserc` | Emuladores y proyecto de prueba `demo-orlo` |
| `firestore.rules` · `storage.rules` | Reglas: cada persona solo lee y escribe lo suyo; lo que mantiene el servidor (`garmentsCount`) no lo escribe ningún cliente |
| `firestore.indexes.json` | Índices (hoy no se necesitan compuestos) |
| `functions/` | Cloud Functions de integridad (JavaScript, v2) |
| `seed/` | Carga de usuarios de prueba con ropa real |

## Emuladores

Requisitos: Node LTS, Java 17+ y Firebase CLI (`npm i -g firebase-tools`).

```bash
cd firebase/functions && npm install && cd ..
firebase emulators:start --project demo-orlo
```

| Emulador | Puerto |
|---|---|
| Authentication | 9099 |
| Firestore | 8080 |
| Storage | 9199 |
| Functions | 5001 |
| Interfaz | http://localhost:4000 |

Con `demo-orlo` no hace falta cuenta de Google: todo corre en tu PC. La app Android usa `10.0.2.2` para llegar a estos puertos; la web y el backend, `localhost`.

> **Windows:** si el emulador de Firestore se cierra con `Unable to establish loopback connection`, la carpeta temporal tiene el nombre corto del usuario (`C:\Users\USUARI~1\...`). Antes de arrancar, en PowerShell: `$env:JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=$PWD\.tmp"; mkdir .tmp -Force`.

## Datos de prueba

Con los emuladores **y el backend** encendidos (el seed le pide el cuestionario, las lecciones y el catálogo a la API):

```bash
cd firebase/seed
npm install
npm run seed
```

Crea dos cuentas para probar a mano y varias generadas con Faker. Contraseña de todas: `Prueba2026` (variable `ORLO_SEED_PASSWORD`).

| Cuenta | Perfil |
|---|---|
| `andres.prueba@orlo.test` | 18–24, clima templado, estudio y salidas; 12 prendas |
| `laura.prueba@orlo.test` | 25–34, clima cálido, trabajo y eventos, datos de colorimetría; 12 prendas |

Cada cuenta queda con perfil, resultado del cuestionario (calculado por la API), prendas reales con foto en Storage, outfits (manuales y del asesor), una conversación con el asesor, avance de Aprende, guardados de Explora y el outfit del día. Si una cuenta ya existe, se borra y se vuelve a crear.

Las fotos son de APIs públicas de datos de prueba (DummyJSON, Fake Store API) y de Wikimedia Commons (licencias Creative Commons, con su autor en el catálogo de Explora). Antes de publicar, los fundadores las cambian por fotos propias.

Para el proyecto real: `ORLO_SEED_DESTINO=real`, `ORLO_FIREBASE_PROJECT_ID`, `ORLO_FIREBASE_WEB_API_KEY`, `ORLO_API_URL` y credenciales de Google (`GOOGLE_APPLICATION_CREDENTIALS`).

## Cloud Functions

| Función | Cuándo | Qué hace |
|---|---|---|
| `contarPrendaCreada` | Se crea una prenda | Suma 1 a `users/{uid}.garmentsCount` |
| `limpiarPrendaBorrada` | Se borra una prenda | Quita su id de los outfits, resta 1 al contador y borra su foto |
| `limpiarUsuarioBorrado` | Se elimina la cuenta de Authentication | Borra `users/{uid}` con todas sus subcolecciones y sus fotos |

Así los datos quedan consistentes aunque la app o la web no alcancen a hacer todos los pasos. La función de cuenta borrada usa el disparador de Authentication de la API v1 de `firebase-functions`, porque la v2 no lo tiene.

## Publicar en el proyecto real

```bash
firebase use <id-del-proyecto>
firebase deploy --only firestore,storage,functions
```
