package com.springboot.eventify.validation.group;

import jakarta.validation.groups.Default;

/**
 * Grupo de validación aplicado en la actualización de recursos (PUT).
 * Extiende Default para ejecutar también las validaciones estándar.
 */
public interface OnUpdate extends Default {
}

