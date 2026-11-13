package ec.edu.espe.soportetickets.service;

import ec.edu.espe.soportetickets.dto.request.*;
import ec.edu.espe.soportetickets.dto.response.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import java.util.List;

public interface TicketService {
    List<TicketResponse> listar(EstadoTicket estado, Prioridad prioridad);
    TicketResponse obtener(Long id);
    List<TicketResponse> pendientes();
    List<EstadisticaEstadoResponse> estadisticas();
    TicketResponse crear(TicketRequest request);
    TicketResponse asignarTecnico(Long id, AsignarTecnicoRequest request);
    TicketResponse cambiarEstado(Long id, CambiarEstadoRequest request);
    void eliminar(Long id);
    ObservacionResponse agregarObservacion(Long id, ObservacionRequest request);
    List<ObservacionResponse> listarObservaciones(Long id);
}

