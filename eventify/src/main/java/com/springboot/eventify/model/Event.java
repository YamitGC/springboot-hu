package com.springboot.eventify.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Event {
    private Long id;
    private String nombre;
    private String fecha;
    private String descripcion;
}
