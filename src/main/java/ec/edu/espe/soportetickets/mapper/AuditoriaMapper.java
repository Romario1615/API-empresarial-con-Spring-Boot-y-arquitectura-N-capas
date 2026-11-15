package ec.edu.espe.soportetickets.mapper;

import ec.edu.espe.soportetickets.entity.RegistroAuditoria;
import ec.edu.espe.soportetickets.dto.response.*;

public final class AuditoriaMapper {
    private AuditoriaMapper() { }

    public static AuditoriaResponse toResponse(RegistroAuditoria e) {
        if (e == null) return null;
        return new AuditoriaResponse(e.getId(), e.getMetodo(), e.getDuracionMs(), e.getResultado(), e.getMensajeError(), e.getFecha());
    }
}

