# Eventify — Semana 5: Desacoplamiento, Validación Profesional y Manejo Global de Errores

Esta entrega evoluciona la plataforma **Eventify** hacia una arquitectura desacoplada y profesional de nivel empresarial:
- **Desacoplamiento total con DTOs y MapStruct:** La API REST nunca expone entidades JPA directamente en operaciones individuales de creación, consulta y actualización. Se mantiene el Record `EventSummaryDTO` con `Slice` para listados masivos.
- **Validación robusta con Jakarta Bean Validation:** Validación declarativa de datos de entrada (`@NotBlank`, `@Size`, `@Min`, `@NotNull`), grupos de validación (`OnCreate` y `OnUpdate`), y el validador personalizado `@NoPastEvents` para asegurar la integridad temporal de los eventos.
- **Manejo global de errores bajo RFC 7807 (Problem Details):** Respuestas de error estandarizadas para integradores externos con `ProblemDetail` (`type`, `title`, `status`, `detail`, `instance` y mapa de `errors` por campo).
- **Retroalimentación visual en Thymeleaf:** Formularios administrativos con resaltado visual (`campo-invalido`), mensajes de error inline (`th:errors`), preservación de datos en pantalla y mensajes flash (`RedirectAttributes`) de éxito y error.
- **Documentación Swagger/OpenAPI actualizada:** Modelos basados en DTOs, restricciones visibles y esquemas de error documentados.

---

## 🏗️ Arquitectura del Proyecto

```
Cliente REST (JSON) ───► [DTOs] ──► Controller (REST) ──► Service ──► [MapStruct] ──► Repository (JPA) ──► BD
                                                                ▲          │
Panel Web (Thymeleaf) ────────────► Controller (Vistas) ───────┘          ▼
                                                                     Entidades (Modelo Interno)
                                                                           ▲
Excepciones Globales (RFC 7807 Problem Details) ◄── @RestControllerAdvice ─┘
```

### 📁 `dto`
- **`EventCreateDTO`**: Datos de entrada para crear/actualizar eventos (`nombre`, `fecha`, `descripcion`, `venueId`, `categoryIds`).
- **`EventResponseDTO`**: Datos de salida aplanados (`id`, `nombre`, `fecha`, `descripcion`, `venueName`, `categoryNames`).
- **`VenueCreateDTO`**: Datos de entrada para lugares (`nombre`, `direccion`, `ciudad`, `capacidad`).
- **`VenueResponseDTO`**: Datos de salida para lugares (`id`, `nombre`, `direccion`, `ciudad`, `capacidad`).
- **`EventSummaryDTO`**: Record optimizado para consultas masivas JPQL con paginación `Slice`.

### 📁 `mapper`
- **`VenueMapper`**: Mapeo automático bidireccional entre `VenueCreateDTO`, `Venue` y `VenueResponseDTO`.
- **`EventMapper`**: Mapeo con MapStruct resolviendo `venueId -> Venue`, `categoryIds -> Set<Category>`, `venue.nombre -> venueName` y `categories -> categoryNames`.

### 📁 `validation`
- **`@NoPastEvents`** y **`NoPastEventsValidator`**: Validador de negocio personalizado que rechaza fechas anteriores al día actual.
- **`group.OnCreate`** y **`group.OnUpdate`**: Grupos de validación que controlan reglas específicas (ej. prohibición de `id` en creación y obligatoriedad en actualización).

### 📁 `exception`
- **`GlobalExceptionHandler`** (`@RestControllerAdvice`): Manejador global que retorna `ProblemDetail` según el estándar RFC 7807 para:
  - `MethodArgumentNotValidException` (400 Bad Request con mapa `errors` por campo)
  - `ResourceNotFoundException` (404 Not Found con URI, título y detalle)
  - `DuplicateResourceException` (409 Conflict)
  - `BusinessRuleViolationException` e `InvalidDataException` (400 Bad Request)
  - `Exception` (500 Internal Server Error)

---

## 🧪 Pruebas Automatizadas

El proyecto cuenta con **51 pruebas automatizadas** que cubren todos los criterios de aceptación:

```bash
cd eventify-semana05
./mvnw test
```

### Cobertura de Pruebas:
1. **Validación (`ValidationTest`):** Validador personalizado `@NoPastEvents`, fechas permitidas y grupos `OnCreate`/`OnUpdate`.
2. **Servicios (`EventServiceTest`, `VenueServiceTest`):** Pruebas unitarias de reglas de negocio con Mockito.
3. **Controladores API REST (`EventApiControllerTest`, `VenueApiControllerTest`):**
   - Retorno de DTOs planos sin filtrar entidades JPA.
   - 400 Bad Request con Problem Details y mapa de errores por campo.
   - 404 Not Found bajo RFC 7807 (`type`, `title`, `status`, `detail`, `instance`).
   - Mapeo bidireccional con MapStruct.
   - Coexistencia del Record `EventSummaryDTO` y `EventResponseDTO`.
4. **Controladores de Vistas Thymeleaf (`EventViewControllerTest`, `VenueViewControllerTest`):**
   - Recarga de formularios con mensajes de error y clases de resaltado.
   - Mensajes flash de confirmación.
5. **Repositorios (`EventRepositoryTest`, `VenueRepositoryTest`):** Pruebas de persistencia y consultas derivadas.
