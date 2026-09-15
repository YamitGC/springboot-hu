package com.springboot.eventify.service;

import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.repository.VenueRepository;
import com.springboot.eventify.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue save(Venue venue){
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

    public Venue update(Long id, Venue datosActualizados) {
        Venue venueExistente = findById(id);
        validar(datosActualizados);

        venueExistente.setNombre(datosActualizados.getNombre());
        venueExistente.setDireccion(datosActualizados.getDireccion());
        venueExistente.setCapacidad(datosActualizados.getCapacidad());

        return venueRepository.save(venueExistente);
    }

    public void delete(Long id) {
        Venue venue = findById(id);
        venueRepository.delete(venue);
    }

    private void validar(Venue venue) {
        if (venue.getNombre() == null || venue.getNombre().trim().isEmpty()) {
            throw new InvalidDataException("El nombre del lugar no puede estar vacío");
        }
        if (venue.getDireccion() == null || venue.getDireccion().trim().isEmpty()){
            throw new InvalidDataException("La dirección del lugar no puede ir vacía");
        }
        if (venue.getDireccion() == null || venue.getDireccion().trim().isEmpty()){
            throw new InvalidDataException("La dirección del lugar no puede ir vacía");
        }
    }
}
