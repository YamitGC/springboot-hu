# Eventify Semana 4

API REST desarrollada con **Spring Boot** para la gestión integral de **Eventos**, **Lugares (Venues)** y **Categorías**, complementada con un **panel administrativo web** construido con **Thymeleaf**. 

En esta cuarta entrega el proyecto evoluciona hacia un **modelo relacional avanzado**, incorporando relaciones **Many-to-One** y **Many-to-Many**, control de versiones y migraciones de base de datos con **Flyway**, arquitectura desacoplada mediante **DTOs / Records**, **borrado lógico (*Soft Delete*)**, optimización de rendimiento y mitigación del problema **N+1** mediante **Entity Graphs**, el patrón de consulta en **dos pasos** y paginación ligera con **Slice**.

---

## 🌟 Novedades principales de la Semana 4

1. **Evolución relacional del modelo de datos:**
   - Nueva entidad y tabla `Category` para clasificar eventos (`Conciertos`, `Tecnología`, `Talleres`, etc.).
   - Relación obligatoria **Many-to-One** entre `Event` y `Venue` (`events.venue_id`).
   - Relación **Many-to-Many** entre `Event` y `Category` gestionada mediante tabla intermedia `events_categories`.
   - Campo obligatorio `ciudad` en `Venue` con índices para búsquedas geográficas eficientes.

2. **Control de migraciones con Flyway:**
   - La base de datos deja de gestionarse con `ddl-auto=update`. Ahora el esquema lo versiona **Flyway** (`V1__estructura_inicial.sql`, `V2__evolucion_relacional.sql`, `V3__seed_datos.sql`).
   - Hibernate pasa a modo `validate`: solo comprueba que las entidades coincidan estrictamente con las tablas existentes.
   - Población de datos inicial masiva (**15 lugares y 200 eventos** con fechas y categorías coherentes).

3. **Arquitectura desacoplada con DTOs y Records:**
   - `EventRequest`: DTO para creación y edición donde lugar y categorías se referencian por ID.
   - `EventSummaryDTO`: `Record` aplanado para listados masivos de alto rendimiento.
   - `EventDetailDTO`: `Record` para vistas de detalle con lugar y lista de nombres de categorías.

4. **Borrado lógico (*Soft Delete*):**
   - La eliminación de eventos (`DELETE /api/events/{id}` y botón de eliminar en web) no destruye filas físicas: marca `activo = false`.
   - Uso de `@SQLRestriction("activo = true")` a nivel de entidad: Hibernate filtra de forma transparente los eventos inactivos en cualquier consulta.

5. **Optimización de consultas JPA y solución al problema N+1:**
   - Paginación mediante `Slice` en lugar de `Page`: elimina la costosa consulta adicional `COUNT(*)` sobre colecciones voluminosas.
   - Proyecciones DTO directas (`SELECT new EventSummaryDTO(...)`) con `JOIN`.
   - Carga con `@EntityGraph` para detalles completos en una sola consulta.
   - **Patrón de dos pasos (*Two-step fetch*)**: primero pagina entidades/IDs haciendo fetch únicamente del `Venue` (evita el warning de Hibernate `HHH000104` y duplicación en memoria), y luego recupera las colecciones `ManyToMany` en una sola consulta secundaria conservando el orden.
   - Motor de búsqueda multicriterio con normalización y escape de caracteres especiales (`%`, `_`, `\`) en sentencias `LIKE`.

6. **Auditoría y observabilidad SQL:**
   - Configuración de logging en `TRACE` (`org.hibernate.orm.jdbc.bind`) para inspeccionar en tiempo real los valores asignados a los parámetros en consola.

---

## 🏗️ Arquitectura del proyecto

El proyecto sigue una arquitectura en capas limpia y desacoplada:

```
Controller (API REST)  ─┐
                         ├─→  Service  →  Repository  →  Base de datos (PostgreSQL / Flyway)
Controller (Vistas Web) ─┘         ↓
                                  DTOs / Model (Entidades con JPA y validaciones)
                                   ↓
                              Exception (Manejo de errores centralizado)
```

### 📁 `model`
Entidades JPA que mapean el esquema relacional versionado:
- **`Event.java`**: evento con `id`, `nombre`, `fecha`, `descripcion`, bandera `activo` para borrado lógico (`@SQLRestriction`), relación `@ManyToOne` obligatoria hacia `Venue` y relación `@ManyToMany` hacia `Category` mediante la tabla intermedia `events_categories`.
- **`Venue.java`**: lugar físico con `id`, `nombre`, `direccion`, `ciudad` (nuevo) y `capacidad`.
- **`Category.java`**: clasificación temática con `id`, `nombre` único y `descripcion`.

### 📁 `dto`
Desacopla la representación interna de la base de datos de los contratos web y REST:
- **`EventRequest`**: objeto recibido en creación/actualización con validaciones Bean Validation (`@NotNull venueId`, `@NotEmpty categoryIds`).
- **`EventSummaryDTO`**: `record` inmutable optimizado para tablas y listados masivos.
- **`EventDetailDTO`**: `record` inmutable con el detalle exhaustivo del evento y sus relaciones.

### 📁 `repository`
Interfaces que extienden `JpaRepository` con consultas JPQL optimizadas:
- **`EventRepository`**:
  - `buscar(...)`: consulta JPQL combinada proyectada a `EventSummaryDTO` con soporte de `Slice`.
  - `findWithRelationsById(id)`: carga puntual mediante `@EntityGraph(attributePaths = {"venue", "categories"})`.
  - `findAllWithRelationsByIdIn(ids)`: segundo paso de carga masiva con `JOIN FETCH` y `LEFT JOIN FETCH` sin paginación en memoria.
  - Finders especializados: `findByVenueCiudad...`, `findByVenueCapacidad...`, `findByFechaBetween...`, y `findByCategoriaNombre...` (utilizando subconsulta `EXISTS` para evitar duplicidad).
- **`VenueRepository`**: CRUD y búsqueda paginada por nombre (`findByNombreContaining`).
- **`CategoryRepository`**: consulta ordenada `findAllByOrderByNombreAsc()`.

### 📁 `service`
Lógica de negocio, orquestación de transacciones y reglas de integridad:
- **`EventService`**: búsqueda multicriterio con valores centinela de fechas, normalización y escape `LIKE`, validaciones de negocio, borrado lógico (`softDelete`), y orquestación del patrón de dos pasos.
- **`VenueService`**: validación de datos requeridos (incluyendo `ciudad`), actualización y control de integridad referencial (captura `DataIntegrityViolationException` si se intenta eliminar un lugar con eventos).
- **`CategoryService`**: listado y validación de existencia para colecciones de identificadores.

### 📁 `controller`
Exposición dual del sistema:
- **Controladores REST** (`@RestController`, documentados con OpenAPI 3):
  - **`EventController`** (`/api/events`): creación con DTO, búsqueda multicriterio, endpoints de filtros individuales (`/por-ciudad`, `/por-categoria`, `/por-capacidad`, `/por-fechas`), detalle, edición y eliminación lógica (código HTTP 204).
  - **`VenueController`** (`/api/venues`): operaciones CRUD para lugares.
- **Controladores de Vistas** (`@Controller` con Thymeleaf):
  - **`EventViewController`** (`/admin/events`): listado con barra de filtros, navegación `Slice`, formulario con selector de lugar y checkboxes de categorías, edición y borrado lógico con patrón Post/Redirect/Get.
  - **`VenueViewController`** (`/admin/venues`): administración de lugares físicos incluyendo el nuevo campo de ciudad.

### 📁 `resources/db/migration`
Scripts SQL secuenciales ejecutados por Flyway:
- **`V1__estructura_inicial.sql`**: tablas base heredadas de la semana 3 (`venues` y `events`).
- **`V2__evolucion_relacional.sql`**: añade columna `ciudad` a `venues`, columna `activo` a `events`, crea la tabla `categories`, establece la clave foránea `events.venue_id`, crea la tabla intermedia `events_categories` e indexa columnas clave para búsquedas.
- **`V3__seed_datos.sql`**: carga masiva de los 15 lugares con ciudad y 200 eventos distribuidos con categorías coherentes.

---

## 🛠️ Tecnologías utilizadas

- **Java 21**
- **Spring Boot 4.1.1**
- **Spring Data JPA & Hibernate 7**
- **Flyway 11** (`flyway-database-postgresql`) — control de versiones del esquema
- **Spring Web MVC** (`spring-boot-starter-webmvc`)
- **Thymeleaf** (`spring-boot-starter-thymeleaf`) — motor de plantillas web
- **Bean Validation** (`spring-boot-starter-validation`) — validación declarativa
- **PostgreSQL 16** (base de datos principal y motor relacional)
- **H2 Database** (soporte para pruebas y desarrollo)
- **Lombok**
- **Springdoc OpenAPI 3.1.0** (Swagger UI en `/api/**`)
- **JUnit 5 & Mockito** (pruebas unitarias y de integración)
- **MockMvc** (`@SpringBootTest` y `@DataJpaTest`)
- **Maven** (con Maven Wrapper)

---

## ✅ Requisitos previos

- **JDK 21** o superior.
- **Docker** (para el contenedor de PostgreSQL).
- **DBeaver** (opcional, para visualización e inspección del esquema).

> No es necesario instalar Maven manualmente; se incluye el wrapper `./mvnw`.

---

## 🐳 Paso 1: Levantar la base de datos con Docker

La aplicación y los tests de integración esperan una base de datos PostgreSQL en el puerto `5433`:

| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `5433` |
| Base de datos principal | `eventify` |
| Base de datos de pruebas | `eventify_test` |
| Usuario | `eventify` |
| Contraseña | `eventify` |

Ejecuta el siguiente comando para iniciar el contenedor:

```bash
docker run --name eventify-db \
  -e POSTGRES_DB=eventify \
  -e POSTGRES_USER=eventify \
  -e POSTGRES_PASSWORD=eventify \
  -p 5433:5432 \
  -d postgres:16
```

Para verificar el estado del contenedor:
```bash
docker ps
```

Para detener o volver a iniciar el contenedor:
```bash
docker stop eventify-db
docker start eventify-db
```

> **Importante para las pruebas:** Las pruebas de integración (`@SpringBootTest` y `@DataJpaTest`) se conectan a `eventify_test`. Puedes crearla ejecutando dentro del contenedor:
> ```bash
> docker exec -it eventify-db psql -U eventify -c "CREATE DATABASE eventify_test;"
> ```

---

## 🔍 Paso 2: Flyway y validación del esquema con DBeaver

1. Abre **DBeaver** y crea una nueva conexión a **PostgreSQL**:
   - **Host:** `localhost` | **Port:** `5433` | **Database:** `eventify` | **User/Password:** `eventify` / `eventify`
2. Al iniciar la aplicación Spring Boot, **Flyway se ejecuta automáticamente** antes de que Hibernate interactúe con los datos:
   - Aplica `V1`, `V2` y `V3`.
   - Crea la tabla de control `flyway_schema_history`.
   - `spring.jpa.hibernate.ddl-auto=validate` valida que las clases Java coincidan exactamente con la base generada por las migraciones.
3. Podrás comprobar en DBeaver las tablas `events`, `venues`, `categories`, `events_categories` y los 200 eventos ya insertados con sus claves foráneas.

---

## ▶️ Paso 3: Ejecutar la aplicación

Sitúate en el directorio de la semana 4 y ejecuta:

**En Linux / macOS:**
```bash
cd eventify-semana04
./mvnw spring-boot:run
```

**En Windows:**
```cmd
cd eventify-semana04
mvnw.cmd spring-boot:run
```

Al arrancar la aplicación:
- Flyway valida y ejecuta las migraciones pendientes.
- Hibernate valida la estructura de las entidades.
- Las consultas SQL y los parámetros vinculados se visualizan en consola gracias al nivel `TRACE`.
- La aplicación quedará disponible en: `http://localhost:8080`.

---

## 🖥️ Panel administrativo web (Thymeleaf)

Accede desde tu navegador a los módulos de administración:

```
http://localhost:8080/admin/events
http://localhost:8080/admin/venues
```

### Funcionalidades del panel web

| Acción | Ruta | Descripción |
|---|---|---|
| **Catálogo con filtros** | `GET /admin/events` | Listado paginado con barra de búsqueda por ciudad, categoría, capacidad mínima y rango de fechas. |
| **Paginación Slice** | `GET /admin/events?page=X` | Navegación Anterior/Siguiente conservando los filtros activos en la URL. |
| **Detalle de evento** | `GET /admin/events/{id}` | Muestra información del evento, lugar, ciudad y etiquetas (*badges*) de categorías. |
| **Nuevo evento** | `GET /admin/events/new` | Formulario con selector desplegable de lugares (`<select>`) y casillas de selección múltiple de categorías (`<input type="checkbox">`). |
| **Guardar evento** | `POST /admin/events` | Valida datos con Bean Validation; si hay errores, preserva los campos y las opciones seleccionadas. |
| **Editar evento** | `GET /admin/events/{id}/edit` | Carga el formulario precargado con el lugar y las categorías asignadas. |
| **Actualizar evento** | `POST /admin/events/{id}/edit` | Procesa la edición y redirige con mensaje flash de éxito. |
| **Borrado lógico** | `POST /admin/events/{id}/delete` | Desactiva el evento; desaparece de inmediato de la lista sin romper historial en BD. |
| **Gestión de lugares** | `/admin/venues` | CRUD completo de lugares incluyendo la gestión del campo `ciudad`. |

---

## 📑 Documentación de la API REST (Swagger / OpenAPI)

Explora y ejecuta las solicitudes interactivas en Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

> La documentación Swagger abarca exclusivamente las rutas `/api/**`.

### Endpoints principales de Eventos (`/api/events`)

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/events` | Registra un evento recibiendo `EventRequest` con `venueId` y lista de `categoryIds`. Retorna `201 Created` con `EventDetailDTO`. |
| `GET` | `/api/events` | **Búsqueda multicriterio:** filtros combinados opcionales (`ciudad`, `categoria`, `capacidadMin`, `desde`, `hasta`) con orden fijo `fecha DESC` y paginación por `Slice`. |
| `GET` | `/api/events/por-ciudad` | Finder por coincidencia parcial e insensible a mayúsculas sobre la ciudad del lugar. |
| `GET` | `/api/events/por-categoria` | Finder por categoría evitando duplicidad de eventos con subconsulta `EXISTS`. |
| `GET` | `/api/events/por-capacidad` | Finder por capacidad mínima del lugar asociado. |
| `GET` | `/api/events/por-fechas` | Finder por rango de fechas (`desde` y `hasta`). |
| `GET` | `/api/events/{id}` | Retorna `EventDetailDTO`. Si el evento no existe o está inactivo (`activo=false`), responde `404 Not Found`. |
| `PUT` | `/api/events/{id}` | Actualiza datos y relaciones de un evento activo. |
| `DELETE` | `/api/events/{id}` | **Borrado lógico:** marca `activo=false` y responde `204 No Content`. |

### Endpoints de Lugares (`/api/venues`)

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/venues` | Crear lugar (`nombre`, `direccion`, `ciudad`, `capacidad`). |
| `GET` | `/api/venues` | Listar lugares (paginado por `Page`). |
| `GET` | `/api/venues/{id}` | Consultar lugar por ID. |
| `PUT` | `/api/venues/{id}` | Actualizar datos y ciudad del lugar. |
| `DELETE` | `/api/venues/{id}` | Eliminar lugar (protegido contra violación de integridad si posee eventos). |

---

## 🚀 Estrategias de rendimiento y optimización JPA

En esta entrega se aplicaron patrones específicos para resolver los problemas de rendimiento comunes en aplicaciones empresariales:

1. **Evitar el problema N+1 en listados:**
   - Para el catálogo principal se emplea **proyección a constructor DTO** (`new EventSummaryDTO(...)`) con `JOIN e.venue v`, trayendo exactamente las columnas necesarias en **1 única consulta SQL**.
2. **Patrón de dos pasos (*Two-step fetch*) para paginación segura con colecciones:**
   - Hacer un `JOIN FETCH` sobre colecciones `ManyToMany` en una consulta paginada provoca que Hibernate aplique la paginación en memoria emitiendo la advertencia `HHH000104`.
   - Se resolvió con dos pasos:
     1. Se pagina la consulta haciendo fetch solo de la relación `@ManyToOne` hacia `Venue`.
     2. Con la lista de IDs de la página actual, se dispara una única consulta con `JOIN FETCH e.venue LEFT JOIN FETCH e.categories WHERE e.id IN :ids` para cargar las colecciones de forma limpia.
3. **Paginación con `Slice`:**
   - En catálogos grandes, calcular el total de páginas (`SELECT count(*)`) añade una penalización de tiempo innecesaria. `Slice` solicita únicamente `size + 1` registros para determinar si existe una página siguiente sin contar todo el universo de filas.

---

## 🧪 Ejecución de pruebas

El proyecto cuenta con una suite completa de pruebas unitarias y de integración:

```bash
cd eventify-semana04
./mvnw test
```

### Cobertura de pruebas (38 pruebas en total):
- **Unitarias (`service` con Mockito):**
  - `EventServiceTest`: validación de campos obligatorios, asignación de relaciones, formateo y escape `LIKE`, ordenamiento y borrado lógico.
  - `VenueServiceTest`: validación de nombre, capacidad, ciudad obligatoria y reglas de guardado.
- **Integración de datos (`repository` con `@DataJpaTest`):**
  - `EventRepositoryTest`: validación de consultas JPQL, Entity Graphs, filtros por ciudad, categoría, rango de fechas y filtro global `@SQLRestriction`.
  - `VenueRepositoryTest`: persistencia, generación de IDs y búsqueda paginada por nombre.
- **Integración web y vistas (`web` con `@SpringBootTest` + `MockMvc`):**
  - `EventViewControllerTest`: listado con y sin datos, aplicación de filtros de búsqueda en URLs, renderizado de formularios, guardado con selección de venue/categorías, validaciones y redirección tras borrado lógico.
  - `VenueViewControllerTest`: renderizado de tabla, mensajes de catálogo vacío, validación de campos obligatorios (incluyendo ciudad) y redirección Post/Redirect/Get.
