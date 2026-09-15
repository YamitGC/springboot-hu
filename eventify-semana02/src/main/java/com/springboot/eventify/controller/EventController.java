package com.springboot.eventify.controller;

import com.springboot.eventify.model.Event;
import com.springboot.eventify.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
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
    public Event create(@RequestBody Event event){
        return eventService.save(event);
    }

    @GetMapping
    @Operation(summary = "Listar eventos de forma paginada", description = "Soporta los parámetros 'page', 'size' y 'sort' (ej: ?page=0&size=10&sort=nombre,asc)")
    public Page<Event> getAll(@PageableDefault(size = 10)Pageable pageable) {
        return  eventService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un evento por ID", description = "Retorna 404 Not Found si el evento no existe")
    public Event getById(@Parameter(description = "ID del evento a consultar") @PathVariable Long id) {
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un evento existente",
            description = "Valida que el evento exista antes de actualizar; retorna 404 Not Found si no existe"
    )
    public Event update(@PathVariable Long id, @RequestBody Event event) {
        return eventService.update(id, event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Eliminar un evento",
            description = "Elimina el registro de forma definitiva; retorna 404 Not Found si el ID no existe"
    )
    public void delete(@PathVariable Long id) {
        eventService.delete(id);
    }
}
