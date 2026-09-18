package com.springboot.eventify.controller;

import com.springboot.eventify.model.Venue;
import com.springboot.eventify.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/venues")
@Tag(name = "Lugares",description = "Operaciones para registrar, consultar, actualizar y eliminar lugares(venues)")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo lugar", description = "Valida y almacena un lugar en la base de datos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lugar creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (por ejemplo, nombre vacío)")
    })
    public Venue create(@RequestBody Venue venue) {
        return venueService.save(venue);
    }

    @GetMapping
    @Operation(summary = "Listar lugares de forma paginada", description = "Soporta los parámetros 'page', 'size' y 'sort' (ej: ?page=0&size=10&sort=nombre,asc)")
    @ApiResponse(responseCode = "200", description = "Listado paginado de lugares")
    public Page<Venue> getAll(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return venueService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un lugar por ID", description = "Retorna 404 Not Found si el lugar no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar encontrado"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public Venue getById(@Parameter(description = "ID del lugar a consultar") @PathVariable Long id) {
        return venueService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un lugar existente", description = "Valida que el lugar exista antes de actualizar; retorna 404 Not Found si no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public Venue update(@PathVariable Long id, @RequestBody Venue venue) {
        return venueService.update(id, venue);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un lugar", description = "Elimina el registro de forma definitiva; retorna 404 Not Found si el ID no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lugar eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public void delete(@PathVariable Long id) {
        venueService.delete(id);
    }

}
