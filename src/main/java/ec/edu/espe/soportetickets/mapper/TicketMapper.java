package ec.edu.espe.soportetickets.mapper;

import ec.edu.espe.soportetickets.entity.Ticket;
import ec.edu.espe.soportetickets.dto.response.*;

public final class TicketMapper {
    private TicketMapper() { }

    public static TicketResponse toResponse(Ticket e) {
        if (e == null) return null;
        return new TicketResponse(e.getId(), e.getCodigo(), e.getTitulo(), e.getDescripcion(), e.getPrioridad(),
                e.getEstado(), e.getFechaCreacion(), e.getFechaActualizacion(), e.getFechaCierre(),
                UsuarioMapper.toResponse(e.getUsuario()), CategoriaMapper.toResponse(e.getCategoria()),
                TecnicoMapper.toResponse(e.getTecnico()),
                e.getObservaciones().stream().map(ObservacionMapper::toResponse).toList());
    }
}

