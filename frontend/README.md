# ORLO Web (frontend)

Una sola aplicación Angular con dos partes:

- **Web de usuarios** (`/`): la misma ORLO de la app móvil, pensada para computador y adaptable al celular. Usa la **misma cuenta de Firebase** (Authentication, Firestore y Storage), así que lo que se hace en la web aparece en la app y al revés.
- **Panel de administración** (`/admin`): el equipo gestiona el contenido sin tocar código, con su propio usuario y JWT del backend.

Angular 19 (componentes standalone) · TypeScript · RxJS · signals · Bootstrap 5.3 con SCSS · Bootstrap Icons · `@angular/fire` 19 (externo al curso: es la librería oficial de Firebase para Angular).

## Cómo levantarlo

Requisitos: Node LTS, Firebase CLI y el backend corriendo en `http://localhost:8090` (ver `../backend/README.md`).

1. Emuladores de Firebase (en `../firebase`, ver su README): Auth 9099, Firestore 8080, Storage 9199, Functions 5001.
2. Datos de prueba con ropa real: `cd ../firebase/seed && npm install && npm run seed`.
3. La web:

```bash
npm install
npx ng serve
```

Abre `http://localhost:4200`.

| Parte | Cómo entrar |
|---|---|
| Web de usuarios | `http://localhost:4200/entrar` con `andres.prueba@orlo.test` / `Prueba2026` (o `laura.prueba@orlo.test`) |
| Panel | `http://localhost:4200/login` con `admin@orlo.co` y la contraseña de `backend/.env` (`ORLO_ADMIN_PASSWORD`) |

En desarrollo (`environment.development.ts`) la web se conecta a los emuladores con el proyecto `demo-orlo`, sin credenciales reales. Con `ORLO_PREMIUM_PARA_TODOS=true` en el backend (valor por defecto en dev) todo está desbloqueado; no hay cobro real.

Compilar para producción:

```bash
npx ng build
```

Antes de desplegar, cambia en `src/environments/environment.ts` el `apiUrl` por la URL HTTPS del backend y la configuración `firebase` por la del proyecto real (la de la app web en la consola de Firebase). Esa configuración no es secreta: la protegen las reglas de Firestore y Storage. El resultado queda en `dist/frontend/browser` (sitio estático).

## Qué tiene la web de usuarios

| Sección | Qué se hace |
|---|---|
| Bienvenida | Página pública con Empezar (registro) y Ya tengo cuenta |
| Primeros pasos | Registro con aceptación de términos, perfil inicial (datos de apariencia solo con consentimiento), cuestionario (o saltarlo eligiendo el estilo principal y uno secundario de la lista), resultado y oferta Premium |
| Inicio | Saludo, outfit del día (máximo 3 al día), avance de Aprende y accesos |
| Aprende | Lecciones con su avance en tiempo real; colorimetría con la paleta de la persona |
| Asesor | Chat con la IA que guarda las conversaciones en Firestore; acciones rápidas y guardar outfits |
| Explora | Prendas para tu estilo, completar el armario, tendencias, complementos, guardados y tiendas |
| Mi armario | Prendas con foto (archivo o cámara del navegador), filtros y favoritas; outfits; mi estilo; historial |
| Cuenta | Foto de perfil, preferencias, descargar mis datos (JSON), eliminar la cuenta y cerrar sesión |

## Qué tiene el panel

| Sección | Qué se hace |
|---|---|
| Resumen | Registros, premium y conversión, cuestionarios, mensajes al asesor y costo de la IA |
| Estilos | Crear, editar y borrar estilos con paleta (selector de color) y galería |
| Cuestionario | Preguntas con sus opciones y la **tabla de puntos** por estilo (0 a 10) |
| Lecciones | Contenido, ejemplos con imagen, pregunta de repaso, muestra gratis y publicación (al publicar se envía una notificación) |
| Explora | Catálogo de prendas de ejemplo: estilos, tendencia, complemento, ideas para combinar y crédito de la imagen |
| Tiendas | Catálogo manual de tiendas con las categorías que venden, activas o no, y si el enlace es de afiliado |
| Asesor IA | Instrucciones y tono, modelo, mensajes por día y largo máximo de respuesta |
| Usuarios | Búsqueda por correo, paginación y activar o quitar premium |

## Estructura

```
src/
├── app/
│   ├── models/         Interfaces del backend, documentos de Firestore (docs/modelo-de-datos.md) y opciones del perfil
│   ├── services/       Panel: un servicio por recurso. services/app/: Firebase (sesión, perfil, armario, conversaciones) y API
│   ├── interceptors/   authInterceptor (JWT del panel), appAuthInterceptor (ID token de Firebase) y errorInterceptor
│   ├── guards/         authGuard (panel); usuarioGuard, invitadoGuard y recorridoGuard (web de usuarios)
│   ├── components/     Layouts (panel, primeros pasos, usuario), diálogo, chips, colores, cámara, premium
│   ├── pages/          Panel en pages/; web de usuarios en pages/app/
│   ├── utils/          Mensajes de error y compresión de fotos
│   ├── app.routes.ts   Rutas (las específicas antes de las que llevan :id)
│   └── app.config.ts   HttpClient, router, idioma es-CO y Firebase (con emuladores en desarrollo)
├── environments/       apiUrl, configuración de Firebase y emuladores
├── styles/_theme.scss  Variables del diseño
└── styles.scss         Tema → Bootstrap → estilos globales
```

- Formularios reactivos con `Validators`; los errores aparecen después de tocar el campo.
- Estado de la vista con `signal`; los datos de Firestore llegan en tiempo real (`collectionData`/`docData`) y se pasan a signals con `toSignal`.
- Las fotos se reducen en el navegador (1280 px, JPEG) antes de subirlas a Storage.
- Cada página muestra "Cargando…" y, si algo falla, un mensaje simple con botón **Reintentar**.
- Registro:
  - Contraseña de 8 a 128 caracteres con confirmación.
  - Aviso si el dominio del correo parece mal escrito (`utils/correo.ts`: `gmil.com` → `gmail.com`), también al fallar el login.
- Firebase Auth se inicia con `initializeAuth` sin popup ni redirect (solo correo y contraseña), así la CSP no tiene que permitir iframes ni scripts de Google.
- En Render, `render.yaml` agrega CSP, `X-Frame-Options`, `Referrer-Policy` y `Permissions-Policy`. El build no incrusta CSS crítico con `onload`, porque la CSP lo bloquearía. Si la web llama a un dominio nuevo, hay que agregarlo en `connect-src`.

## Cambiar el diseño

Todo el diseño sale de `src/styles/_theme.scss`: color de acento, fondo, texto, tipografía y bordes. Esas variables van **antes** de importar Bootstrap, así que cambian botones, formularios, tablas y tarjetas de todo el sitio. Para cambiar la tipografía, cambia también el enlace de Google Fonts en `src/index.html`.

## Agregar una sección al panel

1. Modelo en `models/` con los mismos campos que el DTO del backend.
2. Servicio en `services/` (`ng g service services/nombre`).
3. Página de lista y de formulario en `pages/` (`ng g c pages/nombre`).
4. Rutas en `app.routes.ts`, dentro de `admin`.
5. Entrada del menú en `components/admin-layout/admin-layout.component.ts`.
