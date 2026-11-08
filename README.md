# GAMS Finanzas — Backend

API REST de **GAMS Finanzas**, una aplicación de finanzas personales para jóvenes y universitarios que
no llevan el control de sus gastos. Proyecto del curso **Desarrollo Full Stack** (UTP).

Frontend (Angular): <https://github.com/DiegoPorrasZela/GAMS-Finanzas>

## Tecnologías

- Java 21 y Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation)
- PostgreSQL (Neon en la nube) con migraciones Flyway
- Maven (incluye `mvnw`, no hace falta instalarlo)

## Arquitectura

Arquitectura en capas: cada capa solo conoce a la de abajo.

```
controller/   Endpoints REST: reciben y validan la petición, devuelven DTOs
service/      Lógica y reglas de negocio, transacciones
repository/   Acceso a datos con Spring Data JPA
entity/       Tablas de la base de datos mapeadas con JPA
dto/          Objetos que entran y salen del API (nunca se expone una entidad)
exception/    Errores del negocio y su conversión a respuestas JSON (ProblemDetail)
config/       Seguridad, CORS y datos iniciales
```

## Base de datos (3FN)

El esquema está en [`src/main/resources/db/migration`](src/main/resources/db/migration) y Flyway lo crea al iniciar.

| Tabla | Columnas |
|---|---|
| `rol` | id, nombre |
| `usuario` | id, nombre, email (único), password (BCrypt), rol_id → rol |
| `categoria` | id, nombre, tipo (`INGRESO`/`GASTO`); nombre + tipo es único |
| `movimiento` | id, usuario_id → usuario, categoria_id → categoria, descripcion, monto (> 0), fecha |

`movimiento` no guarda el tipo: lo toma de su categoría. Guardarlo en las dos tablas sería una dependencia
transitiva y rompería la Tercera Forma Normal.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/movimientos` | Movimientos del usuario, del más reciente al más antiguo |
| GET | `/api/movimientos/{id}` | Un movimiento del usuario |
| POST | `/api/movimientos` | Registrar un movimiento |
| PUT | `/api/movimientos/{id}` | Editar un movimiento |
| DELETE | `/api/movimientos/{id}` | Eliminar un movimiento |
| GET | `/api/categorias` | Listar categorías |
| POST | `/api/categorias` | Crear categoría |
| PUT | `/api/categorias/{id}` | Editar categoría (no se puede cambiar el tipo si ya tiene movimientos) |
| DELETE | `/api/categorias/{id}` | Eliminar categoría (solo si no tiene movimientos) |

Ejemplo de cuerpo para crear un movimiento:

```json
{ "descripcion": "Menú del almuerzo", "monto": 15.50, "fecha": "2026-11-08", "categoriaId": 6 }
```

Los errores se devuelven en formato [ProblemDetail](https://www.rfc-editor.org/rfc/rfc9457) con el código HTTP
correspondiente: `400` datos inválidos (con un mensaje por campo), `404` no existe y `409` regla de negocio.

## Cómo ejecutar

Requisitos: JDK 21 y una base de datos PostgreSQL (local o en [Neon](https://neon.tech)).

1. Copia `.env.example` como `.env` y completa los datos de conexión.
2. Ejecuta:

   ```bash
   ./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
   ```

3. El API queda en <http://localhost:8080/api>.

Al primer inicio se crean dos usuarios: `admin@gams.pe` / `admin123` (ADMIN) y `demo@gams.pe` / `demo123`
(USUARIO, con movimientos de ejemplo). En producción sus contraseñas se cambian con las variables
`ADMIN_PASSWORD` y `DEMO_PASSWORD`.
