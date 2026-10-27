package com.springboot.eventify.controller;

import com.springboot.eventify.dto.VenueCreateDTO;
import com.springboot.eventify.dto.VenueResponseDTO;
import com.springboot.eventify.service.VenueService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/venues")
@Tag(name = "Lugares", description = "Operaciones para registrar, consultar, actualizar y eliminar lugares (Venues)")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo lugar", description = "Valida y almacena un lugar desacoplado mediante DTOs")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lugar creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public VenueResponseDTO create(@Validated(OnCreate.class) @RequestBody VenueCreateDTO dto) {
        return venueService.saveDTO(dto);
    }

    @GetMapping
    @Operation(summary = "Listar lugares de forma paginada", description = "Soporta los parámetros 'page', 'size' y 'sort' (ej: ?page=0&size=10&sort=nombre,asc)")
    @ApiResponse(responseCode = "200", description = "Listado paginado de lugares en formato DTO")
    public Page<VenueResponseDTO> getAll(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return venueService.findAllDTO(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un lugar por ID", description = "Retorna 404 Not Found si el lugar no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar encontrado"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public VenueResponseDTO getById(@Parameter(description = "ID del lugar a consultar") @PathVariable Long id) {
        return venueService.findByIdDTO(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un lugar existente", description = "Actualiza los atributos del lugar; retorna 404 si no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public VenueResponseDTO update(@PathVariable Long id, @Validated(OnUpdate.class) @RequestBody VenueCreateDTO dto) {
        dto.setId(id);
        return venueService.updateDTO(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un lugar", description = "Elimina el registro de forma definitiva; retorna 404 si no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lugar eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Conflicto o datos inválidos (tiene eventos asociados)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void delete(@PathVariable Long id) {
        venueService.delete(id);
    }
}
