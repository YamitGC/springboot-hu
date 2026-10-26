package com.springboot.eventify.dto;

import com.springboot.eventify.model.Event;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Resumen aplanado de un evento para listados masivos (evento + lugar + ciudad)")
public record EventSummaryDTO(
        @Schema(description = "Identificador del evento", example = "42")
        Long id,
        @Schema(description = "Nombre del evento", example = "Concierto de ROCK")
        String nombreEvento,
        @Schema(description = "Fecha del evento (yyyy-MM-dd)", example = "2026-12-20")
        LocalDate fecha,
        @Schema(description = "Nombre del lugar donde se realiza", example = "Arena Bicentenario")
        String nombreLugar,
        @Schema(description = "Ciudad del lugar", example = "Bogotá")
        String ciudad
) {
    public static EventSummaryDTO from(Event e) {
        return new EventSummaryDTO(e.getId(), e.getNombre(), e.getFecha(),
                e.getVenue().getNombre(), e.getVenue().getCiudad());
    }
}
