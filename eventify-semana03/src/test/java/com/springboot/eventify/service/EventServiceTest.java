package com.springboot.eventify.service;

import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.model.Event;
import com.springboot.eventify.repository.EventRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event validEvent;

    @BeforeEach
    void setUp() {
        validEvent = new Event(null, "Conferencia Java", LocalDate.of(2026, 10, 10), "Charla técnica de backend");
    }

    @Test
    void save_ValidEvent_ReturnsSavedEvent() {
        // Arrange (Preparar)
        Event savedMock = new Event(1L, "Conferencia Java", LocalDate.of(2026, 10, 10), "Charla técnica de backend");
        when(eventRepository.save(validEvent)).thenReturn(savedMock);

        // Act (Actuar)
        Event result = eventService.save(validEvent);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Conferencia Java", result.getNombre());
        verify(eventRepository, times(1)).save(validEvent);
    }

    @Test
    void save_EmptyName_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Event invalidEvent = new Event(null, "   ", LocalDate.of(2026, 10, 10), "Descripción");

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_NullName_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Event invalidEvent = new Event(null, null, LocalDate.of(2026, 10, 10), "Descripción");

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void findAll_ReturnsPageOfEvents() {
        // Arrange (Preparar)
        Pageable pageable = PageRequest.of(0, 10);
        List<Event> mockList = new ArrayList<>();
        mockList.add(new Event(1L, "Evento 1", LocalDate.of(2026, 10, 10), "Desc 1"));
        Page<Event> mockPage = new PageImpl<>(mockList, pageable, 1);
        when(eventRepository.findAll(pageable)).thenReturn(mockPage);

        // Act (Actuar)
        Page<Event> result = eventService.findAll(pageable);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(eventRepository, times(1)).findAll(pageable);
    }

    @Test
    void findById_ExistingId_ReturnsEvent() {
        // Arrange
        Event event = new Event(1L, "Conferencia Java", LocalDate.of(2026, 10, 10), "Desc");        when(eventRepository.findById(1L)).thenReturn(java.util.Optional.of(event));

        // Act
        Event result = eventService.findById(1L);

        // Assert
        assertEquals("Conferencia Java", result.getNombre());
    }

    @Test
    void findById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(eventRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventService.findById(99L));
    }

    @Test
    void delete_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(eventRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventService.delete(99L));
        verify(eventRepository, never()).delete(any());
    }

}
