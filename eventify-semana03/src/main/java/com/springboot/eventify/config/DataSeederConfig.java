package com.springboot.eventify.config;

import com.springboot.eventify.service.*;
import com.springboot.eventify.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataSeederConfig {

    @Bean
    public boolean seedInitialData(EventService eventService, VenueService venueService) {
        // --- 15 Lugares (Venues) ---
        venueService.save(new Venue(null, "Centro de Convenciones Metropolitano", "Av. El Sol 123", 1200));
        venueService.save(new Venue(null, "Auditorio Innovación Tech", "Calle 72 #45-10", 350));
        venueService.save(new Venue(null, "Pabellón de Cristal", "Malecón del Río Sector 2", 800));
        venueService.save(new Venue(null, "Teatro Universitario Santander", "Carrera 30 #45-03", 650));
        venueService.save(new Venue(null, "Arena Bicentenario", "Diagonal 68 #22-15", 5000));
        venueService.save(new Venue(null, "Espacio Coworking Hub Labs", "Calle 100 #15-32 Piso 4", 80));
        venueService.save(new Venue(null, "Hotel Grand Plaza Ballroom", "Av. Circunvalar #88-20", 450));
        venueService.save(new Venue(null, "Centro Cultural Las Bóvedas", "Plaza Bolívar #4-12", 200));
        venueService.save(new Venue(null, "Campus Tecnológico OpenSpace", "Autopista Norte Km 14", 500));
        venueService.save(new Venue(null, "Auditorio Central de Negocios", "Calle 53 #10-60", 250));
        venueService.save(new Venue(null, "Sala Multimedia Nexus", "Carrera 15 #93-08", 120));
        venueService.save(new Venue(null, "Parque Empresarial Auditorio A", "Av. Esperanza #68B-85", 300));
        venueService.save(new Venue(null, "Estudio Creativo La Fábrica", "Calle 4sur #43A-195", 160));
        venueService.save(new Venue(null, "Centro Empresarial Torre Norte", "Carrera 7 #116-50", 180));
        venueService.save(new Venue(null, "Coliseo Deportivo y Eventos", "Av. Deportistas Transversal 12", 3500));

        // --- 15 Eventos (Events) ---
        eventService.save(new Event(null, "Conferencia Tech 2026", LocalDate.of(2026, 10, 15), "Encuentro anual de arquitectura de software e IA"));
        eventService.save(new Event(null, "Workshop Spring Boot & JPA", LocalDate.of(2026, 10, 22), "Taller intensivo de backend moderno y microservicios"));
        eventService.save(new Event(null, "DevOps Days Latam", LocalDate.of(2026, 11, 5), "Charlas sobre CI/CD, Kubernetes y contenedores"));
        eventService.save(new Event(null, "Hackathon Universitario 48h", LocalDate.of(2026, 11, 14), "Competencia de desarrollo de soluciones tecnológicas"));
        eventService.save(new Event(null, "Cumbre de Ciberseguridad", LocalDate.of(2026, 11, 28), "Estrategias de defensa, pentesting y seguridad cloud"));
        eventService.save(new Event(null, "Foro de Emprendimiento Digital", LocalDate.of(2026, 12, 3), "Panel con fundadores de startups tecnológicas"));
        eventService.save(new Event(null, "Bootcamp de Testing Automatizado", LocalDate.of(2026, 12, 10), "Sesión práctica de pruebas unitarias y de integración"));
        eventService.save(new Event(null, "Feria de Innovación Abierta", LocalDate.of(2026, 12, 18), "Exposición de proyectos universitarios y empresas"));
        eventService.save(new Event(null, "Data Science & AI Summit", LocalDate.of(2027, 1, 15), "Tendencias en modelos de lenguaje y pipelines de datos"));
        eventService.save(new Event(null, "Seminario de Diseño de APIs RESTful", LocalDate.of(2027, 1, 25), "Buenas prácticas, estándares y documentación OpenAPI"));
        eventService.save(new Event(null, "Encuentro de Comunidades Open Source", LocalDate.of(2027, 2, 8), "Mesa redonda sobre contribución y licencias libres"));
        eventService.save(new Event(null, "Simposio de Bases de Datos Distribuidas", LocalDate.of(2027, 2, 19), "Optimización SQL, replicación y almacenamiento NoSQL"));
        eventService.save(new Event(null, "Congreso de Transformación Ágil", LocalDate.of(2027, 3, 5), "Marcos Scrum, Kanban y dinámicas de equipo ágil"));
        eventService.save(new Event(null, "Taller de UI/UX para Desarrolladores", LocalDate.of(2027, 3, 16), "Principios de usabilidad y diseño de interfaces accesibles"));
        eventService.save(new Event(null, "Gala Anual de la Industria de Software", LocalDate.of(2027, 3, 27), "Cierre de año y premiación a desarrollos destacados"));

        return true;
    }
}