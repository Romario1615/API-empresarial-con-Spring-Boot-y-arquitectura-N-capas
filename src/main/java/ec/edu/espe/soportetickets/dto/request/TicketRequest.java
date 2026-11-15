package ec.edu.espe.soportetickets.dto.request;

import jakarta.validation.constraints.*;
import ec.edu.espe.soportetickets.entity.enums.*;

public record TicketRequest(
        @NotBlank(message = "El título es obligatorio") @Size(max = 120, message = "El título admite hasta 120 caracteres") String titulo,
        @NotBlank(message = "La descripción es obligatoria") @Size(max = 2000, message = "La descripción admite hasta 2000 caracteres") String descripcion,
        @NotNull(message = "La prioridad es obligatoria") Prioridad prioridad,
        @NotNull(message = "El usuario es obligatorio") @Positive(message = "El usuario debe ser un ID positivo") Long usuarioId,
        @NotNull(message = "La categoría es obligatoria") @Positive(message = "La categoría debe ser un ID positivo") Long categoriaId) {}

