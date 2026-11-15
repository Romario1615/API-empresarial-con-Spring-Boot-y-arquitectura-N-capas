package ec.edu.espe.soportetickets.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import ec.edu.espe.soportetickets.entity.enums.*;

public record ObservacionResponse(Long id, String autor, String contenido, LocalDateTime fechaRegistro) {}

