package ec.edu.espe.soportetickets.dto.request;

import jakarta.validation.constraints.*;
import ec.edu.espe.soportetickets.entity.enums.*;

public record AsignarTecnicoRequest(
        @NotNull(message = "El técnico es obligatorio") @Positive(message = "El técnico debe ser un ID positivo") Long tecnicoId) {}

