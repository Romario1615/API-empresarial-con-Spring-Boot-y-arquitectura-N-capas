package ec.edu.espe.soportetickets.controller;
 
import ec.edu.espe.soportetickets.entity.enums.EstadoTicket;
import ec.edu.espe.soportetickets.entity.enums.Prioridad;
import ec.edu.espe.soportetickets.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
@DisplayName("Consultas de tickets pendientes y estadísticas")
class ConsultasTicketsIntegrationTest extends IntegrationTestBase {
 
    @Test
    @DisplayName("Las estadísticas agrupan los tickets por estado")
    void estadisticasPorEstado() throws Exception {
        nuevoTicket(EstadoTicket.CERRADO, Prioridad.BAJA, tecnico);
 
        mvc.perform(get("/api/tickets/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.estado == 'ABIERTO')].cantidad").exists())
                .andExpect(jsonPath("$[?(@.estado == 'CERRADO')].cantidad").exists());
    }
 
    @Test
    @DisplayName("Los pendientes de un técnico excluyen los tickets cerrados")
    void pendientesDelTecnico() throws Exception {
        nuevoTicket(EstadoTicket.EN_PROCESO, Prioridad.CRITICA, tecnico);
        nuevoTicket(EstadoTicket.CERRADO, Prioridad.ALTA, tecnico);
 
        mvc.perform(get("/api/tecnicos/{id}/tickets-pendientes", tecnico.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("EN_PROCESO"));
    }
}
