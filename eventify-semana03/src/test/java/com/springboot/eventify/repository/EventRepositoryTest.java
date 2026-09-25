package com.springboot.eventify.repository;

import com.springboot.eventify.model.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void save_PersistsEventWithGeneratedId() {
        // Arrange
        Event event = new Event(null, "Charla de Arquitectura", "2026-12-01", "Sesión técnica sobre diseño de software");

        // Act
        Event saved = eventRepository.save(event);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Charla de Arquitectura", saved.getNombre());
    }

    @Test
    void findByNombreContaining_ReturnsOnlyMatchingEvents() {
        // Arrange
        eventRepository.save(new Event(null, "Conferencia Java", "2026-10-10", "Desc"));
        eventRepository.save(new Event(null, "Workshop Java Avanzado", "2026-10-15", "Desc"));
        eventRepository.save(new Event(null, "Taller de Python", "2026-10-20", "Desc"));
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Event> resultado = eventRepository.findByNombreContaining("Java", pageable);

        // Assert
        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    void deleteById_RemovesEventFromDatabase() {
        // Arrange
        Event saved = eventRepository.save(new Event(null, "Evento temporal", "2026-09-01", "Desc"));
        Long id = saved.getId();

        // Act
        eventRepository.deleteById(id);

        // Assert
        assertTrue(eventRepository.findById(id).isEmpty());
    }
}
