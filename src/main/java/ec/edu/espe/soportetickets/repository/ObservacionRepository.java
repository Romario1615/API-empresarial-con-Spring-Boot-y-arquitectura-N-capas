package ec.edu.espe.soportetickets.repository;

import ec.edu.espe.soportetickets.entity.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.dto.response.EstadisticaEstadoResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ObservacionRepository extends JpaRepository<Observacion, Long> {
    List<Observacion> findByTicketIdOrderByFechaRegistroAscIdAsc(Long ticketId);
}

