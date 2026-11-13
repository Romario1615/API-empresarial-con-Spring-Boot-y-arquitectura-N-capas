package ec.edu.espe.soportetickets.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(LocalDateTime timestamp, int status, String error, String mensaje,
        String ruta, String requestId, @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> detalles) {}

