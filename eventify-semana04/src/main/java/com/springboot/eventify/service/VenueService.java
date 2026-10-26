package com.springboot.eventify.service;

import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.repository.VenueRepository;
import com.springboot.eventify.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import java.util.List;


@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue save(Venue venue){
        venue.setId(null);
        validar(venue);
        return venueRepository.save(venue);
    }

    public Page<Venue> findAll(Pageable pageable){
        return venueRepository.findAll(pageable);
    }

    public Venue findById(Long id){
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado con id: " + id));

    }

    public List<Venue> findAll() {
        return venueRepository.findAll(Sort.by("nombre"));
    }

    // --- update(): añadir la línea de ciudad ---
    public Venue update(Long id, Venue datosActualizados) {
        Venue venueExistente = findById(id);
        validar(datosActualizados);

        venueExistente.setNombre(datosActualizados.getNombre());
        venueExistente.setDireccion(datosActualizados.getDireccion());
        venueExistente.setCiudad(datosActualizados.getCiudad());          // <-- nuevo
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
        if (venue.getCiudad() == null || venue.getCiudad().trim().isEmpty()) {   // <-- nuevo
            throw new InvalidDataException("La ciudad del lugar no puede ir vacía");
        }
        if (venue.getCapacidad() == null || venue.getCapacidad() <= 0) {
            throw new InvalidDataException("La capacidad debe ser un número entero mayor a 0");
        }
    }
}
