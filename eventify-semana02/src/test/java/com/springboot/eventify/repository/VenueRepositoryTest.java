package com.springboot.eventify.repository;

import com.springboot.eventify.model.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VenueRepositoryTest {

    @Autowired
    private VenueRepository venueRepository;

    @Test
    void save_PersistsVenueWithGeneratedId() {
        // Arrange
        Venue venue = new Venue(null, "Sala Norte", "Carrera 10 #20-30", 80);

        // Act
        Venue saved = venueRepository.save(venue);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(80, saved.getCapacidad());
    }

    @Test
    void findByNombreContaining_ReturnsMatchingVenues() {
        // Arrange
        venueRepository.save(new Venue(null, "Auditorio Central", "Dir 1", 200));
        venueRepository.save(new Venue(null, "Auditorio Norte", "Dir 2", 100));
        venueRepository.save(new Venue(null, "Salón Sur", "Dir 3", 50));
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Venue> resultado = venueRepository.findByNombreContaining("Auditorio", pageable);

        // Assert
        assertEquals(2, resultado.getTotalElements());
    }
}