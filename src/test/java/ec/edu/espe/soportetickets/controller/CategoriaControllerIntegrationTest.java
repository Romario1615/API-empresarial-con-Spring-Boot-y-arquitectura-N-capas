package ec.edu.espe.soportetickets.controller;

import ec.edu.espe.soportetickets.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("API de categorías con H2")
class CategoriaControllerIntegrationTest extends IntegrationTestBase {
    @Test @DisplayName("PUT actualiza nombre y tiempo de respuesta")
    void actualizar() throws Exception {
        mvc.perform(put("/api/categorias/{id}", categoria.getId()).header("X-API-KEY", API_KEY)
                .contentType("application/json").content("""
                    {"nombre":"Equipos físicos","descripcion":"Soporte técnico","tiempoRespuestaHoras":12}
                    """)).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Equipos físicos"))
                .andExpect(jsonPath("$.tiempoRespuestaHoras").value(12));
        entityManager.flush(); entityManager.clear();
        assertThat(categoriaRepository.findById(categoria.getId()).orElseThrow().getNombre()).isEqualTo("Equipos físicos");
    }

    @Test @DisplayName("DELETE rechaza categorías que tienen tickets")
    void eliminarConTickets() throws Exception {
        mvc.perform(delete("/api/categorias/{id}", categoria.getId()).header("X-API-KEY", API_KEY))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.status").value(422));
    }

    @Test @DisplayName("Una categoría sin tickets se elimina y libera especialidades")
    void eliminarSinTickets() throws Exception {
        ticketRepository.delete(ticket); ticketRepository.flush();
        mvc.perform(delete("/api/categorias/{id}", categoria.getId()).header("X-API-KEY", API_KEY))
                .andExpect(status().isNoContent());
        entityManager.flush(); entityManager.clear();
        assertThat(categoriaRepository.findById(categoria.getId())).isEmpty();
        assertThat(tecnicoRepository.findById(tecnico.getId()).orElseThrow().getEspecialidad()).isNull();
    }

    @Test @DisplayName("Bean Validation devuelve los errores por campo")
    void validar() throws Exception {
        mvc.perform(post("/api/categorias").header("X-API-KEY", API_KEY).contentType("application/json").content("""
                {"nombre":"","tiempoRespuestaHoras":0}
                """)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.nombre").exists())
                .andExpect(jsonPath("$.detalles.tiempoRespuestaHoras").exists());
    }

    @Test @DisplayName("Un nombre duplicado ignora mayúsculas y devuelve 409")
    void duplicado() throws Exception {
        mvc.perform(post("/api/categorias").header("X-API-KEY", API_KEY).contentType("application/json").content("""
                {"nombre":"hardware","descripcion":"Otro","tiempoRespuestaHoras":8}
                """)).andExpect(status().isConflict());
    }
}

