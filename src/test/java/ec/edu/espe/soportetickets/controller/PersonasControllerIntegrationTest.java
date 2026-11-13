package ec.edu.espe.soportetickets.controller;

import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("API de usuarios y técnicos")
class PersonasControllerIntegrationTest extends IntegrationTestBase {
    @Test @DisplayName("Usuarios permite registrar, listar, consultar y actualizar")
    void usuarios() throws Exception {
        mvc.perform(post("/api/usuarios").header("X-API-KEY", API_KEY).contentType("application/json").content("""
                {"nombres":"Lucía","email":"lucia@espe.edu.ec","departamento":"Biblioteca","activo":true}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.email").value("lucia@espe.edu.ec"));
        mvc.perform(get("/api/usuarios")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/usuarios/{id}", usuario.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Ana"));
        mvc.perform(put("/api/usuarios/{id}", usuario.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"nombres":"Ana Torres","email":"ana@espe.edu.ec","departamento":"Sistemas","activo":false}
                    """)).andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(false));
    }

    @Test @DisplayName("Técnicos permite registrar, listar, consultar, actualizar y eliminar")
    void tecnicos() throws Exception {
        mvc.perform(post("/api/tecnicos").header("X-API-KEY", API_KEY).contentType("application/json").content("""
                {"nombres":"María","email":"maria@espe.edu.ec","especialidadId":%d,"activo":true}
                """.formatted(categoria.getId()))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.especialidad.nombre").value("Hardware"));
        mvc.perform(get("/api/tecnicos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/tecnicos/{id}", tecnico.getId())).andExpect(status().isOk());
        mvc.perform(put("/api/tecnicos/{id}", tecnico.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"nombres":"Luis Pérez","email":"luis@espe.edu.ec","especialidadId":null,"activo":true}
                    """)).andExpect(status().isOk()).andExpect(jsonPath("$.nombres").value("Luis Pérez"));
        mvc.perform(delete("/api/tecnicos/{id}", tecnico.getId()).header("X-API-KEY", API_KEY))
                .andExpect(status().isNoContent());
    }

    @Test @DisplayName("Los correos duplicados de usuarios y técnicos devuelven 409")
    void duplicados() throws Exception {
        mvc.perform(post("/api/usuarios").header("X-API-KEY", API_KEY).contentType("application/json").content("""
                {"nombres":"Otra Ana","email":"ANA@ESPE.EDU.EC","departamento":"TI","activo":true}
                """)).andExpect(status().isConflict());
        mvc.perform(post("/api/tecnicos").header("X-API-KEY", API_KEY).contentType("application/json").content("""
                {"nombres":"Otro Luis","email":"LUIS@ESPE.EDU.EC","activo":true}
                """)).andExpect(status().isConflict());
    }

    @Test @DisplayName("No se elimina un técnico con tickets activos")
    void tecnicoConActivos() throws Exception {
        ticket.setTecnico(tecnico); ticketRepository.saveAndFlush(ticket);
        mvc.perform(delete("/api/tecnicos/{id}", tecnico.getId()).header("X-API-KEY", API_KEY))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test @DisplayName("Eliminar técnico sin activos conserva sus tickets históricos")
    void eliminarTecnicoConHistorico() throws Exception {
        ticket.setTecnico(tecnico); ticket.setEstado(EstadoTicket.RESUELTO);
        ticketRepository.saveAndFlush(ticket);
        mvc.perform(delete("/api/tecnicos/{id}", tecnico.getId()).header("X-API-KEY", API_KEY))
                .andExpect(status().isNoContent());
        entityManager.flush(); entityManager.clear();
        assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getTecnico()).isNull();
    }

    @Test @DisplayName("Pendientes del técnico excluye estados finales y ordena por urgencia")
    void pendientesTecnico() throws Exception {
        nuevoTicket(EstadoTicket.ABIERTO, Prioridad.BAJA, tecnico);
        nuevoTicket(EstadoTicket.EN_PROCESO, Prioridad.CRITICA, tecnico);
        nuevoTicket(EstadoTicket.CERRADO, Prioridad.ALTA, tecnico);
        mvc.perform(get("/api/tecnicos/{id}/tickets-pendientes", tecnico.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].prioridad").value("CRITICA"))
                .andExpect(jsonPath("$[1].prioridad").value("BAJA"));
    }
}

