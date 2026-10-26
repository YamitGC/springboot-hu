package com.springboot.eventify.dto;

import com.springboot.eventify.model.Category;
import com.springboot.eventify.model.Event;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para crear o actualizar un evento. El lugar y las categorías se referencian por ID")
public class EventRequest {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre del evento no puede estar vacío")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombre;

    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "La descripción del evento no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @Schema(description = "ID del lugar (obligatorio)", example = "5")
    @NotNull(message = "Debes seleccionar un lugar")
    private Long venueId;

    @Schema(description = "IDs de las categorías (al menos una)", example = "[1, 8]")
    @NotEmpty(message = "Selecciona al menos una categoría")
    private List<Long> categoryIds = new ArrayList<>();

    /** Convierte una entidad en el objeto de formulario (para precargar la edición). */
    public static EventRequest from(Event event) {
        EventRequest r = new EventRequest();
        r.setId(event.getId());
        r.setNombre(event.getNombre());
        r.setFecha(event.getFecha());
        r.setDescripcion(event.getDescripcion());
        r.setVenueId(event.getVenue().getId());
        r.setCategoryIds(new ArrayList<>(
                event.getCategories().stream().map(Category::getId).toList()));
        return r;
    }
}
