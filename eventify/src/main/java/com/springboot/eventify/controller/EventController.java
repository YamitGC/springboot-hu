package com.springboot.eventify.controller;

import com.springboot.eventify.model.Event;
import com.springboot.eventify.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name="Eventos", description = "Operaciones para registrar y consultar eventos")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo evento", description = "Valida y almacena un nuevo evento en memoria")
    public Event create(@RequestBody Event event){
        return eventService.save(event);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos los eventos", description = "Retorna la colección completa de eventos registrados")
    public List<Event> getAll(){
        return eventService.findAll();
    }
}
