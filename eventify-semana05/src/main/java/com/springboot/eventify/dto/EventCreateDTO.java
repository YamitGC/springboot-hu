package com.springboot.eventify.dto;

import com.springboot.eventify.validation.NoPastEvents;
import com.springboot.eventify.validation.group.OnCreate;
import com.springboot.eventify.validation.group.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Datos de entrada para registrar o actualizar un evento")
public class EventCreateDTO {

    @Schema(description = "Identificador único (prohibido en creación, obligatorio en actualización)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @Null(groups = OnCreate.class, message = "El ID no debe enviarse en la creación")
    @NotNull(groups = OnUpdate.class, message = "El ID es obligatorio en la actualización")
    private Long id;

    @Schema(description = "Nombre del evento", example = "Festival Estéreo Picnic 2026", minLength = 3, maxLength = 150)
    @NotBlank(message = "El nombre del evento no debe estar vacío")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String nombre;

    @Schema(description = "Fecha de realización del evento (yyyy-MM-dd)", example = "2026-11-20")
    @NotNull(message = "La fecha del evento es obligatoria")
    @NoPastEvents(message = "La fecha no puede ser anterior a hoy")
    private LocalDate fecha;

    @Schema(description = "Descripción detallada del evento", example = "Edición anual del festival musical más grande del país", maxLength = 500)
    @NotBlank(message = "La descripción del evento no debe estar vacía")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @Schema(description = "Identificador del lugar (Venue) donde se realizará el evento", example = "1")
    @NotNull(message = "Debes seleccionar un lugar")
    private Long venueId;

    @Schema(description = "Identificadores de las categorías asignadas al evento", example = "[1, 2]")
    @NotEmpty(message = "Selecciona al menos una categoría")
    private List<Long> categoryIds = new ArrayList<>();
}
