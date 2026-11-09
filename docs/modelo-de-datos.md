# Modelo de datos de ORLO

Contrato compartido por la **app móvil** (ORLO-MOVIL), la **web de usuarios** y el **backend** (ORLO-WEB).
Ningún cliente cambia una ruta, un nombre de campo o un código sin actualizar este documento.

- **Firebase** (Authentication, Firestore y Storage) guarda la cuenta y los datos de cada persona. Las reglas están en [`firebase/firestore.rules`](../firebase/firestore.rules) y [`firebase/storage.rules`](../firebase/storage.rules).
- **La API ORLO** (Spring Boot + PostgreSQL) sirve el contenido y decide lo que no puede quedar en el cliente: puntaje del cuestionario, asesor IA, límites, premium y métricas. Ver [`backend/README.md`](../backend/README.md) y Swagger.
- Fechas: milisegundos desde 1970 (`Long` en Kotlin, `number` en TypeScript).

## Firestore

Todo cuelga de `users/{uid}`, donde `uid` es el de Firebase Authentication. Cada persona solo lee y escribe lo suyo.

### `users/{uid}`

| Campo | Tipo | Quién lo escribe | Notas |
|---|---|---|---|
| `name` | string ≤ 40 | cliente | Obligatorio. Nunca se guarda correo ni contraseña |
| `ageRange` | código | cliente | `menos_18`, `18_24`, `25_34`, `35_44`, `45_59`, `60_mas` |
| `garmentPronoun` | código | cliente | `femenino`, `masculino`, `neutro` |
| `bodyType` | código | cliente | Solo con consentimiento: `rectangulo`, `triangulo`, `triangulo_invertido`, `reloj_de_arena`, `ovalo`, `prefiero_no_decir` |
| `skinTone` | código | cliente | Solo con consentimiento: `piel_muy_clara`, `piel_clara`, `piel_media`, `piel_morena`, `piel_oscura`, `piel_muy_oscura` |
| `hairColor` | código | cliente | Solo con consentimiento: `cabello_negro`, `cabello_castano_oscuro`, `cabello_castano_claro`, `cabello_rubio`, `cabello_rojizo`, `cabello_gris` |
| `eyeColor` | código | cliente | Solo con consentimiento: `ojos_negros`, `ojos_cafes`, `ojos_miel`, `ojos_verdes`, `ojos_azules`, `ojos_grises` |
| `climate` | código | cliente | `calido`, `templado`, `frio`, `variable` |
| `budget` | código | cliente | `bajo`, `medio`, `alto`, `prefiero_no_decir` |
| `occasions` | lista de códigos | cliente | `estudio`, `trabajo`, `salidas`, `casa`, `deporte`, `eventos` |
| `sensitiveDataConsent` | bool | cliente | Sin consentimiento, los cuatro datos de apariencia se guardan vacíos |
| `currentResult` | `Resultado` | cliente | Copia del último resultado (desnormalizado) |
| `photoUrl` | string | cliente | URL de `profileImages/{uid}.jpg` |
| `garmentsCount` | number | **Cloud Function** | Cuántas prendas tiene. Ningún cliente puede escribirlo |
| `acceptedTermsAt` | fecha | cliente | Al registrarse |
| `createdAt` | fecha | cliente | Al registrarse |

Los valores hexadecimales de las paletas de piel, cabello y ojos son los de `OpcionesDelPerfil.kt` en la app y `profile-options.ts` en la web.

`Resultado` = `{id, primary: Estilo, secondary: Estilo, createdAt}` · `Estilo` = `{id, code, name, description, palette: [hex], gallery: [url]}` (copia de `StyleDto` de la API).

### Subcolecciones de `users/{uid}`

| Ruta | Campos | Notas |
|---|---|---|
| `results/{id}` | `Resultado` | Historial del cuestionario. Al guardar uno se actualiza también `currentResult` (en un batch) |
| `lessonProgress/{lessonId}` | `lessonId` (number), `status`, `updatedAt` | `status`: `sin_empezar`, `en_curso`, `completada`. El id del documento es el id de la lección |
| `garments/{id}` | `name` (≤ 40), `category`, `color`, `photoUrl` (opcional), `createdAt`, `favorite` (bool, opcional) | Ver códigos abajo. Foto en `garmentImages/{uid}/{id}.jpg` |
| `outfits/{id}` | `name` (≤ 40), `occasion`, `garmentIds` (lista), `itemsText` (lista), `why`, `origin`, `favorite` (bool), `createdAt` | `occasion`: `universidad`, `trabajo`, `salida`, `otra`. `origin`: `asesor` o `manual` |
| `conversations/{id}` | `title`, `updatedAt` | Conversaciones del asesor |
| `conversations/{id}/messages/{id}` | `role`, `text`, `outfits` (`[{title, items, why}]`), `followUpQuestion` (opcional), `createdAt` | `role`: `user` o `assistant`. Al agregar un mensaje se actualiza `updatedAt` de la conversación |
| `saved/{itemId}` | `itemId` (number), `name`, `imageUrl`, `category`, `savedAt` | Guardados de Explora. El id del documento es el id de la prenda del catálogo |
| `dailyOutfits/{AAAA-MM-DD}` | `title`, `items` (lista), `why`, `createdAt` | Outfit del día. El id es la fecha local |

En `garments`, `outfits`, `conversations` y `messages` el id es el del documento: no se guarda como campo. Al leer se toma `doc.id` (en Angular, `idField: 'id'`).

### Códigos de prendas

| Lista | Códigos |
|---|---|
| Categorías | `arriba`, `abajo`, `abrigo`, `calzado`, `accesorio`, `otro` |
| Colores | `negro` #1F1F1F · `blanco` #F7F7F7 · `gris` #9E9E9E · `beige` #D9C3A5 · `cafe` #7A5230 · `azul` #3D5A80 · `celeste` #98C1D9 · `verde` #5C7F5A · `rojo` #C0392B · `rosado` #E8A0B4 · `amarillo` #E9C46A · `morado` #7B5EA7 · `estampado` #BDB2A7 |

El backend usa los mismos códigos en el catálogo de Explora y en las tiendas (`CatalogCodes.java`).

## Storage

| Ruta | Quién | Notas |
|---|---|---|
| `garmentImages/{uid}/{garmentId}.jpg` | dueño | Foto de una prenda. Imagen de menos de 2 MB (se comprime antes de subir) |
| `profileImages/{uid}.jpg` | dueño | Foto de perfil |

## Cloud Functions ([`firebase/functions`](../firebase/functions/index.js))

| Función | Cuándo | Qué hace |
|---|---|---|
| `contarPrendaCreada` | Se crea `garments/{id}` | `garmentsCount + 1` |
| `limpiarPrendaBorrada` | Se borra `garments/{id}` | Quita el id de los outfits que lo usaban, `garmentsCount − 1` y borra la foto |
| `limpiarUsuarioBorrado` | Se borra la cuenta de Authentication | Borra `users/{uid}` con todas sus subcolecciones y sus fotos |

## Notificaciones (FCM)

Tema `lecciones`. Al publicar una lección en el panel, el backend envía `notification: {title: "Nueva lección en ORLO", body: <título>}` y `data: {lessonId, title}`.

## API ORLO

Todas las rutas de la app llevan `Authorization: Bearer <ID token de Firebase>`. Errores en `application/problem+json` con propiedad `code`.

| Método y ruta | Qué hace |
|---|---|
| `GET /api/v1/me` · `DELETE /api/v1/me` | Premium y mensajes restantes · eliminar la cuenta (responde 200 `{"deleted": true}`) |
| `GET /api/v1/styles` | Estilos con paleta y galería |
| `GET /api/v1/quiz/active` · `POST /api/v1/quiz/score` | Cuestionario y puntaje |
| `GET /api/v1/lessons` · `GET /api/v1/lessons/{id}` | Lecciones |
| `POST /api/v1/advisor/messages` | Asesor IA (descuenta del límite diario) |
| `POST /api/v1/advisor/daily-outfit` | Outfit del día (no descuenta mensajes; máximo 3 al día) |
| `GET /api/v1/explore/items?section=FOR_YOUR_STYLE\|TRENDS\|ACCESSORIES&style={code}` | Catálogo de Explora |
| `GET /api/v1/explore/items/{id}` | Detalle con `combineIdeas: [{context, items}]` (`casual`, `universidad`, `elegante`) |
| `POST /api/v1/explore/complete-wardrobe` | `{primaryStyle?, wardrobe: [{category, color}]}` → tipos de prenda que darían más combinaciones |
| `GET /api/v1/stores?category={code}` | Tiendas sugeridas (si ninguna vende esa categoría, todas) |

| Código | Estado | Cuándo |
|---|---|---|
| `PREMIUM_REQUIRED` | 403 | Función premium y el usuario no lo es |
| `LIMIT_REACHED` | 429 | Se acabaron los mensajes (o los outfits del día) de hoy |
| `AI_UNAVAILABLE` | 503 | El modelo falló o no hay clave; el mensaje no se descuenta |
| `VALIDATION` | 400 | Datos inválidos; `errors: {campo: mensaje}` |
| `BUSINESS_RULE` | 409 | Regla de negocio (ej. opción de otra pregunta) |
| `NOT_FOUND` | 404 | No existe |
| `UNAUTHORIZED` | 401 | Sin token o token inválido |

Con `ORLO_PREMIUM_PARA_TODOS=true` (por defecto en desarrollo) `/me` responde `premium: true` a todos.
