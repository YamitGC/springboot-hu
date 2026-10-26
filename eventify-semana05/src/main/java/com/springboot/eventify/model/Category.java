package com.springboot.eventify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name= "categories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @io.swagger.v3.oas.annotations.media.Schema(accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre de la categoría no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    @Column(length = 300)
    private String descripcion;
}
