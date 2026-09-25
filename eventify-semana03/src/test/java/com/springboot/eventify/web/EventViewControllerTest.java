package com.springboot.eventify.web;

import com.springboot.eventify.model.Event;
import com.springboot.eventify.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void limpiarBaseDePrueba() {
        eventRepository.deleteAll();
    }

    // Escenario 4: el Model contiene la lista de eventos necesaria para la vista
    // Escenario 1: hay eventos -> se renderiza la tabla
    @Test
    void listar_ConEventos_Retorna200VistaCorrectaYModeloConDatos() throws Exception {
        eventRepository.save(new Event(null, "Conferencia Java", LocalDate.now().plusDays(5), "desc"));
        eventRepository.save(new Event(null, "Workshop Spring", LocalDate.now().plusDays(10), "desc"));

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(model().attributeExists("eventos"))
                .andExpect(model().attribute("eventos", hasSize(2)));
    }

    // Escenario 2: catálogo vacío -> mensaje amigable, no una tabla vacía muda
    @Test
    void listar_SinEventos_MuestraMensajeDeCatalogoVacio() throws Exception {
        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(model().attribute("eventos", hasSize(0)))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("Actualmente no hay eventos programados")));
    }

    @Test
    void mostrarFormulario_Retorna200YVistaDeFormulario() throws Exception {
        mockMvc.perform(get("/admin/events/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeExists("event"));
    }

    // Escenario 3: registro exitoso -> guarda en BD y redirige (no renderiza directo)
    @Test
    void guardar_DatosValidos_GuardaYRedirigeAlListado() throws Exception {
        mockMvc.perform(post("/admin/events")
                        .param("nombre", "Charla de Arquitectura")
                        .param("fecha", LocalDate.now().plusDays(3).toString())
                        .param("descripcion", "Sesión técnica"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"))
                .andExpect(flash().attributeExists("mensaje"));

        assertEquals(1, eventRepository.count());
    }

    // Datos inválidos -> NO redirige; vuelve a mostrar el formulario con errores
    @Test
    void guardar_NombreVacio_ReRenderizaFormularioConErrores() throws Exception {
        mockMvc.perform(post("/admin/events")
                        .param("nombre", "")
                        .param("fecha", LocalDate.now().plusDays(3).toString())
                        .param("descripcion", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeHasFieldErrors("event", "nombre"));

        assertEquals(0, eventRepository.count());
    }

    private void assertEquals(long esperado, long actual) {
        org.junit.jupiter.api.Assertions.assertEquals(esperado, actual);
    }
}
