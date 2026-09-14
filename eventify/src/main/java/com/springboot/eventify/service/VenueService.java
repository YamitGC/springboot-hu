package com.springboot.eventify.service;

import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.repository.VenueRepository;
import com.springboot.eventify.model.Venue;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue save(Venue venue){
        if (venue.getNombre() == null || venue.getNombre().trim().isEmpty()){
            throw new InvalidDataException("El nombre del lugar no puede ir vacío");
        }
        return venueRepository.save(venue);
    }

    public List<Venue> findAll(){
        return venueRepository.findAll();
    }
}
