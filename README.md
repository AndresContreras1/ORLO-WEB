# ORLO — Web

Backend y frontend web de ORLO, la app de asesoría de imagen personal.

| Carpeta | Qué contiene |
|---|---|
| `backend/` | API REST en Spring Boot con JPA y PostgreSQL. Sirve el contenido, el cuestionario, el asesor con IA, Explora y el panel de administración |
| `frontend/` | Una sola app Angular: la web de usuarios en `/`, con la misma cuenta de Firebase que la app móvil, y el panel de administración en `/admin` |
| `firebase/` | Reglas, índices, Cloud Functions y datos de prueba, compartidos con la app móvil |
| `docs/` | Modelo de datos común y guía de despliegue |

- Cómo levantar cada parte: `backend/README.md`, `frontend/README.md` y `firebase/README.md`.
- Despliegue en Render (`render.yaml`, sin despliegue automático): [`docs/despliegue-render.md`](docs/despliegue-render.md).
- CI en GitHub Actions: pruebas del backend y build del frontend en cada push y pull request.
- Seguridad: protecciones, pruebas de ataque y de carga, y riesgos pendientes en [`docs/seguridad.md`](docs/seguridad.md).
