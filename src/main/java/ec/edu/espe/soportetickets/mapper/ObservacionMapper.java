package ec.edu.espe.soportetickets.mapper;

import ec.edu.espe.soportetickets.entity.Observacion;
import ec.edu.espe.soportetickets.dto.response.*;

public final class ObservacionMapper {
    private ObservacionMapper() { }

    public static ObservacionResponse toResponse(Observacion e) {
        if (e == null) return null;
        return new ObservacionResponse(e.getId(), e.getAutor(), e.getContenido(), e.getFechaRegistro());
    }
}

