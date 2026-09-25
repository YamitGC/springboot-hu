package com.springboot.eventify.repository;

import com.springboot.eventify.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Consulta derivada
    Page<Event> findByNombreContaining(String nombre, Pageable pageable);
}