package com.springboot.eventify.service;

import com.springboot.eventify.model.Venue;
import com.springboot.eventify.repository.VenueRepository;
import com.springboot.eventify.exception.InvalidDataException;

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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    private Venue validVenue;

    @BeforeEach
    void setUp() {
        validVenue = new Venue(null, "Auditorio Principal", "Calle 50 #20-10", 300);
    }

    @Test
    void save_ValidVenue_ReturnsSavedVenue() {
        // Arrange (Preparar)
        Venue savedMock = new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", 300);
        when(venueRepository.save(validVenue)).thenReturn(savedMock);

        // Act (Actuar)
        Venue result = venueService.save(validVenue);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Auditorio Principal", result.getNombre());
        verify(venueRepository, times(1)).save(validVenue);
    }

    @Test
    void save_EmptyName_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Venue invalidVenue = new Venue(null, "", "Calle 50 #20-10", 300);

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> venueService.save(invalidVenue));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void findAll_ReturnsPageOfVenues() {
        // Arrange (Preparar)
        Pageable pageable = PageRequest.of(0, 10);
        List<Venue> mockList = new ArrayList<>();
        mockList.add(new Venue(1L, "Lugar A", "Dirección A", 100));
        Page<Venue> mockPage = new PageImpl<>(mockList, pageable, 1);
        when(venueRepository.findAll(pageable)).thenReturn(mockPage);

        // Act (Actuar)
        Page<Venue> result = venueService.findAll(pageable);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(venueRepository, times(1)).findAll(pageable);
    }
}