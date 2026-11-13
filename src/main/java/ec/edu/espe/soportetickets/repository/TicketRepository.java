package ec.edu.espe.soportetickets.repository;

import ec.edu.espe.soportetickets.entity.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.dto.response.EstadisticaEstadoResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    boolean existsByCategoriaId(Long categoriaId);
    long countByTecnicoIdAndEstadoIn(Long tecnicoId, Collection<EstadoTicket> estados);
    List<Ticket> findByTecnicoIdAndEstadoInOrderByPrioridadDescFechaCreacionAsc(Long tecnicoId, Collection<EstadoTicket> estados);
    List<Ticket> findByTecnicoId(Long tecnicoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> buscarPorIdConBloqueo(@Param("id") Long id);

    @Query("""
            select t from Ticket t
            where (:estado is null or t.estado = :estado)
              and (:prioridad is null or t.prioridad = :prioridad)
            order by t.fechaCreacion desc, t.id desc
            """)
    List<Ticket> buscarFiltrados(@Param("estado") EstadoTicket estado, @Param("prioridad") Prioridad prioridad);

    @Query("""
            select t from Ticket t where t.estado in :estados
            order by case t.prioridad
                when ec.edu.espe.soportetickets.entity.enums.Prioridad.CRITICA then 0
                when ec.edu.espe.soportetickets.entity.enums.Prioridad.ALTA then 1
                when ec.edu.espe.soportetickets.entity.enums.Prioridad.MEDIA then 2
                else 3 end, t.fechaCreacion asc, t.id asc
            """)
    List<Ticket> buscarPendientesOrdenados(@Param("estados") Collection<EstadoTicket> estados);

    @Query("""
            select new ec.edu.espe.soportetickets.dto.response.EstadisticaEstadoResponse(t.estado, count(t))
            from Ticket t group by t.estado
            """)
    List<EstadisticaEstadoResponse> contarPorEstado();
}

