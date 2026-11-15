package ec.edu.espe.soportetickets.service;

import ec.edu.espe.soportetickets.dto.request.*;
import ec.edu.espe.soportetickets.dto.response.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import java.util.List;

public interface TecnicoService {
    List<TecnicoResponse> listar();
    TecnicoResponse obtener(Long id);
    TecnicoResponse crear(TecnicoRequest request);
    TecnicoResponse actualizar(Long id, TecnicoRequest request);
    void eliminar(Long id);
    List<TicketResponse> ticketsPendientes(Long id);
}

