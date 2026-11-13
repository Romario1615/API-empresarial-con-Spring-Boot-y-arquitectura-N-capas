package ec.edu.espe.soportetickets.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import ec.edu.espe.soportetickets.entity.enums.*;

public record EstadisticaEstadoResponse(EstadoTicket estado, Long cantidad) {}

