package com.springboot.eventify.service;

import com.springboot.eventify.dto.VenueCreateDTO;
import com.springboot.eventify.dto.VenueResponseDTO;
import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.mapper.VenueMapper;
import com.springboot.eventify.model.Venue;
import com.springboot.eventify.repository.VenueRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    public VenueService(VenueRepository venueRepository, VenueMapper venueMapper) {
        this.venueRepository = venueRepository;
        this.venueMapper = venueMapper;
    }

    @Transactional
    public VenueResponseDTO saveDTO(VenueCreateDTO dto) {
        Venue venue = venueMapper.toEntity(dto);
        venue.setId(null);
        validar(venue);
        Venue saved = venueRepository.save(venue);
        return venueMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public VenueResponseDTO findByIdDTO(Long id) {
        Venue venue = findById(id);
        return venueMapper.toResponseDTO(venue);
    }

    @Transactional(readOnly = true)
    public Page<VenueResponseDTO> findAllDTO(Pageable pageable) {
        return venueRepository.findAll(pageable).map(venueMapper::toResponseDTO);
    }

    @Transactional
    public VenueResponseDTO updateDTO(Long id, VenueCreateDTO dto) {
        Venue venueExistente = findById(id);
        venueMapper.updateEntityFromDTO(dto, venueExistente);
        validar(venueExistente);
        Venue saved = venueRepository.save(venueExistente);
        return venueMapper.toResponseDTO(saved);
    }

    public Venue save(Venue venue) {
        venue.setId(null);
        validar(venue);
        return venueRepository.save(venue);
    }

    public Page<Venue> findAll(Pageable pageable) {
        return venueRepository.findAll(pageable);
    }

    public Venue findById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El lugar con ID " + id + " no existe"));
    }

    public List<Venue> findAll() {
        return venueRepository.findAll(Sort.by("nombre"));
    }

    public Venue update(Long id, Venue datosActualizados) {
        Venue venueExistente = findById(id);
        validar(datosActualizados);

        venueExistente.setNombre(datosActualizados.getNombre());
        venueExistente.setDireccion(datosActualizados.getDireccion());
        venueExistente.setCiudad(datosActualizados.getCiudad());
        venueExistente.setCapacidad(datosActualizados.getCapacidad());

        return venueRepository.save(venueExistente);
    }

    public void delete(Long id) {
        Venue venue = findById(id);
        try {
            venueRepository.delete(venue);
        } catch (DataIntegrityViolationException ex) {
            throw new InvalidDataException(
                    "No se puede eliminar un lugar que tiene eventos asociados");
        }
    }

    private void validar(Venue venue) {
        if (venue.getNombre() == null || venue.getNombre().trim().isEmpty()) {
            throw new InvalidDataException("El nombre del lugar no puede estar vacío");
        }
        if (venue.getDireccion() == null || venue.getDireccion().trim().isEmpty()) {
            throw new InvalidDataException("La dirección del lugar no puede ir vacía");
        }
        if (venue.getCiudad() == null || venue.getCiudad().trim().isEmpty()) {
            throw new InvalidDataException("La ciudad del lugar no puede ir vacía");
        }
        if (venue.getCapacidad() == null || venue.getCapacidad() <= 0) {
            throw new InvalidDataException("La capacidad debe ser un número entero mayor a 0");
        }
    }
}
