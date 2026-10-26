package com.springboot.eventify.service;

import com.springboot.eventify.dto.EventDetailDTO;
import com.springboot.eventify.dto.EventRequest;
import com.springboot.eventify.dto.EventSummaryDTO;
import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.model.Event;
import com.springboot.eventify.repository.EventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventService {

    // Valores "centinela": evitan parámetros nulos en la consulta (PostgreSQL no infiere su tipo)
    private static final LocalDate FECHA_MIN = LocalDate.of(1900, 1, 1);
    private static final LocalDate FECHA_MAX = LocalDate.of(9999, 12, 31);

    private final EventRepository eventRepository;
    private final VenueService venueService;
    private final CategoryService categoryService;

    public EventService(EventRepository eventRepository,
                        VenueService venueService,
                        CategoryService categoryService) {
        this.eventRepository = eventRepository;
        this.venueService = venueService;
        this.categoryService = categoryService;
    }

    @Transactional
    public Event save(EventRequest request) {
        Event event = new Event();
        aplicar(event, request);
        return eventRepository.save(event);
    }

    // ---------- Listado principal (Records, 1 consulta, Slice) ----------
    @Transactional(readOnly = true)
    public Slice<EventSummaryDTO> search(String ciudad, String categoria, Integer capacidadMin,
                                         LocalDate desde, LocalDate hasta, Pageable pageable) {
        return eventRepository.buscar(
                paraLike(ciudad),
                paraLike(categoria),
                capacidadMin != null ? capacidadMin : 0,
                desde != null ? desde : FECHA_MIN,
                hasta != null ? hasta : FECHA_MAX,
                sinOrden(pageable));
    }

    // ---------- Finders por criterio (dos pasos: página + relaciones) ----------
    @Transactional(readOnly = true)
    public Slice<EventDetailDTO> findByCiudad(String ciudad, Pageable pageable) {
        return conRelaciones(eventRepository
                .findByVenueCiudadContainingIgnoreCaseOrderByFechaDesc(normalizar(ciudad), sinOrden(pageable)));
    }

    @Transactional(readOnly = true)
    public Slice<EventDetailDTO> findByCategoria(String categoria, Pageable pageable) {
        return conRelaciones(eventRepository
                .findByCategoriaNombreOrderByFechaDesc(paraLike(categoria), sinOrden(pageable)));
    }

    @Transactional(readOnly = true)
    public Slice<EventDetailDTO> findByCapacidad(Integer capacidadMin, Pageable pageable) {
        if (capacidadMin == null || capacidadMin < 0) {
            throw new InvalidDataException("La capacidad mínima debe ser un número mayor o igual a 0");
        }
        return conRelaciones(eventRepository
                .findByVenueCapacidadGreaterThanEqualOrderByFechaDesc(capacidadMin, sinOrden(pageable)));
    }

    @Transactional(readOnly = true)
    public Slice<EventDetailDTO> findByRangoFechas(LocalDate desde, LocalDate hasta, Pageable pageable) {
        if (desde == null || hasta == null) {
            throw new InvalidDataException("Debes indicar las fechas 'desde' y 'hasta'");
        }
        if (desde.isAfter(hasta)) {
            throw new InvalidDataException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        return conRelaciones(eventRepository
                .findByFechaBetweenOrderByFechaDesc(desde, hasta, sinOrden(pageable)));
    }

    @Transactional(readOnly = true)
    public Event findById(Long id) {
        return obtenerConRelaciones(id);
    }

    @Transactional
    public Event update(Long id, EventRequest request) {
        Event event = obtenerConRelaciones(id);
        aplicar(event, request);
        return eventRepository.save(event);
    }

    /** Borrado lógico: el evento pasa a activo = false; la fila NO se elimina. */
    @Transactional
    public void softDelete(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        event.deactivate();
        eventRepository.save(event);
    }

    // ----------------- privados -----------------

    /**
     * Segundo paso del patrón "dos pasos": con los eventos de la página (que ya traen el venue)
     * hace UNA consulta con JOIN FETCH para traer sus categorías, y conserva el orden original.
     */
    private Slice<EventDetailDTO> conRelaciones(Slice<Event> pagina) {
        List<Long> ids = pagina.getContent().stream().map(Event::getId).toList();
        if (ids.isEmpty()) {
            return new SliceImpl<>(List.of(), pagina.getPageable(), false);
        }
        Map<Long, Event> porId = eventRepository.findAllWithRelationsByIdIn(ids).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        List<EventDetailDTO> contenido = ids.stream()
                .map(porId::get)
                .map(EventDetailDTO::from)
                .toList();
        return new SliceImpl<>(contenido, pagina.getPageable(), pagina.hasNext());
    }

    private Event obtenerConRelaciones(Long id) {
        return eventRepository.findWithRelationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
    }

    private void aplicar(Event event, EventRequest request) {
        validar(request);
        event.setNombre(request.getNombre());
        event.setFecha(request.getFecha());
        event.setDescripcion(request.getDescripcion());
        event.setVenue(venueService.findById(request.getVenueId()));
        event.setCategories(categoryService.findAllByIds(request.getCategoryIds()));
    }

    // Pageable SIN orden: el ORDER BY fecha DESC ya vive en la consulta / en el nombre del finder
    private Pageable sinOrden(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }

    /** trim + minúsculas (null -> ""). */
    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase();
    }

    /** Igual que normalizar, pero además escapa \, % y _ para usarlo en un LIKE ... ESCAPE '\'. */
    private String paraLike(String texto) {
        return normalizar(texto)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private void validar(EventRequest request) {
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new InvalidDataException("El nombre del evento no puede estar vacío");
        }
        if (request.getFecha() == null) {
            throw new InvalidDataException("La fecha del evento no puede ir vacía");
        }
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            throw new InvalidDataException("La descripcion del evento no puede estar vacía");
        }
        if (request.getVenueId() == null) {
            throw new InvalidDataException("El evento debe estar asociado a un lugar");
        }
    }
}
