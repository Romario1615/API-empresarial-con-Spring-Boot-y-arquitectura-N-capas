package ec.edu.espe.soportetickets.dto.request;

import jakarta.validation.constraints.*;
import ec.edu.espe.soportetickets.entity.enums.*;

public record TecnicoRequest(
        @NotBlank(message = "Los nombres son obligatorios") @Size(max = 100, message = "Los nombres admiten hasta 100 caracteres") String nombres,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 254, message = "El correo admite hasta 254 caracteres") String email,
        @Positive(message = "La especialidad debe ser un ID positivo") Long especialidadId,
        @NotNull(message = "El estado activo es obligatorio") Boolean activo) {}

