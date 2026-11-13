package ec.edu.espe.soportetickets.dto.request;

import jakarta.validation.constraints.*;
import ec.edu.espe.soportetickets.entity.enums.*;

public record CategoriaRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80, message = "El nombre admite hasta 80 caracteres") String nombre,
        @Size(max = 255, message = "La descripción admite hasta 255 caracteres") String descripcion,
        @NotNull(message = "El tiempo de respuesta es obligatorio") @Positive(message = "El tiempo de respuesta debe ser mayor que cero") Integer tiempoRespuestaHoras) {}

