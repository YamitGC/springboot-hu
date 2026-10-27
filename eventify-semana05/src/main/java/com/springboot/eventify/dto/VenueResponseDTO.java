package com.springboot.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de respuesta para un lugar (Venue)")
public class VenueResponseDTO {

    @Schema(description = "Identificador único del lugar", example = "1")
    private Long id;

    @Schema(description = "Nombre del lugar", example = "Movistar Arena")
    private String nombre;

    @Schema(description = "Dirección física del lugar", example = "Diagonal 61C #26-36")
    private String direccion;

    @Schema(description = "Ciudad donde se ubica el lugar", example = "Bogotá")
    private String ciudad;

    @Schema(description = "Capacidad máxima de asistentes", example = "14000")
    private Integer capacidad;
}

