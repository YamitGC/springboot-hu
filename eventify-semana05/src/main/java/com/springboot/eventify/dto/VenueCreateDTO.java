package com.springboot.eventify.dto;

import com.springboot.eventify.validation.group.OnCreate;
import com.springboot.eventify.validation.group.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de entrada para registrar o actualizar un lugar (Venue)")
public class VenueCreateDTO {

    @Schema(description = "Identificador único (prohibido en creación, obligatorio en actualización)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @Null(groups = OnCreate.class, message = "El ID no debe enviarse en la creación")
    @NotNull(groups = OnUpdate.class, message = "El ID es obligatorio en la actualización")
    private Long id;

    @Schema(description = "Nombre del lugar", example = "Movistar Arena")
    @NotBlank(message = "El nombre del lugar no puede estar vacío")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String nombre;

    @Schema(description = "Dirección física del lugar", example = "Diagonal 61C #26-36")
    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(max = 250, message = "La dirección no puede superar los 250 caracteres")
    private String direccion;

    @Schema(description = "Ciudad donde se ubica el lugar", example = "Bogotá")
    @NotBlank(message = "La ciudad no puede estar vacía")
    @Size(max = 100, message = "La ciudad no puede superar los 100 caracteres")
    private String ciudad;

    @Schema(description = "Capacidad máxima de asistentes", example = "14000")
    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private Integer capacidad;
}
