package com.springboot.eventify.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Venue {
    private Long id;
    private String nombre;
    private String fecha;
    private Integer capacidad;
}
