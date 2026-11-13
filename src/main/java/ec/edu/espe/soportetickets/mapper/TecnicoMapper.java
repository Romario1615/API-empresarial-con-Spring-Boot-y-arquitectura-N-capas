package ec.edu.espe.soportetickets.mapper;

import ec.edu.espe.soportetickets.entity.Tecnico;
import ec.edu.espe.soportetickets.dto.response.*;

public final class TecnicoMapper {
    private TecnicoMapper() { }

    public static TecnicoResponse toResponse(Tecnico e) {
        if (e == null) return null;
        return new TecnicoResponse(e.getId(), e.getNombres(), e.getEmail(), CategoriaMapper.toResponse(e.getEspecialidad()), e.isActivo());
    }
}

