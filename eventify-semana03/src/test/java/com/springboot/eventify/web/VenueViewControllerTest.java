package com.springboot.eventify.web;

import com.springboot.eventify.model.Venue;
import com.springboot.eventify.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VenueViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VenueRepository venueRepository;

    @BeforeEach
    void limpiarBaseDePrueba() {
        venueRepository.deleteAll();
    }

    // Escenario 4: el Model contiene la lista de lugares necesaria para la vista
    // Escenario 1: hay lugares -> se renderiza la tabla
    @Test
    void listar_ConLugares_Retorna200VistaCorrectaYModeloConDatos() throws Exception {
        venueRepository.save(new Venue(null, "Auditorio Central", "Calle 10 #20-30", 200));
        venueRepository.save(new Venue(null, "Sala Norte", "Carrera 5 #15-40", 80));

        mockMvc.perform(get("/admin/venues"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/list"))
                .andExpect(model().attributeExists("lugares"))
                .andExpect(model().attribute("lugares", hasSize(2)));
    }

    // Escenario 2: catálogo vacío -> mensaje amigable, no una tabla vacía muda
    @Test
    void listar_SinLugares_MuestraMensajeDeCatalogoVacio() throws Exception {
        mockMvc.perform(get("/admin/venues"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/list"))
                .andExpect(model().attribute("lugares", hasSize(0)))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("Actualmente no hay lugares registrados")));
    }

    @Test
    void mostrarFormulario_Retorna200YVistaDeFormulario() throws Exception {
        mockMvc.perform(get("/admin/venues/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/form"))
                .andExpect(model().attributeExists("venue"));
    }

    // Escenario 3: registro exitoso -> guarda en BD y redirige (no renderiza directo)
    @Test
    void guardar_DatosValidos_GuardaYRedirigeAlListado() throws Exception {
        mockMvc.perform(post("/admin/venues")
                        .param("nombre", "Centro de Eventos Sur")
                        .param("direccion", "Av. Siempre Viva 742")
                        .param("capacidad", "150"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/venues"))
                .andExpect(flash().attributeExists("mensaje"));

        assertEquals(1, venueRepository.count());
    }

    // Datos inválidos -> NO redirige; vuelve a mostrar el formulario con errores
    @Test
    void guardar_NombreVacio_ReRenderizaFormularioConErrores() throws Exception {
        mockMvc.perform(post("/admin/venues")
                        .param("nombre", "")
                        .param("direccion", "Av. Siempre Viva 742")
                        .param("capacidad", "150"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/form"))
                .andExpect(model().attributeHasFieldErrors("venue", "nombre"));

        assertEquals(0, venueRepository.count());
    }

    private void assertEquals(long esperado, long actual) {
        org.junit.jupiter.api.Assertions.assertEquals(esperado, actual);
    }
}