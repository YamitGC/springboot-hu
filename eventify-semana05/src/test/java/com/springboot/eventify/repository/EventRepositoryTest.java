package com.springboot.eventify.repository;

import com.springboot.eventify.dto.EventSummaryDTO;
import com.springboot.eventify.model.Category;
import com.springboot.eventify.model.Event;
import com.springboot.eventify.model.Venue;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
// Spring Boot 4: la anotación vive en el módulo de pruebas JDBC. Si tu IDE no la encuentra,
// busca "AutoConfigureTestDatabase" y usa el import que te sugiera.
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)   // usar PostgreSQL de pruebas
class EventRepositoryTest {

    private static final LocalDate MIN = LocalDate.of(1900, 1, 1);
    private static final LocalDate MAX = LocalDate.of(9999, 12, 31);

    @Autowired private EventRepository eventRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private EntityManager em;

    private Venue bogota;

    @BeforeEach
    void setUp() {
        bogota = venueRepository.save(
                new Venue(null, "Arena Bicentenario", "Diagonal 68 #22-15", "Bogotá", 5000));
    }

    private Event nuevoEvento(String nombre, LocalDate fecha, Venue venue, String... categorias) {
        Event e = new Event(null, nombre, fecha, "Desc", venue);
        for (String c : categorias) {
            e.getCategories().add(new Category(null, c, "desc"));   // categoría NUEVA: la guarda la cascada
        }
        return e;
    }

    @Test
    void save_PersistsEventWithVenueAndCascadesNewCategories() {
        Event saved = eventRepository.save(
                nuevoEvento("Charla de Arquitectura", LocalDate.of(2026, 12, 1), bogota, "Conferencias"));
        em.flush();
        em.clear();

        Event leido = eventRepository.findWithRelationsById(saved.getId()).orElseThrow();

        assertEquals("Arena Bicentenario", leido.getVenue().getNombre());
        assertEquals(1, leido.getCategories().size());
        assertNotNull(leido.getCategories().iterator().next().getId());   // la cascada insertó la categoría
    }

    @Test
    void buscar_CiudadParcialYCategoriaEnMinusculas_EncuentraElEvento() {
        eventRepository.save(nuevoEvento("Concierto de ROCK", LocalDate.of(2026, 12, 20), bogota, "Rock"));
        eventRepository.save(nuevoEvento("Taller de Java", LocalDate.of(2026, 11, 5), bogota, "Talleres"));

        Slice<EventSummaryDTO> r = eventRepository.buscar("bog", "rock", 0, MIN, MAX, PageRequest.of(0, 10));

        assertEquals(1, r.getContent().size());
        assertEquals("Concierto de ROCK", r.getContent().get(0).nombreEvento());
        assertEquals("Bogotá", r.getContent().get(0).ciudad());
    }

    @Test
    void buscar_SinFiltros_OrdenaPorFechaDescendente() {
        eventRepository.save(nuevoEvento("Antiguo", LocalDate.of(2026, 1, 1), bogota, "Teatro"));
        eventRepository.save(nuevoEvento("Reciente", LocalDate.of(2026, 12, 1), bogota, "Deportes"));
        eventRepository.save(nuevoEvento("Medio", LocalDate.of(2026, 6, 1), bogota, "Festivales"));

        Slice<EventSummaryDTO> r = eventRepository.buscar("", "", 0, MIN, MAX, PageRequest.of(0, 10));

        assertEquals("Reciente", r.getContent().get(0).nombreEvento());
        assertEquals("Medio", r.getContent().get(1).nombreEvento());
        assertEquals("Antiguo", r.getContent().get(2).nombreEvento());
    }

    @Test
    void buscar_ConMasRegistrosQueElTamano_SliceIndicaQueHaySiguiente() {
        for (int i = 1; i <= 3; i++) {
            eventRepository.save(nuevoEvento("Evento " + i, LocalDate.of(2026, 10, i), bogota, "Cat" + i));
        }
        Pageable pagina = PageRequest.of(0, 2);

        Slice<EventSummaryDTO> r = eventRepository.buscar("", "", 0, MIN, MAX, pagina);

        assertEquals(2, r.getContent().size());
        assertTrue(r.hasNext());
        assertFalse(r.hasPrevious());
    }

    @Test
    void buscar_PorcentajeEscapado_NoActuaComoComodin() {
        eventRepository.save(nuevoEvento("Concierto", LocalDate.of(2026, 12, 20), bogota, "Rock"));

        // el servicio envía "\%" (escapado): debe buscar un % literal y NO coincidir con todo
        Slice<EventSummaryDTO> r = eventRepository.buscar("\\%", "", 0, MIN, MAX, PageRequest.of(0, 10));

        assertTrue(r.getContent().isEmpty());
    }

    @Test
    void findByCategoria_DosCategoriasCoinciden_NoDuplicaElEventoYTraeElLugar() {
        // "Rock" y "Rock alternativo" contienen "rock": un JOIN simple devolvería el evento dos veces
        eventRepository.save(nuevoEvento("Festival Rock", LocalDate.of(2026, 12, 1), bogota,
                "Rock", "Rock alternativo"));
        em.flush();
        em.clear();

        Slice<Event> r = eventRepository.findByCategoriaNombreOrderByFechaDesc("rock", PageRequest.of(0, 10));

        assertEquals(1, r.getContent().size());
        assertTrue(Hibernate.isInitialized(r.getContent().get(0).getVenue()));   // JOIN FETCH e.venue
    }

    @Test
    void findByCiudad_TraeElLugarSinConsultasExtra_YSegundoPasoTraeCategorias() {
        eventRepository.save(nuevoEvento("Concierto de ROCK", LocalDate.of(2026, 12, 20), bogota, "Rock", "Conciertos"));
        em.flush();
        em.clear();

        Slice<Event> pagina = eventRepository
                .findByVenueCiudadContainingIgnoreCaseOrderByFechaDesc("BOG", PageRequest.of(0, 10));
        assertEquals(1, pagina.getContent().size());
        assertTrue(Hibernate.isInitialized(pagina.getContent().get(0).getVenue()));   // @EntityGraph("venue")

        List<Long> ids = pagina.getContent().stream().map(Event::getId).toList();
        List<Event> completos = eventRepository.findAllWithRelationsByIdIn(ids);

        assertEquals(1, completos.size());
        assertTrue(Hibernate.isInitialized(completos.get(0).getCategories()));        // JOIN FETCH categories
        assertEquals(2, completos.get(0).getCategories().size());
    }

    @Test
    void softDelete_EventoInactivoDesapareceDeLasConsultasPeroLaFilaSigueExistiendo() {
        Event event = eventRepository.save(
                nuevoEvento("Evento temporal", LocalDate.of(2026, 9, 1), bogota, "Teatro"));
        Long id = event.getId();

        event.deactivate();
        eventRepository.save(event);
        em.flush();
        em.clear();   // limpiar la caché: sin esto findById devolvería el objeto en memoria

        assertTrue(eventRepository.findById(id).isEmpty());          // @SQLRestriction lo oculta
        assertEquals(0, eventRepository.count());

        Number filasReales = (Number) em.createNativeQuery("SELECT COUNT(*) FROM events").getSingleResult();
        assertEquals(1, filasReales.longValue());                    // la fila NO fue eliminada
    }
}
