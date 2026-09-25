package com.springboot.eventify.repository;

import com.springboot.eventify.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    // Consulta derivada
    Page<Venue> findByNombreContaining(String nombre, Pageable pageable);
}
