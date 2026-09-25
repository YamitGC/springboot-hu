package com.springboot.eventify.service;

import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.repository.EventRepository;
import com.springboot.eventify.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event save(Event event){
        event.setId(null);
        validar(event);
        return eventRepository.save(event);
    }

    public Page<Event> findAll(Pageable pageable){
        return eventRepository.findAll(pageable);
    }

    public Event findById(Long id){
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
    }

    public Event update(Long id, Event datosActualizados) {
        Event eventoExistente = findById(id);
        validar(datosActualizados);

        eventoExistente.setNombre(datosActualizados.getNombre());
        eventoExistente.setFecha(datosActualizados.getFecha());
        eventoExistente.setDescripcion(datosActualizados.getDescripcion());

        return eventRepository.save(eventoExistente);
    }

    public void delete(Long id) {
        Event evento = findById(id);
        eventRepository.delete(evento);
    }

    private void validar(Event event){
        if(event.getNombre() == null || event.getNombre().trim().isEmpty()){
            throw new InvalidDataException("El nombre del evento no puede estar vacío");
        }
        if(event.getFecha() == null || event.getFecha().trim().isEmpty()){
            throw new InvalidDataException("La fecha del evento no puede ir vacío");
        }
        if(event.getDescripcion() == null || event.getDescripcion().trim().isEmpty()){
            throw new InvalidDataException("La descripcion del evento no puede estar vacía");
        }
    }
}
