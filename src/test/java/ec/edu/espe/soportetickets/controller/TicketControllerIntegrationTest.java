package ec.edu.espe.soportetickets.controller;

import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("API de tickets con H2")
class TicketControllerIntegrationTest extends IntegrationTestBase {
    @Test @DisplayName("POST registra un ticket ABIERTO con código definitivo")
    void registrar() throws Exception {
        String respuesta = mvc.perform(post("/api/tickets").header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"titulo":"Sin internet","descripcion":"No hay conexión",
                     "prioridad":"CRITICA","usuarioId":%d,"categoriaId":%d}
                    """.formatted(usuario.getId(), categoria.getId())))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andExpect(jsonPath("$.usuario.id").value(usuario.getId()))
                .andReturn().getResponse().getContentAsString();
        var json = objectMapper.readTree(respuesta);
        assertThat(json.path("codigo").asString()).isEqualTo("TCK-%06d".formatted(json.path("id").asLong()));
    }

    @Test @DisplayName("GET obtiene un ticket y sus observaciones")
    void obtenerConObservaciones() throws Exception {
        mvc.perform(post("/api/tickets/{id}/observaciones", ticket.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"autor":"Ana","contenido":"El problema persiste"}
                    """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.fechaRegistro").exists());
        mvc.perform(get("/api/tickets/{id}", ticket.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.observaciones[0].contenido").value("El problema persiste"));
        mvc.perform(get("/api/tickets/{id}/observaciones", ticket.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].autor").value("Ana"));
    }

    @Test @DisplayName("PATCH asigna técnico y permite pasar a EN_PROCESO")
    void asignarYCambiarEstado() throws Exception {
        mvc.perform(patch("/api/tickets/{id}/asignar", ticket.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"tecnicoId":%d}
                    """.formatted(tecnico.getId())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tecnico.id").value(tecnico.getId()));
        mvc.perform(patch("/api/tickets/{id}/estado", ticket.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"estado":"EN_PROCESO","comentario":"Inicia diagnóstico"}
                    """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("EN_PROCESO"))
                .andExpect(jsonPath("$.observaciones[0].contenido").value("Inicia diagnóstico"));
    }

    @Test @DisplayName("DELETE elimina un ticket abierto y sus observaciones")
    void eliminar() throws Exception {
        mvc.perform(post("/api/tickets/{id}/observaciones", ticket.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"autor":"Ana","contenido":"Prueba"}
                    """)).andExpect(status().isCreated());
        mvc.perform(delete("/api/tickets/{id}", ticket.getId()).header("X-API-KEY", API_KEY))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        entityManager.flush(); entityManager.clear();
        assertThat(ticketRepository.findById(ticket.getId())).isEmpty();
        assertThat(observacionRepository.findByTicketIdOrderByFechaRegistroAscIdAsc(ticket.getId())).isEmpty();
    }

    @Test @DisplayName("Un ticket inexistente devuelve ErrorResponse 404")
    void noEncontrado() throws Exception {
        mvc.perform(get("/api/tickets/99999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").exists()).andExpect(jsonPath("$.mensaje").isNotEmpty())
                .andExpect(jsonPath("$.ruta").value("/api/tickets/99999999"))
                .andExpect(jsonPath("$.requestId").isNotEmpty()).andExpect(jsonPath("$.detalles").doesNotExist());
    }

    @Test @DisplayName("Una transición inválida devuelve 409")
    void transicionInvalida() throws Exception {
        mvc.perform(patch("/api/tickets/{id}/estado", ticket.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"estado":"CERRADO"}
                    """)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
    }

    @Test @DisplayName("Un técnico con cinco tickets activos no recibe el sexto")
    void capacidad() throws Exception {
        for (int i = 0; i < 5; i++) nuevoTicket(EstadoTicket.ABIERTO, Prioridad.BAJA, tecnico);
        mvc.perform(patch("/api/tickets/{id}/asignar", ticket.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"tecnicoId":%d}
                    """.formatted(tecnico.getId())))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test @DisplayName("Un ticket cerrado rechaza observaciones con 422")
    void cerrado() throws Exception {
        ticket.setEstado(EstadoTicket.CERRADO); ticketRepository.saveAndFlush(ticket);
        mvc.perform(post("/api/tickets/{id}/observaciones", ticket.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"autor":"Ana","contenido":"Ya no se puede registrar"}
                    """)).andExpect(status().isUnprocessableEntity());
    }

    @Test @DisplayName("Los filtros combinan estado y prioridad")
    void filtrar() throws Exception {
        nuevoTicket(EstadoTicket.RESUELTO, Prioridad.ALTA, tecnico);
        mvc.perform(get("/api/tickets").param("estado", "ABIERTO").param("prioridad", "MEDIA"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ticket.getId()));
    }

    @Test @DisplayName("Pendientes ordena CRITICA, ALTA, MEDIA, BAJA y excluye resueltos")
    void pendientes() throws Exception {
        nuevoTicket(EstadoTicket.ABIERTO, Prioridad.BAJA, tecnico);
        nuevoTicket(EstadoTicket.EN_PROCESO, Prioridad.ALTA, tecnico);
        nuevoTicket(EstadoTicket.ABIERTO, Prioridad.CRITICA, tecnico);
        nuevoTicket(EstadoTicket.RESUELTO, Prioridad.CRITICA, tecnico);
        mvc.perform(get("/api/tickets/pendientes")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].prioridad").value("CRITICA"))
                .andExpect(jsonPath("$[1].prioridad").value("ALTA"))
                .andExpect(jsonPath("$[2].prioridad").value("MEDIA"))
                .andExpect(jsonPath("$[3].prioridad").value("BAJA"));
    }

    @Test @DisplayName("Estadísticas agrupa los tickets por estado")
    void estadisticas() throws Exception {
        nuevoTicket(EstadoTicket.ABIERTO, Prioridad.ALTA, null);
        nuevoTicket(EstadoTicket.RESUELTO, Prioridad.BAJA, tecnico);
        mvc.perform(get("/api/tickets/estadisticas")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.estado == 'ABIERTO')].cantidad").value(org.hamcrest.Matchers.contains(2)))
                .andExpect(jsonPath("$[?(@.estado == 'RESUELTO')].cantidad").value(org.hamcrest.Matchers.contains(1)));
    }

    @Test @DisplayName("Un usuario inactivo no puede crear tickets por HTTP")
    void usuarioInactivo() throws Exception {
        usuario.setActivo(false); usuarioRepository.saveAndFlush(usuario);
        mvc.perform(post("/api/tickets").header("X-API-KEY", API_KEY).contentType("application/json").content("""
                {"titulo":"Falla","descripcion":"Detalle","prioridad":"BAJA","usuarioId":%d,"categoriaId":%d}
                """.formatted(usuario.getId(), categoria.getId())))
                .andExpect(status().isUnprocessableEntity());
    }
}

