package com.springboot.eventify.controller;

import com.springboot.eventify.dto.EventCreateDTO;
import com.springboot.eventify.dto.EventDetailDTO;
import com.springboot.eventify.dto.EventResponseDTO;
import com.springboot.eventify.dto.EventSummaryDTO;
import com.springboot.eventify.service.EventService;
import com.springboot.eventify.validation.group.OnCreate;
import com.springboot.eventify.validation.group.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Operaciones para registrar, consultar, actualizar y eliminar (lógicamente) eventos")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo evento",
            description = "Crea un nuevo evento mediante EventCreateDTO y lo vincula al lugar y categorías.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Lugar o categoría no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public EventResponseDTO create(@Validated(OnCreate.class) @RequestBody EventCreateDTO dto) {
        return eventService.saveDTO(dto);
    }

    @GetMapping
    @Operation(summary = "Buscar eventos con filtros combinados (paginación por Slice)",
            description = """
                    Devuelve un Slice<EventSummaryDTO> (Record aplanado: evento, fecha, lugar y ciudad).
                    - Coexistencia arquitectónica: usa el Record optimizado para listados masivos.
                    - Orden fijo: fecha descendente.
                    - Borrado lógico: los eventos con activo=false no aparecen.
                    """)
    @ApiResponse(responseCode = "200", description = "Fragmento de resultados (Slice) de eventos activos")
    public Slice<EventSummaryDTO> search(
            @Parameter(description = "Ciudad del lugar (coincidencia parcial)")
            @RequestParam(required = false) String ciudad,
            @Parameter(description = "Nombre de una categoría (coincidencia parcial)")
            @RequestParam(required = false) String categoria,
            @Parameter(description = "Capacidad mínima del lugar")
            @RequestParam(required = false) Integer capacidadMin,
            @Parameter(description = "Fecha inicial (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha final (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.search(ciudad, categoria, capacidadMin, desde, hasta, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un evento activo por ID",
            description = "Retorna un EventResponseDTO desacoplado con venueName y categoryNames planos. 404 si no existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado o inactivo",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public EventResponseDTO getById(@Parameter(description = "ID del evento a consultar") @PathVariable Long id) {
        return eventService.findByIdDTO(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un evento existente",
            description = "Actualiza los datos del evento mediante EventCreateDTO y retorna EventResponseDTO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Evento, lugar o categoría no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public EventResponseDTO update(@PathVariable Long id, @Validated(OnUpdate.class) @RequestBody EventCreateDTO dto) {
        dto.setId(id);
        return eventService.updateDTO(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un evento (borrado lógico)",
            description = "Marca el evento como activo=false para conservar la trazabilidad histórica.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento desactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void delete(@PathVariable Long id) {
        eventService.softDelete(id);
    }

    // ---------------- Búsquedas por un solo criterio (finders optimizados) ----------------

    @GetMapping("/por-ciudad")
    @Operation(summary = "Buscar eventos por ciudad", description = "Devuelve un Slice<EventDetailDTO> filtrado por ciudad.")
    public Slice<EventDetailDTO> porCiudad(
            @Parameter(description = "Ciudad", required = true) @RequestParam String ciudad,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.findByCiudad(ciudad, pageable);
    }

    @GetMapping("/por-categoria")
    @Operation(summary = "Buscar eventos por categoría", description = "Devuelve un Slice<EventDetailDTO> filtrado por categoría.")
    public Slice<EventDetailDTO> porCategoria(
            @Parameter(description = "Categoría", required = true) @RequestParam String categoria,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.findByCategoria(categoria, pageable);
    }

    @GetMapping("/por-capacidad")
    @Operation(summary = "Buscar eventos por capacidad mínima", description = "Devuelve un Slice<EventDetailDTO> filtrado por capacidad.")
    public Slice<EventDetailDTO> porCapacidad(
            @Parameter(description = "Capacidad mínima", required = true) @RequestParam Integer capacidadMin,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.findByCapacidad(capacidadMin, pageable);
    }

    @GetMapping("/por-fechas")
    @Operation(summary = "Buscar eventos por rango de fechas", description = "Devuelve un Slice<EventDetailDTO> filtrado por rango de fechas.")
    public Slice<EventDetailDTO> porFechas(
            @Parameter(description = "Fecha inicial (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha final (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.findByRangoFechas(desde, hasta, pageable);
    }
}
