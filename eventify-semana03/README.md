# Eventify Semana 3

API REST desarrollada con **Spring Boot** para la gestión de **Eventos** y **Lugares (Venues)**, ahora complementada con un **panel administrativo web** construido con **Thymeleaf**. Permite crear, consultar, actualizar y eliminar eventos y los espacios donde se realizan, tanto desde la API REST como desde una interfaz web, incluyendo paginación, validaciones de negocio (Bean Validation) y manejo centralizado de errores.

---

## 🏗️ Arquitectura del proyecto

El proyecto sigue una arquitectura en capas típica de Spring Boot, separando responsabilidades de la siguiente manera:

```
Controller (API)  ─┐
                    ├─→  Service  →  Repository  →  Base de datos
Controller (Vistas)─┘         ↓
                           Model (Entidades)
                               ↓
                          Exception (Manejo de errores)
```

La novedad de esta semana es que ahora existen **dos tipos de controladores** que comparten la misma capa de servicio: los controladores REST (`@RestController`, ya existentes) y los nuevos controladores de vistas (`@Controller`), que renderizan plantillas HTML con Thymeleaf para un panel administrativo navegable desde el navegador.

### 📁 `model`
Contiene las **entidades JPA** que representan las tablas de la base de datos.

- **`Event.java`**: representa un evento (`id`, `nombre`, `fecha`, `descripcion`). Incluye anotaciones de **Bean Validation** (`@NotBlank`, `@NotNull`, `@Size`) usadas tanto por la API como por los formularios web.
- **`Venue.java`**: representa un lugar (`id`, `nombre`, `direccion`, `capacidad`). También validado con `@NotBlank`, `@NotNull` y `@Min`.

Ambas usan Lombok (`@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`) para reducir código repetitivo, y anotaciones de Swagger/OpenAPI para marcar el campo `id` como de solo lectura en la documentación.

### 📁 `repository`
Contiene las interfaces que extienden `JpaRepository`, encargadas del acceso a datos.

- **`EventRepository`** y **`VenueRepository`**: además de los métodos CRUD que provee Spring Data JPA, incluyen una consulta derivada `findByNombreContaining(...)` para búsquedas paginadas por nombre.

### 📁 `service`
Contiene la **lógica de negocio** de la aplicación. Es la capa intermedia entre los controladores (tanto REST como de vistas) y los repositorios.

- **`EventService`** y **`VenueService`**: implementan las operaciones de guardar, listar (paginado), buscar por ID, actualizar y eliminar. También incluyen las **validaciones de negocio** (por ejemplo, que el nombre no esté vacío o que la capacidad de un venue sea mayor a 0), lanzando excepciones personalizadas cuando los datos no son válidos o el recurso no existe.

### 📁 `controller`
Expone tanto los **endpoints REST** como las **rutas del panel web**, delegando siempre el trabajo a la capa de servicio.

**Controladores REST** (JSON, documentados con Swagger):
- **`EventController`** → `/api/events`
- **`VenueController`** → `/api/venues`

**Controladores de vistas** (HTML con Thymeleaf, nuevos en esta entrega):
- **`EventViewController`** → `/admin/events`
- **`VenueViewController`** → `/admin/venues`

Cada controlador REST soporta `POST`, `GET` (listado paginado y por ID), `PUT` y `DELETE`, documentado con anotaciones de **Springdoc OpenAPI** (`@Tag`, `@Operation`, `@ApiResponse`). Los controladores de vistas exponen rutas equivalentes pensadas para un navegador: listado paginado, detalle, formulario de creación/edición y eliminación mediante formularios POST.

### 📁 `exception`
Maneja los errores de forma centralizada.

- **`ResourceNotFoundException`**: se lanza cuando no se encuentra un evento o lugar por su ID.
- **`InvalidDataException`**: se lanza cuando los datos enviados no cumplen las reglas de negocio.
- **`GlobalExceptionHandler`**: mediante `@RestControllerAdvice`, intercepta estas excepciones (y cualquier otra genérica) **solo para los controladores REST**, devolviendo una respuesta JSON estandarizada con `timestamp`, `status`, `error` y `message`.
- Los **controladores de vistas** (`EventViewController`, `VenueViewController`) manejan estas mismas excepciones de forma independiente, mediante métodos `@ExceptionHandler` locales que redirigen al listado correspondiente mostrando un mensaje de error amigable en la interfaz, en lugar de responder JSON.

### 📁 `config`
Contiene configuración adicional de la aplicación.

- **`DataSeederConfig`**: al iniciar la aplicación, precarga **15 lugares** y **15 eventos** de ejemplo (ampliado respecto a la semana anterior) para facilitar las pruebas manuales y ver la paginación en acción tanto en Swagger como en el panel administrativo.

### 📄 `EventifyApplication.java`
Clase principal que arranca la aplicación Spring Boot.

### 📁 `resources`
- **`application.properties`**: configuración de conexión a la base de datos y de Hibernate/JPA (dialecto, autogeneración de esquema, logueo de SQL), además de restringir la documentación de Swagger únicamente a las rutas `/api/**`.
- **`static/css/styles.css`**: hoja de estilos del panel administrativo (tablas, botones, formularios, paginación, alertas).
- **`templates/`**: plantillas Thymeleaf del panel web.
    - `events/list.html`, `events/detail.html`, `events/form.html`: listado paginado, detalle y formulario (creación/edición) de eventos.
    - `venues/list.html`, `venues/detail.html`, `venues/form.html`: listado paginado, detalle y formulario (creación/edición) de lugares.
    - `fragments/layout.html`: fragmentos reutilizables de navegación (`nav`) y pie de página (`footer`), incluidos en todas las vistas.
    - `fragments/error.html`: página genérica de error para el panel web.

### 📁 `test`
Incluye:
- **Pruebas unitarias** (con **Mockito**, mockeando los repositorios) para la capa `service` (`EventServiceTest`, `VenueServiceTest`).
- **Pruebas de integración de repositorio** con **`@DataJpaTest`** (`EventRepositoryTest`, `VenueRepositoryTest`).
- **Pruebas de integración de los controladores de vistas** con **`@SpringBootTest` + `MockMvc`** (`EventViewControllerTest`, `VenueViewControllerTest`), que verifican listados con y sin datos, carga de formularios, guardado exitoso con redirección, y re-renderizado del formulario con errores de validación cuando los datos son inválidos.

---

## 🛠️ Tecnologías utilizadas

- **Java 21**
- **Spring Boot 4.1.1**
- **Spring Data JPA**
- **Spring Web MVC** (`spring-boot-starter-webmvc`)
- **Thymeleaf** (`spring-boot-starter-thymeleaf`) — motor de plantillas del panel administrativo
- **Bean Validation** (`spring-boot-starter-validation`) — validación de formularios y de la API
- **PostgreSQL** (base de datos principal)
- **H2** (dependencia disponible para pruebas en memoria)
- **Lombok**
- **Springdoc OpenAPI** (documentación Swagger, restringida a `/api/**`)
- **JUnit 5 + Mockito** (pruebas unitarias)
- **MockMvc** (`spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`) — pruebas de integración de vistas y repositorios
- **Maven** (con Maven Wrapper incluido)

---

## ✅ Requisitos previos

Antes de ejecutar el proyecto necesitas tener instalado:

- **JDK 21** o superior
- **Docker** (para levantar la base de datos PostgreSQL)
- **DBeaver** (para visualizar y administrar la base de datos)

> No necesitas tener Maven instalado localmente: el proyecto incluye el **Maven Wrapper** (`mvnw` / `mvnw.cmd`).

---

## 🐳 Paso 1: Levantar la base de datos con Docker

Según la configuración en `application.properties`, la aplicación espera una base de datos PostgreSQL con estos datos:

| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `5433` |
| Base de datos | `eventify` |
| Usuario | `eventify` |
| Contraseña | `eventify` |

Levanta un contenedor de PostgreSQL con esos mismos valores ejecutando:

```bash
docker run --name eventify-db \
  -e POSTGRES_DB=eventify \
  -e POSTGRES_USER=eventify \
  -e POSTGRES_PASSWORD=eventify \
  -p 5433:5432 \
  -d postgres:16
```

Esto crea y ejecuta un contenedor llamado `eventify-db`, mapeando el puerto `5433` de tu máquina al puerto `5432` interno del contenedor (donde escucha PostgreSQL por defecto).

Para verificar que el contenedor está corriendo:

```bash
docker ps
```

Para detenerlo o volver a iniciarlo en otro momento:

```bash
docker stop eventify-db
docker start eventify-db
```

> Las pruebas de repositorio (`@DataJpaTest`) usan una base de datos separada, `eventify_test`, definida en `src/test/resources/application.properties`. Si vas a ejecutar `./mvnw test`, crea también esa base de datos (por ejemplo, conectándote al mismo contenedor de Docker y ejecutando `CREATE DATABASE eventify_test;` desde DBeaver).

---

## 🔍 Paso 2: Conectarte con DBeaver

1. Abre **DBeaver** y crea una nueva conexión: `Database` → `New Database Connection` → selecciona **PostgreSQL**.
2. Completa los datos de conexión:
    - **Host**: `localhost`
    - **Port**: `5433`
    - **Database**: `eventify`
    - **Username**: `eventify`
    - **Password**: `eventify`
3. Da clic en **Test Connection** para confirmar que todo esté correcto (si DBeaver pide descargar el driver de PostgreSQL, acéptalo).
4. Guarda la conexión y conéctate.
5. Una vez que ejecutes la aplicación (paso 3), Hibernate creará automáticamente las tablas `events` y `venues` dentro de la base `eventify`, y podrás explorarlas desde DBeaver.

---

## ▶️ Paso 3: Ejecutar la aplicación

Con la base de datos ya corriendo en Docker, ejecuta el proyecto desde la raíz del repositorio usando el Maven Wrapper:

**En Linux / macOS:**
```bash
./mvnw spring-boot:run
```

**En Windows:**
```bash
mvnw.cmd spring-boot:run
```

Al iniciar, Spring Boot:
- Se conectará a la base de datos PostgreSQL en el puerto `5433`.
- Creará/actualizará automáticamente las tablas (`spring.jpa.hibernate.ddl-auto=update`).
- Ejecutará el `DataSeederConfig`, insertando **15 lugares** y **15 eventos** de ejemplo.
- Quedará disponible en `http://localhost:8080`.

---

## 🖥️ Panel administrativo (Thymeleaf)

Además de la API REST, esta semana se incorpora un panel web navegable directamente desde el navegador, pensado para gestionar eventos y lugares sin necesidad de herramientas como Postman o Swagger:

```
http://localhost:8080/admin/events
http://localhost:8080/admin/venues
```

Desde el panel puedes:

| Acción | Ruta |
|---|---|
| Ver listado paginado | `GET /admin/events` · `GET /admin/venues` |
| Ver detalle | `GET /admin/events/{id}` · `GET /admin/venues/{id}` |
| Mostrar formulario de creación | `GET /admin/events/new` · `GET /admin/venues/new` |
| Crear (procesar formulario) | `POST /admin/events` · `POST /admin/venues` |
| Mostrar formulario de edición | `GET /admin/events/{id}/edit` · `GET /admin/venues/{id}/edit` |
| Actualizar (procesar formulario) | `POST /admin/events/{id}/edit` · `POST /admin/venues/{id}/edit` |
| Eliminar | `POST /admin/events/{id}/delete` · `POST /admin/venues/{id}/delete` |

**Comportamiento destacado:**
- Si el catálogo está vacío, se muestra un mensaje amigable en lugar de una tabla vacía.
- Al guardar o eliminar exitosamente, la aplicación redirige al listado mostrando un mensaje de confirmación (patrón *Post/Redirect/Get*).
- Si los datos del formulario no son válidos (por ejemplo, un nombre vacío), el formulario se vuelve a mostrar con los datos ya escritos y los mensajes de error correspondientes, sin perder la información ingresada.
- Los errores de negocio (recurso no encontrado, datos inválidos) capturados por los `ExceptionHandler` de cada `ViewController` redirigen al listado con un mensaje de error visible.

---

## 📑 Documentación de la API (Swagger)

Una vez levantada la aplicación, puedes explorar y probar todos los endpoints REST desde Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

> La documentación de Swagger está restringida únicamente a las rutas `/api/**` (`springdoc.paths-to-match=/api/**`); las rutas `/admin/**` del panel web no forman parte del contrato de la API y no aparecen en Swagger.

### Endpoints principales

**Eventos** (`/api/events`)
| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/events` | Crear un evento |
| `GET` | `/api/events` | Listar eventos (paginado) |
| `GET` | `/api/events/{id}` | Consultar evento por ID |
| `PUT` | `/api/events/{id}` | Actualizar evento |
| `DELETE` | `/api/events/{id}` | Eliminar evento |

**Lugares** (`/api/venues`)
| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/venues` | Crear un lugar |
| `GET` | `/api/venues` | Listar lugares (paginado) |
| `GET` | `/api/venues/{id}` | Consultar lugar por ID |
| `PUT` | `/api/venues/{id}` | Actualizar lugar |
| `DELETE` | `/api/venues/{id}` | Eliminar lugar |

> El listado soporta parámetros de paginación estándar de Spring: `?page=0&size=10&sort=nombre,asc`

---

## 🧪 Ejecutar las pruebas

```bash
./mvnw test
```

Esto ejecuta:
- Las **pruebas unitarias** de la capa `service` (con Mockito).
- Las **pruebas de integración** de la capa `repository` (con `@DataJpaTest`, usando la base `eventify_test`).
- Las **pruebas de integración** de los controladores de vistas (con `@SpringBootTest` + `MockMvc`), que verifican el comportamiento del panel administrativo: listados con y sin datos, carga de formularios, creación exitosa con redirección, y re-renderizado del formulario ante datos inválidos.