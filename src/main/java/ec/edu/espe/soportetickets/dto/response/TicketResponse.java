package ec.edu.espe.soportetickets.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import ec.edu.espe.soportetickets.entity.enums.*;

public record TicketResponse(Long id, String codigo, String titulo, String descripcion, Prioridad prioridad,
        EstadoTicket estado, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion,
        LocalDateTime fechaCierre, UsuarioResponse usuario, CategoriaResponse categoria,
        TecnicoResponse tecnico, List<ObservacionResponse> observaciones) {}

