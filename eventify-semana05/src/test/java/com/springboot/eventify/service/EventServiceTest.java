package com.springboot.eventify.service;

import com.springboot.eventify.dto.EventDetailDTO;
import com.springboot.eventify.dto.EventRequest;
import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.model.Category;
import com.springboot.eventify.model.Event;
import com.springboot.eventify.model.Venue;
import com.springboot.eventify.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private VenueService venueService;
    @Mock private CategoryService categoryService;
    @Mock private com.springboot.eventify.mapper.EventMapper eventMapper;

    @InjectMocks private EventService eventService;

    private Venue venue;
    private Category category;
    private EventRequest request;

    @BeforeEach
    void setUp() {
        venue = new Venue(1L, "Arena Bicentenario", "Diagonal 68", "Bogotá", 5000);
        category = new Category(1L, "Rock", "Música rock");

        request = new EventRequest();
        request.setNombre("Conferencia Java");
        request.setFecha(LocalDate.of(2026, 10, 10));
        request.setDescripcion("Charla técnica de backend");
        request.setVenueId(1L);
        request.setCategoryIds(List.of(1L));
    }

    @Test
    void save_ValidRequest_AssignsVenueAndCategories() {
        when(venueService.findById(1L)).thenReturn(venue);
        when(categoryService.findAllByIds(List.of(1L))).thenReturn(Set.of(category));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.save(request);

        assertEquals("Conferencia Java", result.getNombre());
        assertSame(venue, result.getVenue());
        assertTrue(result.getCategories().contains(category));
        assertTrue(result.isActivo());
    }

    @Test
    void save_EmptyName_ThrowsInvalidDataException() {
        request.setNombre("   ");

        assertThrows(InvalidDataException.class, () -> eventService.save(request));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_WithoutVenue_ThrowsInvalidDataException() {
        request.setVenueId(null);

        assertThrows(InvalidDataException.class, () -> eventService.save(request));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void softDelete_ExistingEvent_DeactivatesInsteadOfDeleting() {
        Event event = new Event(1L, "Evento", LocalDate.of(2026, 10, 10), "Desc", venue);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        eventService.softDelete(1L);

        assertFalse(event.isActivo());
        verify(eventRepository).save(event);
        verify(eventRepository, never()).delete(any());
        verify(eventRepository, never()).deleteById(any());
    }

    @Test
    void softDelete_NonExistingId_ThrowsResourceNotFoundException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.softDelete(99L));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void search_NormalizesFiltersAndAppliesDefaults() {
        when(eventRepository.buscar(eq("bog"), eq("rock"), eq(0),
                eq(LocalDate.of(1900, 1, 1)), eq(LocalDate.of(9999, 12, 31)), any()))
                .thenReturn(new SliceImpl<>(List.of()));

        eventService.search("  BOG ", " Rock", null, null, null, PageRequest.of(0, 10));

        verify(eventRepository).buscar(eq("bog"), eq("rock"), eq(0),
                eq(LocalDate.of(1900, 1, 1)), eq(LocalDate.of(9999, 12, 31)), any());
    }

    @Test
    void search_EscapesLikeWildcards() {
        when(eventRepository.buscar(eq("100\\%"), eq("a\\_b"), eq(0), any(), any(), any()))
                .thenReturn(new SliceImpl<>(List.of()));

        eventService.search("100%", "a_b", null, null, null, PageRequest.of(0, 10));

        verify(eventRepository).buscar(eq("100\\%"), eq("a\\_b"), eq(0), any(), any(), any());
    }

    @Test
    void findByCiudad_UsaDosPasosYConservaElOrden() {
        Event e1 = new Event(1L, "Reciente", LocalDate.of(2026, 12, 1), "d", venue);
        Event e2 = new Event(2L, "Antiguo", LocalDate.of(2026, 1, 1), "d", venue);
        when(eventRepository.findByVenueCiudadContainingIgnoreCaseOrderByFechaDesc(eq("bog"), any()))
                .thenReturn(new SliceImpl<>(List.of(e1, e2)));
        // el segundo paso devuelve los eventos en OTRO orden: el servicio debe restaurar el original
        when(eventRepository.findAllWithRelationsByIdIn(List.of(1L, 2L))).thenReturn(List.of(e2, e1));

        Slice<EventDetailDTO> r = eventService.findByCiudad(" BOG ", PageRequest.of(0, 10));

        assertEquals(2, r.getContent().size());
        assertEquals("Reciente", r.getContent().get(0).nombre());
        assertEquals("Antiguo", r.getContent().get(1).nombre());
        verify(eventRepository, times(1)).findAllWithRelationsByIdIn(any());   // una sola consulta de relaciones
    }

    @Test
    void findByRangoFechas_DesdePosteriorAHasta_ThrowsInvalidDataException() {
        assertThrows(InvalidDataException.class, () -> eventService.findByRangoFechas(
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 1, 1), PageRequest.of(0, 10)));
        verifyNoInteractions(eventRepository);
    }
}
