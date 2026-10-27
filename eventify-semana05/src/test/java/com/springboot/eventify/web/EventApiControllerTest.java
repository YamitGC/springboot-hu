package com.springboot.eventify.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.eventify.dto.EventCreateDTO;
import com.springboot.eventify.model.Category;
import com.springboot.eventify.model.Event;
import com.springboot.eventify.model.Venue;
import com.springboot.eventify.repository.CategoryRepository;
import com.springboot.eventify.repository.EventRepository;
import com.springboot.eventify.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventApiControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    @Autowired private EventRepository eventRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Venue venue;
    private Category catRock;
    private Category catPop;

    @BeforeEach
    void setUp() {
        venue = venueRepository.save(new Venue(null, "Movistar Arena", "Diagonal 61C", "Bogotá", 14000));
        catRock = categoryRepository.save(new Category(null, "Rock", "Música Rock"));
        catPop = categoryRepository.save(new Category(null, "Pop", "Música Pop"));
    }

    @Test
    @DisplayName("Escenario 1 & 7: GET /api/events/{id} retorna EventResponseDTO plano y desacoplado")
    void getById_RetornaEventResponseDTODesacoplado() throws Exception {
        Event event = new Event(null, "Festival Estéreo Picnic", LocalDate.now().plusDays(30), "Gran festival", venue);
        event.getCategories().add(catRock);
        event.getCategories().add(catPop);
        event = eventRepository.save(event);

        mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId()))
                .andExpect(jsonPath("$.nombre").value("Festival Estéreo Picnic"))
                .andExpect(jsonPath("$.venueName").value("Movistar Arena"))
                .andExpect(jsonPath("$.categoryNames", hasItems("Pop", "Rock")))
                .andExpect(jsonPath("$.venue.id").doesNotExist()) // No expone entidades internas
                .andExpect(jsonPath("$.categories").doesNotExist());
    }

    @Test
    @DisplayName("Escenario 2: Validación de entrada vía API con 400 y Problem Details RFC 7807")
    void create_NombreVacioYFechaPasada_Retorna400ProblemDetailConErroresPorCampo() throws Exception {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setNombre(""); // Vacío
        dto.setFecha(LocalDate.now().minusDays(5)); // Pasada
        dto.setDescripcion("Descripción");
        dto.setVenueId(venue.getId());
        dto.setCategoryIds(List.of(catRock.getId()));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.instance").value("/api/events"))
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.fecha").exists());
    }

    @Test
    @DisplayName("Escenario 4: Validador personalizado @NoPastEvents rechaza fecha pasada")
    void create_FechaPasada_RechazaConMensajeDeNegocio() throws Exception {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setNombre("Concierto Válido");
        dto.setFecha(LocalDate.now().minusDays(1)); // Ayer
        dto.setDescripcion("Descripción válida");
        dto.setVenueId(venue.getId());
        dto.setCategoryIds(List.of(catRock.getId()));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fecha", containsString("no puede ser anterior a hoy")));
    }

    @Test
    @DisplayName("Escenario 5: Recurso no encontrado con Problem Details (GET /api/events/9999)")
    void getById_Inexistente_Retorna404ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/events/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("El evento con ID 9999 no existe"))
                .andExpect(jsonPath("$.instance").value("/api/events/9999"))
                .andExpect(jsonPath("$.type").value("https://eventify.com/errors/not-found"));
    }

    @Test
    @DisplayName("Escenario 6: Mapeo bidireccional MapStruct con venueId y categoryIds")
    void create_MapeoBidireccionalMapStruct_PersisteRelacionesYRetornaResponseDTO() throws Exception {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setNombre("Lollapalooza 2026");
        dto.setFecha(LocalDate.now().plusMonths(2));
        dto.setDescripcion("Festival de tres días");
        dto.setVenueId(venue.getId());
        dto.setCategoryIds(List.of(catRock.getId(), catPop.getId()));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Lollapalooza 2026"))
                .andExpect(jsonPath("$.venueName").value("Movistar Arena"))
                .andExpect(jsonPath("$.categoryNames", hasItems("Pop", "Rock")));
    }

    @Test
    @DisplayName("Escenario 7: Coexistencia con listado masivo vía Record EventSummaryDTO")
    void search_ListadoMasivo_UsaRecordEventSummaryDTO() throws Exception {
        Event event = new Event(null, "Recital Acústico", LocalDate.now().plusDays(10), "desc", venue);
        event.getCategories().add(catRock);
        eventRepository.save(event);

        mockMvc.perform(get("/api/events?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreEvento").exists())
                .andExpect(jsonPath("$.content[0].nombreLugar").value("Movistar Arena"))
                .andExpect(jsonPath("$.content[0].ciudad").value("Bogotá"));
    }
}
