package com.springboot.eventify.controller;

import com.springboot.eventify.dto.EventDetailDTO;
import com.springboot.eventify.dto.EventRequest;
import com.springboot.eventify.dto.EventSummaryDTO;
import com.springboot.eventify.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
            description = "Crea el evento vinculándolo a un lugar (venueId, obligatorio) y a una o más categorías "
                    + "(categoryIds). Los vínculos con las categorías se guardan en cascada en la tabla events_categories.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o categorías inexistentes"),
            @ApiResponse(responseCode = "404", description = "El lugar indicado no existe")
    })
    public EventDetailDTO create(@RequestBody EventRequest request) {
        return EventDetailDTO.from(eventService.save(request));
    }

    @GetMapping
    @Operation(summary = "Buscar eventos con filtros combinados (paginación por Slice)",
            description = """
                    Devuelve un Slice<EventSummaryDTO> (Record aplanado: evento, fecha, lugar y ciudad).
                    - Orden fijo: fecha descendente (los más recientes primero). El parámetro 'sort' se ignora.
                    - Todos los filtros son opcionales, se combinan con AND, son parciales e insensibles a mayúsculas.
                    - Borrado lógico: los eventos con activo=false NUNCA aparecen en los resultados.
                    - Slice no calcula el total de registros: la respuesta incluye 'last' para saber si hay más páginas.
                    """)
    @ApiResponse(responseCode = "200", description = "Fragmento de resultados (Slice) de eventos activos")
    public Slice<EventSummaryDTO> search(
            @Parameter(description = "Ciudad del lugar. Coincidencia parcial e insensible a mayúsculas (ej: 'bog' encuentra 'Bogotá')")
            @RequestParam(required = false) String ciudad,
            @Parameter(description = "Nombre de una categoría asignada al evento. Parcial e insensible a mayúsculas (ej: 'rock')")
            @RequestParam(required = false) String categoria,
            @Parameter(description = "Capacidad mínima del lugar del evento (ej: 500)")
            @RequestParam(required = false) Integer capacidadMin,
            @Parameter(description = "Fecha inicial del rango, inclusive (formato yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha final del rango, inclusive (formato yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.search(ciudad, categoria, capacidadMin, desde, hasta, pageable);
    }

    // ---------------- Búsquedas por UN solo criterio (finders del repositorio) ----------------

    @GetMapping("/por-ciudad")
    @Operation(summary = "Buscar eventos por ciudad",
            description = """
                    Devuelve un Slice<EventDetailDTO> con lugar y categorías de cada evento.
                    Coincidencia parcial e insensible a mayúsculas sobre la ciudad del lugar. Orden fijo: fecha descendente.
                    Los eventos eliminados lógicamente (activo=false) no aparecen.
                    """)
    public Slice<EventDetailDTO> porCiudad(
            @Parameter(description = "Ciudad (parcial, insensible a mayúsculas). Ej: 'bog'", required = true)
            @RequestParam String ciudad,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.findByCiudad(ciudad, pageable);
    }

    @GetMapping("/por-categoria")
    @Operation(summary = "Buscar eventos por categoría",
            description = """
                    Devuelve un Slice<EventDetailDTO>. El nombre de la categoría se compara de forma parcial e
                    insensible a mayúsculas; un evento aparece una sola vez aunque varias de sus categorías coincidan.
                    Orden fijo: fecha descendente. Los eventos con activo=false no aparecen.
                    """)
    public Slice<EventDetailDTO> porCategoria(
            @Parameter(description = "Nombre de la categoría (parcial, insensible a mayúsculas). Ej: 'rock'", required = true)
            @RequestParam String categoria,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.findByCategoria(categoria, pageable);
    }

    @GetMapping("/por-capacidad")
    @Operation(summary = "Buscar eventos por capacidad mínima del lugar",
            description = "Devuelve los eventos cuyo lugar tiene capacidad mayor o igual a la indicada, "
                    + "ordenados por fecha descendente. Los eventos con activo=false no aparecen.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fragmento de resultados"),
            @ApiResponse(responseCode = "400", description = "Capacidad inválida")
    })
    public Slice<EventDetailDTO> porCapacidad(
            @Parameter(description = "Capacidad mínima del lugar. Ej: 500", required = true)
            @RequestParam Integer capacidadMin,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.findByCapacidad(capacidadMin, pageable);
    }

    @GetMapping("/por-fechas")
    @Operation(summary = "Buscar eventos por rango de fechas",
            description = "Devuelve los eventos con fecha entre 'desde' y 'hasta' (ambas inclusive), "
                    + "ordenados por fecha descendente. Los eventos con activo=false no aparecen.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fragmento de resultados"),
            @ApiResponse(responseCode = "400", description = "Rango inválido (desde posterior a hasta)")
    })
    public Slice<EventDetailDTO> porFechas(
            @Parameter(description = "Fecha inicial, inclusive (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha final, inclusive (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return eventService.findByRangoFechas(desde, hasta, pageable);
    }

    // ---------------- CRUD por id ----------------

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un evento activo por ID",
            description = "Retorna 404 si el evento no existe o si fue eliminado lógicamente (activo=false).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado o inactivo")
    })
    public EventDetailDTO getById(@Parameter(description = "ID del evento a consultar") @PathVariable Long id) {
        return EventDetailDTO.from(eventService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un evento existente",
            description = "Reemplaza nombre, fecha, descripción, lugar y categorías. Un evento inactivo no puede actualizarse (404).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento o lugar no encontrado")
    })
    public EventDetailDTO update(@PathVariable Long id, @RequestBody EventRequest request) {
        return EventDetailDTO.from(eventService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un evento (borrado lógico)",
            description = "NO elimina la fila de la base de datos: marca el evento como activo=false para conservar "
                    + "la trazabilidad histórica. Desde ese momento el evento deja de aparecer en listados, "
                    + "búsquedas y consultas por ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public void delete(@PathVariable Long id) {
        eventService.softDelete(id);
    }
}
