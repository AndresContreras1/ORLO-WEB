# ORLO API (backend)

API REST de ORLO. Sirve a la app móvil (contenido, cuestionario de estilo, lecciones, asesor con IA y estado premium) y al panel de administración.

Spring Boot 3.5 · Java 17+ · Spring Data JPA · PostgreSQL 16 · Flyway · Spring Security (JWT) · springdoc-openapi.

## Cómo levantarlo

Requisitos: Java 17 o superior, Docker y Firebase CLI (`npm i -g firebase-tools`).

1. Base de datos (PostgreSQL en Docker, puerto **5433** para no chocar con un PostgreSQL instalado en Windows):
   ```bash
   docker compose up -d
   ```
2. Emulador de Firebase Authentication (proyecto `demo-orlo`, puerto 9099). Si la app móvil ya levantó los emuladores, no hace falta:
   ```bash
   firebase emulators:start --only auth --project demo-orlo
   ```
3. Secretos de desarrollo: copia `.env.example` como `.env` (está en `.gitignore`) y llena `ORLO_JWT_SECRET` (`openssl rand -base64 32`) y `ORLO_ADMIN_PASSWORD`. Spring lo lee con `spring.config.import=optional:file:.env[.properties]`; sin esos dos valores el backend no arranca.

   Variables de entorno (las demás tienen un valor de desarrollo, salvo la de la IA):

   | Variable | Para qué | Valor por defecto en `dev` |
   |---|---|---|
   | `ORLO_ANTHROPIC_API_KEY` | Clave del API de Anthropic para el asesor | — (sin ella el asesor responde 503) |
   | `ORLO_ADMIN_PASSWORD` | Contraseña del administrador inicial `admin@orlo.co` | la de tu `.env` |
   | `ORLO_JWT_SECRET` | Secreto (Base64) de los tokens del panel | el de tu `.env` |
   | `DATABASE_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL | `jdbc:postgresql://localhost:5433/orlo`, `orlo`, `orlo_dev` |
   | `ORLO_CORS_ORIGINS` | Orígenes permitidos (panel web) | `http://localhost:4200` |
   | `FIREBASE_AUTH_EMULATOR_HOST` | Emulador de Auth | `localhost:9099` |
   | `ORLO_PREMIUM_PARA_TODOS` | Todos se tratan como premium (pruebas). Ponla en `false` para ver el bloqueo premium | `true` (en `prod`, `false`) |
   | `ORLO_FCM_ENABLED` | Enviar la notificación de lección nueva por FCM | `false` (en `prod`, `true`) |

4. Arrancar:
   ```bash
   ./mvnw spring-boot:run
   ```
   Queda en `http://localhost:8090`. La documentación interactiva está en `http://localhost:8090/swagger-ui.html`.

Al arrancar, Flyway crea el esquema y `DataLoader` carga datos de ejemplo una sola vez: 8 estilos con paleta, un cuestionario de 10 preguntas con su tabla de puntos, 6 lecciones (la primera es la muestra gratis), 5 tiendas con sus categorías, 47 prendas reales en el catálogo de Explora, la configuración del asesor y el administrador. Las galerías de los estilos, las opciones del cuestionario y las lecciones usan esas mismas fotos de ropa (`CatalogoDeEjemplo`). Para volver a cargar todo desde cero: `docker compose down -v` y arrancar de nuevo.

Endpoints principales (detalle en Swagger y en [`../docs/modelo-de-datos.md`](../docs/modelo-de-datos.md)):

| App (token de Firebase) | Panel (JWT de admin) |
|---|---|
| `GET/DELETE /api/v1/me` | `POST /api/v1/admin/auth/login` |
| `GET /api/v1/styles` | CRUD `/api/v1/admin/styles`, `/quiz`, `/lessons`, `/stores`, `/explore-items` |
| `GET /api/v1/quiz/active` · `POST /api/v1/quiz/score` | `GET/PUT /api/v1/admin/ai-config` |
| `GET /api/v1/lessons` · `GET /api/v1/lessons/{id}` | `GET /api/v1/admin/users` · `PUT /api/v1/admin/users/{uid}/premium` |
| `POST /api/v1/advisor/messages` · `POST /api/v1/advisor/daily-outfit` | `GET /api/v1/admin/metrics` |
| `GET /api/v1/explore/items` · `GET /api/v1/explore/items/{id}` · `POST /api/v1/explore/complete-wardrobe` | |
| `GET /api/v1/stores` | |

Al publicar una lección (crearla publicada o pasarla de oculta a publicada) se envía una notificación FCM al tema `lecciones`, después de guardar. En desarrollo FCM está apagado y solo queda en el log.

## Pruebas

```bash
./mvnw verify
```

Las pruebas usan el perfil `test` (H2 en memoria, sin Flyway) y no necesitan Docker ni Firebase:

| Carpeta | Qué prueba | Herramienta |
|---|---|---|
| `repository/` | Consultas propias | `@DataJpaTest` |
| `service/` | Reglas de negocio (puntaje del cuestionario, premium, límite del asesor) | Mockito |
| `controller/` | Contrato HTTP, validación, códigos de error | `@WebMvcTest` + MockMvc |
| `security/` | 401 sin token, 403 sin rol, tokens alterados, token de la app en el panel | `@WebMvcTest` |
| `arquitectura/` | Capas, controladores sin repositorios ni entidades | ArchUnit |

## Arquitectura

```
com.orlo.api
├── controller/        App: /api/v1/{styles,quiz,lessons,advisor,me}
│   └── admin/         Panel: /api/v1/admin/**
├── service/           Interfaces de negocio
│   └── impl/          Implementaciones (@Service, @Transactional)
├── repository/        Spring Data JPA
├── entities/          Entidades JPA
├── dto/               Records de entrada y salida (con from(entidad))
│   └── admin/
├── security/          SecurityConfig, filtros de token, 401/403 en ProblemDetail
├── exception/         Excepciones de negocio y GlobalExceptionHandler
├── client/            Servicios externos: Firebase Auth y modelo de IA
└── config/            Firebase, OpenAPI y datos iniciales
```

- Controller → Service → Repository. Los controladores no tocan repositorios ni entidades (lo verifica ArchUnit).
- Errores en `application/problem+json` con una propiedad `code` estable (`PREMIUM_REQUIRED`, `LIMIT_REACHED`, `VALIDATION`, `BUSINESS_RULE`, `NOT_FOUND`, `UNAUTHORIZED`, `FORBIDDEN`, `AI_UNAVAILABLE`).
- Sin sesión: el panel usa un JWT propio con rol `ADMIN`; la app envía el ID token de Firebase.
- El asesor descuenta el mensaje del límite diario **antes** de llamar al modelo y lo devuelve si el modelo falla. La llamada al modelo va fuera de cualquier transacción.

### Piezas externas

Además de lo estándar del stack, el backend usa:

- **Firebase Admin SDK** (`firebase-admin`): valida el ID token de la app dentro de un `OncePerRequestFilter` y borra la cuenta en `DELETE /api/v1/me`. En desarrollo apunta al emulador.
- **SDK de Anthropic para Java** (`anthropic-java`): el asesor llama al modelo `claude-haiku-5-5` con salida estructurada (JSON con `reply`, `outfits` y `followUpQuestion`). El modelo, el límite diario y las instrucciones se editan desde el panel.

### Nota para Windows

Java abre un socket local en la carpeta temporal. Si esa ruta usa el nombre corto del usuario (`C:\Users\USUARI~1\...`), el arranque falla con `Unable to establish loopback connection`. El `pom.xml` ya apunta esa carpeta a `target/`, tanto para `spring-boot:run` como para las pruebas.

## Producción

Perfil `prod` (`--spring.profiles.active=prod`): toda la configuración llega por variables de entorno, Swagger queda apagado y Firebase usa las credenciales de la cuenta de servicio en lugar del emulador.

- **Imagen Docker** (`Dockerfile`): compila el `.jar` con el Maven Wrapper y lo corre sobre `eclipse-temurin:17-jre` con el perfil `prod`. `docker-entrypoint.sh` convierte `DATABASE_URL` del formato `postgresql://` (el que entrega Render) al formato JDBC.
  ```bash
  docker build -t orlo-api .
  ```
- **Salud**: `GET /actuator/health` es público y responde `{"status":"UP"}`; el resto de Actuator no se expone.
- **Detrás del proxy del hosting**: `server.forward-headers-strategy=framework` respeta `X-Forwarded-*`.
- **Recursos en prod**: `THREADS` (hilos de Tomcat, 50) y `DB_POOL` (conexiones a la base, 5), pensados para el plan gratis.
- Despliegue en Render paso a paso: [`../docs/despliegue-render.md`](../docs/despliegue-render.md).
