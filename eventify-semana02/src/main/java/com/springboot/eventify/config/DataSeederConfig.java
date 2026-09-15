package com.springboot.eventify.config;

import com.springboot.eventify.service.*;
import com.springboot.eventify.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeederConfig {

    @Bean
    public boolean seedInitialData(EventService eventService, VenueService venueService) {
        venueService.save(new Venue(null, "Centro de Convenciones Principal", "Av. El Sol 123", 500));
        venueService.save(new Venue(null, "Auditorio Tecnológico", "Calle Innovación 456", 150));

        eventService.save(new Event(null, "Conferencia Tech 2026", "2026-10-15", "Encuentro anual de desarrollo"));
        eventService.save(new Event(null, "Workshop Spring Boot", "2026-11-20", "Taller práctico de backend"));

        return true;
    }
}