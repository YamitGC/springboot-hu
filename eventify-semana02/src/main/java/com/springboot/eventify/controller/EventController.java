package com.springboot.eventify.controller;

import com.springboot.eventify.model.Event;
import com.springboot.eventify.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Operaciones para registrar, consultar, actualizar y eliminar eventos")
public class EventController{

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo evento", description = "Valida y almacena un evento en la DB")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public Event create(@RequestBody Event event){
        return eventService.save(event);
    }

    @GetMapping
    @Operation(summary = "Listar eventos de forma paginada")
    @ApiResponse(responseCode = "200", description = "Listado paginado de eventos")
    public Page<Event> getAll(@ParameterObject @PageableDefault(size = 10)Pageable pageable) {
        return eventService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un evento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public Event getById(@Parameter(description = "ID del evento a consultar") @PathVariable Long id) {
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un evento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public Event update(@PathVariable Long id, @RequestBody Event event) {
        return eventService.update(id, event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un evento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public void delete(@PathVariable Long id) {
        eventService.delete(id);
    }
}
