package ec.edu.espe.soportetickets.dto.request;

import jakarta.validation.constraints.*;
import ec.edu.espe.soportetickets.entity.enums.*;

public record CambiarEstadoRequest(
        @NotNull(message = "El estado es obligatorio") EstadoTicket estado,
        @Size(max = 1000, message = "El comentario admite hasta 1000 caracteres") String comentario) {}

