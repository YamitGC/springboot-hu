package com.springboot.eventify.repository;

import com.springboot.eventify.model.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void save_PersistsEventWithGeneratedId() {
        // Arrange
        Event event = new Event(null, "Charla de Arquitectura", LocalDate.of(2026, 12, 1), "Sesión técnica sobre diseño de software");

        // Act
        Event saved = eventRepository.save(event);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Charla de Arquitectura", saved.getNombre());
    }

    @Test
    void findByNombreContaining_ReturnsOnlyMatchingEvents() {
        // Arrange
        eventRepository.save(new Event(null, "Conferencia Java", LocalDate.of(2026, 10, 10), "Desc"));
        eventRepository.save(new Event(null, "Workshop Java Avanzado", LocalDate.of(2026, 10, 15), "Desc"));
        eventRepository.save(new Event(null, "Taller de Python", LocalDate.of(2026, 10, 20), "Desc"));
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Event> resultado = eventRepository.findByNombreContaining("Java", pageable);

        // Assert
        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    void deleteById_RemovesEventFromDatabase() {
        // Arrange
        Event saved = eventRepository.save(new Event(null, "Evento temporal", LocalDate.of(2026, 9, 1), "Desc"));        Long id = saved.getId();

        // Act
        eventRepository.deleteById(id);

        // Assert
        assertTrue(eventRepository.findById(id).isEmpty());
    }
}
