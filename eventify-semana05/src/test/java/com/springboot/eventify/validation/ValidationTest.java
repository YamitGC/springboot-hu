package com.springboot.eventify.validation;

import com.springboot.eventify.dto.EventCreateDTO;
import com.springboot.eventify.validation.group.OnCreate;
import com.springboot.eventify.validation.group.OnUpdate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe fallar si la fecha del evento es anterior a hoy (@NoPastEvents)")
    void fechaAnteriorAHoy_FallaValidacion() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setNombre("Concierto");
        dto.setFecha(LocalDate.now().minusDays(1)); // Ayer
        dto.setDescripcion("Descripción válida");
        dto.setVenueId(1L);
        dto.setCategoryIds(List.of(1L));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fecha")
                && v.getMessage().contains("no puede ser anterior a hoy"));
    }

    @Test
    @DisplayName("Debe aceptar si la fecha del evento es hoy o futura")
    void fechaHoyOFutura_PasaValidacion() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setNombre("Concierto");
        dto.setFecha(LocalDate.now().plusDays(5));
        dto.setDescripcion("Descripción válida");
        dto.setVenueId(1L);
        dto.setCategoryIds(List.of(1L));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("fecha"));
    }

    @Test
    @DisplayName("En grupo OnCreate el ID debe ser nulo")
    void grupoOnCreate_ConIdPresente_Falla() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setId(10L);
        dto.setNombre("Concierto");
        dto.setFecha(LocalDate.now().plusDays(5));
        dto.setDescripcion("Descripción válida");
        dto.setVenueId(1L);
        dto.setCategoryIds(List.of(1L));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto, OnCreate.class);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("id")
                && v.getMessage().contains("no debe enviarse en la creación"));
    }

    @Test
    @DisplayName("En grupo OnUpdate el ID es obligatorio")
    void grupoOnUpdate_SinId_Falla() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setId(null);
        dto.setNombre("Concierto");
        dto.setFecha(LocalDate.now().plusDays(5));
        dto.setDescripcion("Descripción válida");
        dto.setVenueId(1L);
        dto.setCategoryIds(List.of(1L));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto, OnUpdate.class);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("id")
                && v.getMessage().contains("es obligatorio en la actualización"));
    }
}

