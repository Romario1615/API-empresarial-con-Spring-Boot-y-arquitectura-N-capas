package ec.edu.espe.soportetickets.repository;

import ec.edu.espe.soportetickets.entity.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.dto.response.EstadisticaEstadoResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    List<Tecnico> findByEspecialidadId(Long categoriaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tecnico t where t.id = :id")
    Optional<Tecnico> buscarPorIdConBloqueo(@Param("id") Long id);
}

