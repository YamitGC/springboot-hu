package com.springboot.eventify.dto;

import com.springboot.eventify.model.Category;
import com.springboot.eventify.model.Event;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Schema(description = "Detalle completo de un evento activo, con su lugar y sus categorías")
public record EventDetailDTO(
        Long id,
        String nombre,
        LocalDate fecha,
        String descripcion,
        @Schema(description = "ID del lugar asignado") Long venueId,
        String nombreLugar,
        String ciudad,
        @Schema(description = "Nombres de las categorías asignadas") List<String> categorias
) {
    public static EventDetailDTO from(Event e) {
        List<String> nombres = e.getCategories().stream()
                .map(Category::getNombre)
                .sorted(Comparator.naturalOrder())
                .toList();
        return new EventDetailDTO(e.getId(), e.getNombre(), e.getFecha(), e.getDescripcion(),
                e.getVenue().getId(), e.getVenue().getNombre(), e.getVenue().getCiudad(), nombres);
    }
}
