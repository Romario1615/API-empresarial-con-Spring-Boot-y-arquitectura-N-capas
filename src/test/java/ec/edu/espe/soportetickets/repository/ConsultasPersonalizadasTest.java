
package ec.edu.espe.soportetickets.repository;

import ec.edu.espe.soportetickets.entity.enums.EstadoTicket;
import ec.edu.espe.soportetickets.entity.enums.Prioridad;
import ec.edu.espe.soportetickets.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Consultas personalizadas de Spring Data")
class ConsultasPersonalizadasTest extends IntegrationTestBase {

    @Test
    @DisplayName("Cuenta solo los tickets activos de un técnico")
    void cuentaTicketsActivosDelTecnico() {
        nuevoTicket(EstadoTicket.ABIERTO, Prioridad.ALTA, tecnico);
        nuevoTicket(EstadoTicket.EN_PROCESO, Prioridad.MEDIA, tecnico);
        nuevoTicket(EstadoTicket.CERRADO, Prioridad.BAJA, tecnico);

        long activos = ticketRepository.countByTecnicoIdAndEstadoIn(
            tecnico.getId(),
            List.of(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO)
        );

        assertThat(activos).isEqualTo(2);
    }

    @Test
    @DisplayName("Detecta si una categoría tiene tickets asociados")
    void detectaCategoriaConTickets() {
        assertThat(ticketRepository.existsByCategoriaId(categoria.getId())).isTrue();
        assertThat(ticketRepository.existsByCategoriaId(999_999L)).isFalse();
    }
}
