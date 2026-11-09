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
3. Variables de entorno (todas tienen un valor de desarrollo, salvo la de la IA):

   | Variable | Para qué | Valor por defecto en `dev` |
   |---|---|---|
   | `ORLO_ANTHROPIC_API_KEY` | Clave del API de Anthropic para el asesor | — (sin ella el asesor responde 503) |
   | `ORLO_ADMIN_PASSWORD` | Contraseña del administrador inicial `admin@orlo.co` | `OrloAdmin2026` |
   | `ORLO_JWT_SECRET` | Secreto (Base64) de los tokens del panel | uno de desarrollo |
   | `DATABASE_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL | `jdbc:postgresql://localhost:5433/orlo`, `orlo`, `orlo_dev` |
   | `ORLO_CORS_ORIGINS` | Orígenes permitidos (panel web) | `http://localhost:4200` |
   | `FIREBASE_AUTH_EMULATOR_HOST` | Emulador de Auth | `localhost:9099` |

4. Arrancar:
   ```bash
   ./mvnw spring-boot:run
   ```
   Queda en `http://localhost:8090`. La documentación interactiva está en `http://localhost:8090/swagger-ui.html`.

Al arrancar, Flyway crea el esquema y `DataLoader` carga datos de ejemplo una sola vez: 8 estilos con paleta, un cuestionario de 10 preguntas con su tabla de puntos, 6 lecciones (la primera es la muestra gratis), 5 tiendas, la configuración del asesor y el administrador.

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
