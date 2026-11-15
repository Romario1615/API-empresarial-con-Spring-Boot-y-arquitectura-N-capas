package ec.edu.espe.soportetickets.mapper;

import ec.edu.espe.soportetickets.entity.Categoria;
import ec.edu.espe.soportetickets.dto.response.*;

public final class CategoriaMapper {
    private CategoriaMapper() { }

    public static CategoriaResponse toResponse(Categoria e) {
        if (e == null) return null;
        return new CategoriaResponse(e.getId(), e.getNombre(), e.getDescripcion(), e.getTiempoRespuestaHoras());
    }
}

