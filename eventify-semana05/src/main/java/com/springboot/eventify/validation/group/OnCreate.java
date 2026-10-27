package com.springboot.eventify.validation.group;

import jakarta.validation.groups.Default;

/**
 * Grupo de validación aplicado en la creación de recursos (POST).
 * Extiende Default para ejecutar también las validaciones estándar.
 */
public interface OnCreate extends Default {
}

