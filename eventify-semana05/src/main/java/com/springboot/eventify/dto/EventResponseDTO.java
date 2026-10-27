package com.springboot.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de respuesta desacoplados de un evento")
public class EventResponseDTO {

    @Schema(description = "Identificador único del evento", example = "1")
    private Long id;

    @Schema(description = "Nombre del evento", example = "Festival Estéreo Picnic 2026")
    private String nombre;

    @Schema(description = "Fecha de realización del evento", example = "2026-11-20")
    private LocalDate fecha;

    @Schema(description = "Descripción del evento", example = "Edición anual del festival musical más grande del país")
    private String descripcion;

    @Schema(description = "Nombre del lugar donde se realiza", example = "Parque Simón Bolívar")
    private String venueName;

    @Schema(description = "Nombres de las categorías asociadas al evento", example = "[\"Música\", \"Festivales\"]")
    private List<String> categoryNames = new ArrayList<>();
}

