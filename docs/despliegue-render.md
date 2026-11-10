# Despliegue en Render

El repositorio ya trae todo lo necesario:

- `render.yaml` crea la API (`orlo-api`), la web (`orlo-web`) y la base de datos (`orlo-db`).
- `backend/Dockerfile` construye la API.
- La regla de rutas de Angular y el CI (`.github/workflows/ci.yml`) también están listos.

**Nada se despliega solo** (`autoDeployTrigger: off`). Después de cada corrección mergeada a `main` se entra a Render y se usa **Manual Deploy**.

```
Navegador ──► orlo-web (sitio estático, Angular)
   │               │ HTTPS + ID token de Firebase / JWT del panel
   │               ▼
   │          orlo-api (Docker, Spring Boot, perfil prod) ──► orlo-db (PostgreSQL)
   │               │ Firebase Admin (valida tokens, borra cuentas, FCM)
   ▼               ▼
Firebase real: Authentication · Firestore · Storage · Cloud Functions
```

## 1. Proyecto real de Firebase (una sola vez)

1. En https://console.firebase.google.com, crea el proyecto (por ejemplo `orlo-app`). Anota su **ID**.
2. Activa el plan **Blaze** (pago por uso). Storage en proyectos nuevos y Cloud Functions lo exigen. Con pocas personas se queda dentro de la cuota gratis; pon una alerta de presupuesto de 1 USD en Google Cloud.
3. Activa lo que usa la app:
   - **Authentication**: proveedor *Correo electrónico/contraseña*.
   - **Firestore Database**: modo producción.
   - **Storage**.
4. En *Authentication → Configuración → Dominios autorizados*, agrega `orlo-web.onrender.com`.
5. Crea la app web (*Configuración del proyecto → Tus apps → Web*). Copia su configuración en `frontend/src/environments/environment.ts`: `apiKey`, `authDomain`, `projectId`, `storageBucket` y `appId`. No es secreta.
6. Descarga la cuenta de servicio (*Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada*). Ese JSON **sí es secreto**: nunca va al repositorio.
7. Publica las reglas, los índices y las Cloud Functions:
   ```bash
   cd firebase/functions && npm install && cd ..
   firebase deploy --only firestore,storage,functions --project <id-del-proyecto>
   ```
8. App Android: descarga el `google-services.json` del proyecto real y reemplaza el de demo, en ORLO-MOVIL.

## 2. Ajustar el repositorio

- `frontend/src/environments/environment.ts`:
  - La configuración de Firebase del paso 1.5.
  - `apiUrl`, que ya apunta a `https://orlo-api.onrender.com/api/v1`. Cámbialo si Render le pone otro nombre.
  - El build de Render falla a propósito mientras quede algún `CAMBIAR`.
- Haz commit en una rama, abre el PR y mergéalo a `main`.

## 3. Crear los servicios en Render (una sola vez)

1. En https://dashboard.render.com entra con GitHub: **New → Blueprint** → repositorio `ORLO-WEB` → rama `main`.
2. Render lee `render.yaml` y pide los valores marcados con `sync: false`:

   | Variable | Valor |
   |---|---|
   | `ORLO_ADMIN_PASSWORD` | Contraseña del admin del panel (`admin@orlo.co`). Una nueva, no la de desarrollo |
   | `ORLO_ANTHROPIC_API_KEY` | Clave de Anthropic para el asesor |
   | `ORLO_FIREBASE_PROJECT_ID` | ID del proyecto de Firebase |

   `ORLO_JWT_SECRET` la genera Render. La conexión a la base la toma de `orlo-db`.
3. En `orlo-api` → **Environment → Secret Files**, agrega un archivo:
   - Nombre: `firebase-service-account.json`.
   - Contenido: el JSON del paso 1.6.
   - Queda en `/etc/secrets/firebase-service-account.json` (`GOOGLE_APPLICATION_CREDENTIALS`).
4. Si Render agregó un sufijo a los nombres (por ejemplo `orlo-web-x1y2.onrender.com`), corrige dos cosas: `ORLO_CORS_ORIGINS` en `orlo-api` y `apiUrl` en `environment.ts`.

## 4. Desplegar (cada vez que quieras publicar)

1. Mergea a `main` las correcciones. El CI corre las pruebas del backend y el build del frontend en el PR.
2. En Render, ejecuta **Manual Deploy → Deploy latest commit** en `orlo-api`. Espera a que el estado sea *Live*: el chequeo es `GET /actuator/health`.
3. Haz lo mismo en `orlo-web`.
4. Prueba en `https://orlo-web.onrender.com`:
   - Registro y cuestionario.
   - El panel en `/login`.
   - Recarga una ruta interna, como `/armario`: no debe dar 404.

En el primer arranque Flyway crea el esquema y `DataLoader` carga el contenido de ejemplo: estilos, cuestionario, lecciones, tiendas y catálogo de Explora.

### Datos de prueba (opcional)

Para que las personas encuentren cuentas con ropa ya cargada:

```bash
cd firebase/seed
ORLO_SEED_DESTINO=real ORLO_FIREBASE_PROJECT_ID=<id> ORLO_FIREBASE_WEB_API_KEY=<apiKey> \
ORLO_API_URL=https://orlo-api.onrender.com/api/v1 GOOGLE_APPLICATION_CREDENTIALS=<ruta al JSON> npm run seed
```

## Límites del plan gratis

- **API**: se duerme tras 15 minutos sin uso. La primera petición tarda unos 30–60 s en despertarla, y la web muestra "No pudimos conectar con el servidor" si se agota la espera. Tiene 512 MB de RAM: la JVM usa el 75 % (`-XX:MaxRAMPercentage=75`).
- **Base de datos gratis**:
  - **Vence a los 30 días.** Hay 14 días de gracia y después Render la borra.
  - No tiene copias de seguridad.
  - Para algo más largo, crea una base en Neon o Supabase (gratis) y pon su URL en `DATABASE_URL`, en formato `postgresql://` o JDBC, junto con `DB_USER` y `DB_PASSWORD`.
- **Sitio estático**: gratis y sin dormirse.
