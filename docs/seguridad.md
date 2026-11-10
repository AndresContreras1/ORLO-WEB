# Seguridad de ORLO

Qué protege a la API, a la web y a Firebase, cómo se probó y qué falta por hacer fuera del código.

## Protecciones

### API (Spring Boot)

| Tema | Cómo está |
|---|---|
| Identidad | Dos tipos de token. El panel usa un JWT propio: HS256, emisor `orlo-api`, vencimiento obligatorio de 30 minutos y sin datos sensibles. Se rechaza sin firma, con otra llave, vencido, sin `exp` o de otro emisor. La app usa el ID token de Firebase, que valida el Admin SDK (firma de Google, proyecto y vencimiento) |
| Permisos | `/api/v1/admin/**` exige rol `ADMIN` y `/api/v1/**` exige token de la app; lo demás es `denyAll`. Cada usuario solo toca lo suyo: el `uid` sale del token, nunca de la URL ni del cuerpo |
| Login del panel | Máximo 10 intentos por IP cada 5 minutos. Tras 5 fallos seguidos, la cuenta se bloquea 15 minutos, aunque el intento venga de otra IP. Responde 429 con `Retry-After`. El mensaje y el tiempo de respuesta son iguales si el correo no existe |
| Abuso | Máximo 120 peticiones por minuto por usuario (429 con `Retry-After`). Cuerpo de máximo 1 MB (413). Listas con tope: 50 respuestas del cuestionario, 80 prendas para el asesor |
| Entradas | Bean Validation en todos los DTO. Las imágenes y enlaces del panel solo aceptan `http(s)://` (nada de `javascript:` ni `data:`). JPQL con parámetros, sin concatenar |
| Errores | `application/problem+json` con un `code`, sin trazas, sin mensajes de la base y sin decir por qué falló un token |
| Cabeceras | CSP `default-src 'none'; frame-ancestors 'none'`, `nosniff`, `X-Frame-Options: DENY`, `Cache-Control: no-store` y `Referrer-Policy: no-referrer`. Con HTTPS, también HSTS |
| Superficie | CORS solo para el dominio de la web. De Actuator solo se expone `/actuator/health`. Swagger está apagado en producción |
| Concurrencia | El alta del usuario y el contador diario del asesor usan `INSERT … ON CONFLICT DO NOTHING`: dos peticiones simultáneas de un usuario nuevo ya no chocan (antes daban 500) |

Todo se configura en `application.properties` (`app.http.*`, `app.rate-limit.*`, `app.login.*`). Los contadores viven en memoria porque la API corre en una sola instancia.

### Web (Angular en Render)

- **Cabeceras** en `render.yaml`:
  - CSP: scripts solo propios; conexiones solo a la API y a `*.googleapis.com`; no se deja incrustar.
  - `X-Frame-Options: DENY`, `Referrer-Policy`, `Permissions-Policy` (la cámara solo para la propia web) y `Cross-Origin-Opener-Policy`.
  - El build no inyecta código en línea: el CSS crítico no se incrusta (`inlineCritical: false`).
- **Firebase Auth** se inicia con `initializeAuth` sin popup ni redirect, así que no carga iframes ni scripts de Google.
- **Contenido:** Angular escapa todo lo que muestra y no se usa `innerHTML`.
- **Registro:**
  - Contraseña de 8 a 128 caracteres, con confirmación.
  - Aviso cuando el dominio del correo parece mal escrito (`gmil.com` → `gmail.com`), también al fallar el login.
  - Firebase no deja crear dos cuentas con el mismo correo, sin importar mayúsculas.
- **Token del panel:** se guarda en `localStorage`. Un XSS podría leerlo; la CSP y el escape de Angular cierran esa puerta.

### Firebase

- **Firestore:** cada persona solo lee y escribe lo suyo, y cada documento acepta solo los campos del contrato, con tipo y tamaño máximo (ver [`modelo-de-datos.md`](modelo-de-datos.md#lo-que-validan-las-reglas)). `garmentsCount` solo lo escribe la Cloud Function.
- **Storage:** solo el dueño, solo JPEG, PNG o WebP de menos de 2 MB. SVG no, porque puede llevar scripts.

## Cómo se probó

| Prueba | Cómo correrla | Qué cubre |
|---|---|---|
| Backend | `./mvnw verify` (90 pruebas) | Tokens alterados, vencidos, sin `exp` o de otro emisor. Sexto login fallido → 429. Límite por usuario. Cuerpo de más de 1 MB → 413. Cabeceras. Enlaces `javascript:`. Listas enormes. Alta concurrente sin duplicados |
| Reglas de Firebase | `node --test pruebas/reglas.test.mjs` en `firebase/`, con los emuladores (31 pruebas) | Documentos válidos de la web y de la app. Ataques a Firestore y Storage |

**Ataques en local** contra la API, los emuladores y producción (solo lectura en producción): 108 casos, todos bloqueados después de las correcciones. Entre ellos:
- Evasión de rutas (`..`, `%2e`, `;`, `%00`, mayúsculas).
- Métodos HTTP no permitidos.
- JWT `alg: none`, firmado con otra llave, con el payload cambiado o sin vencimiento.
- Token de la app en el panel y token del panel en la app.
- Fuerza bruta, con y sin IP falsa en `X-Forwarded-For`.
- Enumeración de cuentas por mensaje y por tiempo.
- Inyección SQL y NoSQL.
- JSON roto, ids desbordados, cuerpos de 15 MB y listas de 50.000 elementos.
- CORS desde `evil.com`, `null` y subdominios engañosos.
- XSS por enlaces del panel.
- Peticiones simultáneas.
- Campos inventados y textos gigantes en Firestore.
- SVG y HTML disfrazados en Storage.
- Correos repetidos y contraseñas cortas en Firebase Auth.

**Carga** (usuarios virtuales con su propia cuenta y pausas como una persona navegando; se descarta el calentamiento de la JVM y cada medición se repitió):

| Máquina | Usuarios a la vez | Peticiones/s | Mitad responde en | 95 % responde en | Errores |
|---|---|---|---|---|---|
| Equipo de desarrollo, con 50 hilos y 5 conexiones como en prod | 100 | 156 | 32 ms | 79 ms | 0 % |
| Igual | 200 | 212 | 335 ms | 581 ms | 0 % |
| Docker limitado como el plan gratis de Render (0,1 CPU, 512 MB) | 5 | 2 | 1,8 s | 3,9 s | 0 % |
| Igual | 20 | 4 | 4,5 s | 9,1 s | 0 % |

- **Estrés:** 20 usuarios sin pausa (554 peticiones/s). El límite por usuario respondió 429 a lo que pasaba de 120 por minuto y la API siguió estable.
- **Arranque:** con 0,1 CPU la API tarda de 3 a 6 minutos en arrancar.
- **Conclusión:** en el plan gratis la API aguanta unas 4 peticiones por segundo sin errores. Para pruebas con pocas personas alcanza; para uso real conviene el plan Starter, que tiene más CPU.

## Riesgos que quedan

| Riesgo | Por qué | Cómo cerrarlo |
|---|---|---|
| El límite por IP se puede saltar con `X-Forwarded-For` | Spring toma la primera IP de esa cabecera | El bloqueo por cuenta sí aguanta (probado). Si hiciera falta, leer la IP que agrega el proxy de Render |
| Premium del armario | Firestore no sabe quién es premium; hoy lo decide la app | Cuando haya cobro real: custom claims de Firebase puestos por la API y revisados en las reglas |
| Cuotas del plan Spark | Un usuario con sesión podría gastar las escrituras gratis del día | Firebase App Check y plan Blaze con alerta de presupuesto |
| Cuentas creadas por fuera de la web | La API de Firebase permite registrarse sin pasar por el formulario (contraseña de 6, sin aceptar términos) | Política de contraseñas en la consola y App Check |
| Token de Firebase después de borrar la cuenta | Sigue sirviendo hasta que vence (1 hora) | `verifyIdToken(token, true)` en las rutas delicadas (cuesta una consulta a Google por petición) |
| Token del panel en `localStorage` | Un XSS lo podría leer | Cookie `HttpOnly` con protección CSRF |
| Contadores en memoria | Con más de una instancia cada una contaría por su lado | Redis o similar si se escala |

## Configuración fuera del código (consolas)

- **Firebase → Authentication → Configuración:**
  - Política de contraseñas: mínimo 8 caracteres.
  - Protección contra la enumeración de correos activada.
- **Google Cloud → Credenciales:** restringir la API key de la app web a los dominios `orlo-web.onrender.com` y `localhost`.
- **Render → `orlo-db` → Access Control:** quitar `0.0.0.0/0`. La API entra por la red interna.
- **Secretos expuestos:** si un secreto se vio fuera de Render (capturas, chats), cámbialo:
  - un `ORLO_JWT_SECRET` nuevo, que solo cierra la sesión del panel;
  - una clave nueva de la cuenta de servicio de Firebase.
