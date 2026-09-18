# Eventify Semana 2

API REST desarrollada con **Spring Boot** para la gestión de **Eventos** y **Lugares (Venues)**. Permite crear, consultar, actualizar y eliminar eventos y los espacios donde se realizan, incluyendo paginación, validaciones de negocio y manejo centralizado de errores.

---

## 🏗️ Arquitectura del proyecto

El proyecto sigue una arquitectura en capas típica de Spring Boot, separando responsabilidades de la siguiente manera:

```
Controller  →  Service  →  Repository  →  Base de datos
                  ↓
              Model (Entidades)
                  ↓
             Exception (Manejo de errores)
```

### 📁 `model`
Contiene las **entidades JPA** que representan las tablas de la base de datos.

- **`Event.java`**: representa un evento (`id`, `nombre`, `fecha`, `descripcion`).
- **`Venue.java`**: representa un lugar (`id`, `nombre`, `direccion`, `capacidad`).

Ambas usan Lombok (`@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`) para reducir código repetitivo, y anotaciones de Swagger/OpenAPI para marcar el campo `id` como de solo lectura en la documentación.

### 📁 `repository`
Contiene las interfaces que extienden `JpaRepository`, encargadas del acceso a datos.

- **`EventRepository`** y **`VenueRepository`**: además de los métodos CRUD que provee Spring Data JPA, incluyen una consulta derivada `findByNombreContaining(...)` para búsquedas paginadas por nombre.

### 📁 `service`
Contiene la **lógica de negocio** de la aplicación. Es la capa intermedia entre los controladores y los repositorios.

- **`EventService`** y **`VenueService`**: implementan las operaciones de guardar, listar (paginado), buscar por ID, actualizar y eliminar. También incluyen las **validaciones de negocio** (por ejemplo, que el nombre no esté vacío o que la capacidad de un venue sea mayor a 0), lanzando excepciones personalizadas cuando los datos no son válidos o el recurso no existe.

### 📁 `controller`
Expone los **endpoints REST** de la aplicación y delega el trabajo a la capa de servicio.

- **`EventController`** → `/api/events`
- **`VenueController`** → `/api/venues`

Cada controlador soporta las operaciones `POST`, `GET` (listado paginado y por ID), `PUT` y `DELETE`, y está documentado con anotaciones de **Springdoc OpenAPI** (`@Tag`, `@Operation`, `@ApiResponse`) para generar la documentación Swagger automáticamente.

### 📁 `exception`
Maneja los errores de forma centralizada mediante `@RestControllerAdvice`.

- **`ResourceNotFoundException`**: se lanza cuando no se encuentra un evento o lugar por su ID (responde `404`).
- **`InvalidDataException`**: se lanza cuando los datos enviados no cumplen las reglas de negocio (responde `400`).
- **`GlobalExceptionHandler`**: intercepta estas excepciones (y cualquier otra genérica) y devuelve una respuesta JSON estandarizada con `timestamp`, `status`, `error` y `message`.

### 📁 `config`
Contiene configuración adicional de la aplicación.

- **`DataSeederConfig`**: al iniciar la aplicación, precarga datos de ejemplo (dos venues y dos eventos) para facilitar las pruebas manuales.

### 📄 `EventifyApplication.java`
Clase principal que arranca la aplicación Spring Boot.

### 📁 `resources/application.properties`
Configuración de conexión a la base de datos y de Hibernate/JPA (dialecto, autogeneración de esquema, logueo de SQL).

### 📁 `test`
Incluye pruebas unitarias (con **Mockito**, mockeando los repositorios) para la capa de servicio, y pruebas de integración con **`@DataJpaTest`** para la capa de repositorio.

---

## 🛠️ Tecnologías utilizadas

- **Java 21**
- **Spring Boot 4.1.1**
- **Spring Data JPA**
- **Spring Web MVC**
- **PostgreSQL** (base de datos principal)
- **H2** (dependencia disponible para pruebas en memoria)
- **Lombok**
- **Springdoc OpenAPI** (documentación Swagger)
- **JUnit 5 + Mockito** (pruebas)
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
- Ejecutará el `DataSeederConfig`, insertando datos de ejemplo (venues y eventos).
- Quedará disponible en `http://localhost:8080`.

---

## 📑 Documentación de la API (Swagger)

Una vez levantada la aplicación, puedes explorar y probar todos los endpoints desde Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

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

Esto ejecuta tanto las pruebas unitarias de la capa `service` (con Mockito) como las pruebas de integración de la capa `repository` (con `@DataJpaTest`).