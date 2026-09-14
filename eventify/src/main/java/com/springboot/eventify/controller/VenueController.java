package com.springboot.eventify.controller;

import com.springboot.eventify.model.Venue;
import com.springboot.eventify.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@Tag(name = "Lugares", description = "Operaciones para registrar y consultar lugares(venues)")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo lugar", description = "Valida y almacena un nuevo lugar en memoria")
    public Venue create(@RequestBody Venue venue){
        return venueService.save(venue);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos los lugares", description = "Retora la colección completa de lugares registrados")
    public List<Venue> getAll(){
        return venueService.findAll();
    }
}
