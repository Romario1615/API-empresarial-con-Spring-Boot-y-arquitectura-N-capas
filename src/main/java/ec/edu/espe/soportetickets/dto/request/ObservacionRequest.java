package ec.edu.espe.soportetickets.dto.request;

import jakarta.validation.constraints.*;
import ec.edu.espe.soportetickets.entity.enums.*;

public record ObservacionRequest(
        @NotBlank(message = "El autor es obligatorio") @Size(max = 100, message = "El autor admite hasta 100 caracteres") String autor,
        @NotBlank(message = "El contenido es obligatorio") @Size(max = 1000, message = "El contenido admite hasta 1000 caracteres") String contenido) {}

