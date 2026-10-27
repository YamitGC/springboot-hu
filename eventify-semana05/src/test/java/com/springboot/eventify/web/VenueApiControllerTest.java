package com.springboot.eventify.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.eventify.dto.VenueCreateDTO;
import com.springboot.eventify.model.Venue;
import com.springboot.eventify.repository.VenueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VenueApiControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    @Autowired private VenueRepository venueRepository;

    @Test
    @DisplayName("POST /api/venues crea un lugar usando VenueCreateDTO y retorna VenueResponseDTO")
    void create_VenueValido_Retorna201YResponseDTO() throws Exception {
        VenueCreateDTO dto = new VenueCreateDTO(null, "Teatro Mayor", "Calle 170 #67-51", "Bogotá", 1300);

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Teatro Mayor"))
                .andExpect(jsonPath("$.ciudad").value("Bogotá"))
                .andExpect(jsonPath("$.capacidad").value(1300));
    }

    @Test
    @DisplayName("POST /api/venues con datos inválidos retorna 400 ProblemDetail")
    void create_VenueInvalido_Retorna400ProblemDetail() throws Exception {
        VenueCreateDTO dto = new VenueCreateDTO(null, "", "", "", -5);

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.capacidad").exists());
    }

    @Test
    @DisplayName("GET /api/venues/{id} inexistente retorna 404 ProblemDetail")
    void getById_Inexistente_Retorna404ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/venues/{id}", 8888))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail", containsString("8888")));
    }
}
