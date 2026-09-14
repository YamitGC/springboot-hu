package com.springboot.eventify.repository;

import com.springboot.eventify.model.Venue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class VenueRepository {

    private final List<Venue> venues = new ArrayList<>();
    private Long idCounter = 1L;

    public Venue save(Venue venue){
        venue.setId(idCounter++);
        venues.add(venue);
        return venue;
    }

    public List<Venue> findAll(){
        return venues;
    }
}
