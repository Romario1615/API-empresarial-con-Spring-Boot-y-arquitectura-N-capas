package ec.edu.espe.soportetickets.dto.request;

import jakarta.validation.constraints.*;
import ec.edu.espe.soportetickets.entity.enums.*;

public record UsuarioRequest(
        @NotBlank(message = "Los nombres son obligatorios") @Size(max = 100, message = "Los nombres admiten hasta 100 caracteres") String nombres,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 254, message = "El correo admite hasta 254 caracteres") String email,
        @NotBlank(message = "El departamento es obligatorio") @Size(max = 80, message = "El departamento admite hasta 80 caracteres") String departamento,
        @NotNull(message = "El estado activo es obligatorio") Boolean activo) {}

