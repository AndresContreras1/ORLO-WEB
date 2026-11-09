# ORLO Web (frontend)

Página pública de ORLO (`/`) y panel de administración (`/admin`) para que el equipo gestione el contenido de la app sin tocar código.

Angular 19 (componentes standalone) · TypeScript · RxJS · Bootstrap 5.3 con SCSS · Bootstrap Icons.

## Cómo levantarlo

Requisitos: Node LTS y el backend corriendo en `http://localhost:8090` (ver `../backend/README.md`).

```bash
npm install
npx ng serve
```

Abre `http://localhost:4200`. El panel está en `http://localhost:4200/login` (en desarrollo: `admin@orlo.co` / `OrloAdmin2026`).

Compilar para producción:

```bash
npx ng build
```

Antes de desplegar, cambia `apiUrl` en `src/environments/environment.ts` por la URL HTTPS del backend. El resultado queda en `dist/frontend/browser` (sitio estático). Nada secreto va en el frontend: todo lo del bundle es público.

## Qué tiene el panel

| Sección | Qué se hace |
|---|---|
| Resumen | Registros, premium y conversión, cuestionarios, mensajes al asesor y costo de la IA |
| Estilos | Crear, editar y borrar estilos con paleta (selector de color) y galería |
| Cuestionario | Preguntas con sus opciones y la **tabla de puntos** por estilo (0 a 10) |
| Lecciones | Contenido, ejemplos con imagen, pregunta de repaso, muestra gratis y publicación |
| Tiendas | Catálogo manual de tiendas, activas o no, y si el enlace es de afiliado |
| Asesor IA | Instrucciones y tono, modelo, mensajes por día y largo máximo de respuesta |
| Usuarios | Búsqueda por correo, paginación y activar o quitar premium |

## Estructura

```
src/
├── app/
│   ├── models/         Interfaces con los mismos nombres de campo que los DTO del backend
│   ├── services/       Un servicio por recurso; devuelven Observable y usan environment.apiUrl
│   ├── interceptors/   authInterceptor (token Bearer solo a nuestra API) y errorInterceptor (401 → login)
│   ├── guards/         authGuard: sin token vigente no se entra a /admin
│   ├── components/     Compartidos: layout del panel, diálogo de confirmación, errores de campo, carga
│   ├── pages/          Una carpeta por página; crear y editar usan el mismo formulario (/new y /:id)
│   ├── utils/          Mensajes de error en palabras simples
│   ├── app.routes.ts   Rutas (las específicas antes de las que llevan :id)
│   └── app.config.ts   HttpClient, router e idioma es-CO
├── environments/       apiUrl de desarrollo y de producción
├── styles/_theme.scss  Variables del diseño
└── styles.scss         Tema → Bootstrap → estilos globales
```

- Formularios reactivos con `Validators`; los errores aparecen después de tocar el campo.
- Estado de la vista con `signal`; peticiones independientes en paralelo con `forkJoin`; búsqueda con `debounceTime`.
- La lista se actualiza con lo que devuelve el backend, no con lo que había en el formulario.
- Cada página muestra "Cargando…" y, si algo falla, un mensaje simple con botón **Reintentar**.

## Cambiar el diseño

Todo el diseño sale de `src/styles/_theme.scss`: color de acento, fondo, texto, tipografía y bordes. Esas variables van **antes** de importar Bootstrap, así que cambian botones, formularios, tablas y tarjetas de todo el sitio. Para cambiar la tipografía, cambia también el enlace de Google Fonts en `src/index.html`.

## Agregar una sección al panel

1. Modelo en `models/` con los mismos campos que el DTO del backend.
2. Servicio en `services/` (`ng g service services/nombre`).
3. Página de lista y de formulario en `pages/` (`ng g c pages/nombre`).
4. Rutas en `app.routes.ts`, dentro de `admin`.
5. Entrada del menú en `components/admin-layout/admin-layout.component.ts`.
