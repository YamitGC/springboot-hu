package com.springboot.eventify.web;

import com.springboot.eventify.model.Category;
import com.springboot.eventify.model.Event;
import com.springboot.eventify.model.Venue;
import com.springboot.eventify.repository.CategoryRepository;
import com.springboot.eventify.repository.EventRepository;
import com.springboot.eventify.repository.VenueRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional   // rollback automático: cada prueba empieza con la base limpia
class EventViewControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EventRepository eventRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EntityManager em;

    private Venue bogota;
    private Category rock;

    @BeforeEach
    void setUp() {
        bogota = venueRepository.save(new Venue(null, "Arena Bicentenario", "Diagonal 68", "Bogotá", 5000));
        rock = categoryRepository.save(new Category(null, "Rock", "Música rock"));
    }

    private Event crearEvento(String nombre, LocalDate fecha, Venue venue) {
        Event e = new Event(null, nombre, fecha, "desc", venue);
        e.getCategories().add(rock);
        return eventRepository.save(e);
    }

    @Test
    void listar_ConEventos_Retorna200VistaCorrectaYModeloConDatos() throws Exception {
        crearEvento("Conferencia Java", LocalDate.now().plusDays(5), bogota);
        crearEvento("Workshop Spring", LocalDate.now().plusDays(10), bogota);

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(model().attribute("eventos", hasSize(2)))
                .andExpect(model().attributeExists("pagina"));
    }

    @Test
    void listar_SinEventos_MuestraMensajeDeCatalogoVacio() throws Exception {
        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("eventos", hasSize(0)))
                .andExpect(content().string(containsString("Actualmente no hay eventos programados")));
    }

    @Test
    void listar_FiltroParcialEInsensible_EncuentraElEvento() throws Exception {
        crearEvento("Concierto de ROCK", LocalDate.now().plusDays(5), bogota);

        mockMvc.perform(get("/admin/events").param("ciudad", "bog").param("categoria", "ROCK"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("eventos", hasSize(1)));

        mockMvc.perform(get("/admin/events").param("ciudad", "medell"))
                .andExpect(model().attribute("eventos", hasSize(0)));
    }

    @Test
    void listar_ConSiguientePagina_LaUrlConservaLosFiltros() throws Exception {
        for (int i = 1; i <= 3; i++) {
            crearEvento("Evento " + i, LocalDate.now().plusDays(i), bogota);
        }

        mockMvc.perform(get("/admin/events").param("size", "2").param("ciudad", "bog"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("urlSiguiente", containsString("page=1")))
                .andExpect(model().attribute("urlSiguiente", containsString("ciudad=bog")))
                .andExpect(model().attributeDoesNotExist("urlAnterior"));
    }

    @Test
    void mostrarFormulario_IncluyeLugaresYCategoriasParaLosControles() throws Exception {
        mockMvc.perform(get("/admin/events/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeExists("event", "venues", "categorias"));
    }

    @Test
    void guardar_DatosValidos_GuardaYRedirigeAlListado() throws Exception {
        mockMvc.perform(post("/admin/events")
                        .param("nombre", "Charla de Arquitectura")
                        .param("fecha", LocalDate.now().plusDays(3).toString())
                        .param("descripcion", "Sesión técnica")
                        .param("venueId", String.valueOf(bogota.getId()))
                        .param("categoryIds", String.valueOf(rock.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"))
                .andExpect(flash().attributeExists("mensaje"));

        assertEquals(1, eventRepository.count());
    }

    @Test
    void guardar_SinLugar_ReRenderizaFormularioConErroresYCatalogos() throws Exception {
        mockMvc.perform(post("/admin/events")
                        .param("nombre", "Evento sin lugar")
                        .param("fecha", LocalDate.now().plusDays(3).toString())
                        .param("descripcion", "desc")
                        .param("categoryIds", String.valueOf(rock.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeHasFieldErrors("event", "venueId"))
                .andExpect(model().attributeExists("venues", "categorias"));

        assertEquals(0, eventRepository.count());
    }

    @Test
    void guardar_SinDescripcion_MuestraElErrorEnElCampo() throws Exception {
        mockMvc.perform(post("/admin/events")
                        .param("nombre", "Evento sin descripción")
                        .param("fecha", LocalDate.now().plusDays(3).toString())
                        .param("descripcion", "   ")
                        .param("venueId", String.valueOf(bogota.getId()))
                        .param("categoryIds", String.valueOf(rock.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeHasFieldErrors("event", "descripcion"));

        assertEquals(0, eventRepository.count());
    }

    @Test
    void eliminar_DesactivaElEventoPeroNoBorraLaFila() throws Exception {
        Event evento = crearEvento("Evento a eliminar", LocalDate.now().plusDays(2), bogota);
        em.flush();

        mockMvc.perform(post("/admin/events/{id}/delete", evento.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));

        em.flush();
        em.clear();
        assertEquals(0, eventRepository.count());   // invisible para la aplicación
        Number filas = (Number) em.createNativeQuery("SELECT COUNT(*) FROM events").getSingleResult();
        assertEquals(1, filas.longValue());          // pero la fila sigue en la base de datos
    }
}
