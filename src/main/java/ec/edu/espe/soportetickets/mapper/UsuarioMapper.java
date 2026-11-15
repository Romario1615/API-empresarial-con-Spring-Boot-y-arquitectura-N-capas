package ec.edu.espe.soportetickets.mapper;

import ec.edu.espe.soportetickets.entity.Usuario;
import ec.edu.espe.soportetickets.dto.response.*;

public final class UsuarioMapper {
    private UsuarioMapper() { }

    public static UsuarioResponse toResponse(Usuario e) {
        if (e == null) return null;
        return new UsuarioResponse(e.getId(), e.getNombres(), e.getEmail(), e.getDepartamento(), e.isActivo());
    }
}

