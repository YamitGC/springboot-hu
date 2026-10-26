package com.springboot.eventify.repository;

import com.springboot.eventify.dto.EventSummaryDTO;
import com.springboot.eventify.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // ------------------------------------------------------------------
    // 1) Motor combinado del panel y de la API: DTO + JOIN + Slice (1 consulta)
    //    Los parámetros de texto llegan ya en minúsculas y con % y _ escapados (ver EventService)
    // ------------------------------------------------------------------
    @Query("""
            SELECT new com.springboot.eventify.dto.EventSummaryDTO(
                       e.id, e.nombre, e.fecha, v.nombre, v.ciudad)
            FROM Event e
            JOIN e.venue v
            WHERE LOWER(v.ciudad) LIKE CONCAT('%', :ciudad, '%') ESCAPE '\\'
              AND v.capacidad >= :capacidadMin
              AND e.fecha BETWEEN :desde AND :hasta
              AND (:categoria = '' OR EXISTS (
                      SELECT c.id FROM e.categories c
                      WHERE LOWER(c.nombre) LIKE CONCAT('%', :categoria, '%') ESCAPE '\\'))
            ORDER BY e.fecha DESC, e.id DESC
            """)
    Slice<EventSummaryDTO> buscar(@Param("ciudad") String ciudad,
                                  @Param("categoria") String categoria,
                                  @Param("capacidadMin") Integer capacidadMin,
                                  @Param("desde") LocalDate desde,
                                  @Param("hasta") LocalDate hasta,
                                  Pageable pageable);

    // ------------------------------------------------------------------
    // 2) Carga puntual con TODO su grafo en una sola consulta (detalle / edición)
    // ------------------------------------------------------------------
    @EntityGraph(attributePaths = {"venue", "categories"})
    Optional<Event> findWithRelationsById(Long id);

    // ------------------------------------------------------------------
    // 3) Segundo paso del patrón "dos pasos": trae venue + categorías de una lista de ids
    //    con JOIN FETCH (sin paginación, por eso es seguro fetchear la colección)
    // ------------------------------------------------------------------
    @Query("""
            SELECT DISTINCT e FROM Event e
            JOIN FETCH e.venue
            LEFT JOIN FETCH e.categories
            WHERE e.id IN :ids
            """)
    List<Event> findAllWithRelationsByIdIn(@Param("ids") Collection<Long> ids);

    // ------------------------------------------------------------------
    // 4) Finders por criterio individual (Task 3): parciales, insensibles a mayúsculas
    //    y ordenados por fecha DESC. Solo se hace fetch de 'venue' (ToOne) para que la
    //    paginación ocurra en SQL; las categorías se cargan en el segundo paso.
    // ------------------------------------------------------------------
    @EntityGraph(attributePaths = "venue")
    Slice<Event> findByVenueCiudadContainingIgnoreCaseOrderByFechaDesc(String ciudad, Pageable pageable);

    @EntityGraph(attributePaths = "venue")
    Slice<Event> findByVenueCapacidadGreaterThanEqualOrderByFechaDesc(Integer capacidad, Pageable pageable);

    @EntityGraph(attributePaths = "venue")
    Slice<Event> findByFechaBetweenOrderByFechaDesc(LocalDate desde, LocalDate hasta, Pageable pageable);

    // Por categoría: un finder derivado con "Categories" haría JOIN sobre la colección y
    // DUPLICARÍA el evento si dos de sus categorías coinciden con el texto (p. ej. "Rock" y
    // "Rock alternativo"). Con EXISTS cada evento aparece una sola vez.
    @Query("""
            SELECT e FROM Event e
            JOIN FETCH e.venue
            WHERE EXISTS (SELECT c.id FROM e.categories c
                          WHERE LOWER(c.nombre) LIKE CONCAT('%', :categoria, '%') ESCAPE '\\')
            ORDER BY e.fecha DESC, e.id DESC
            """)
    Slice<Event> findByCategoriaNombreOrderByFechaDesc(@Param("categoria") String categoria, Pageable pageable);
}
