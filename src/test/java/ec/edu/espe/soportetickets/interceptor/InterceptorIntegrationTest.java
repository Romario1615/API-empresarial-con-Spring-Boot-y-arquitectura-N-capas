package ec.edu.espe.soportetickets.interceptor;

import ec.edu.espe.soportetickets.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Interceptores y errores HTTP")
class InterceptorIntegrationTest extends IntegrationTestBase {
    @Test @DisplayName("La clave incorrecta devuelve 401 con ambas cabeceras")
    void claveIncorrecta() throws Exception {
        var respuesta = mvc.perform(delete("/api/tickets/{id}", ticket.getId()).header("X-API-KEY", "incorrecta"))
                .andExpect(status().isUnauthorized()).andExpect(header().exists("X-Request-Id"))
                .andExpect(header().exists("X-Response-Time"))
                .andExpect(jsonPath("$.status").value(401)).andReturn().getResponse();
        assertThat(objectMapper.readTree(respuesta.getContentAsString()).path("requestId").asString())
                .isEqualTo(respuesta.getHeader("X-Request-Id"));
        assertThat(org.slf4j.MDC.get("requestId")).isNull();
    }

    @Test @DisplayName("GET no exige clave y genera identificadores diferentes")
    void lecturaSinClave() throws Exception {
        var primera = mvc.perform(get("/api/tickets")).andExpect(status().isOk())
                .andExpect(header().exists("X-Response-Time")).andReturn().getResponse();
        var segunda = mvc.perform(get("/api/tickets")).andExpect(status().isOk()).andReturn().getResponse();
        assertThat(primera.getHeader("X-Request-Id")).hasSize(8).isNotEqualTo(segunda.getHeader("X-Request-Id"));
        assertThat(org.slf4j.MDC.get("requestId")).isNull();
    }

    @Test @DisplayName("OPTIONS no exige clave")
    void optionsSinClave() throws Exception {
        mvc.perform(options("/api/tickets")).andExpect(status().isOk());
    }

    @Test @DisplayName("JSON mal formado produce 400")
    void jsonInvalido() throws Exception {
        mvc.perform(post("/api/tickets").header("X-API-KEY", API_KEY).contentType("application/json")
                .content("""
                    {"titulo":
                    """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test @DisplayName("Ruta inexistente produce 404 uniforme")
    void rutaInexistente() throws Exception {
        mvc.perform(get("/api/no-existe")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ruta").value("/api/no-existe")).andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test @DisplayName("ID y enum inválidos producen 400")
    void parametrosInvalidos() throws Exception {
        mvc.perform(get("/api/tickets/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tickets").param("prioridad", "URGENTE")).andExpect(status().isBadRequest());
    }

    @Test @DisplayName("Método no soportado produce 405")
    void metodoNoPermitido() throws Exception {
        mvc.perform(put("/api/tickets/{id}", ticket.getId()).header("X-API-KEY", API_KEY))
                .andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.status").value(405));
    }

    @ParameterizedTest @ValueSource(strings = {"bloqueante", "asincrono"})
    @DisplayName("La demostración informa modo, tiempo y hilo HTTP")
    void demo(String modo) throws Exception {
        mvc.perform(post("/api/demo/notificacion").param("modo", modo).header("X-API-KEY", API_KEY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.modo").value(modo))
                .andExpect(jsonPath("$.tiempoRespuestaMs").isNumber()).andExpect(jsonPath("$.hilo").isNotEmpty());
    }

    @Test @DisplayName("La demostración usa asincrono por defecto y rechaza modos desconocidos")
    void modoDemo() throws Exception {
        mvc.perform(post("/api/demo/notificacion").header("X-API-KEY", API_KEY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.modo").value("asincrono"));
        mvc.perform(post("/api/demo/notificacion").param("modo", "otro").header("X-API-KEY", API_KEY))
                .andExpect(status().isBadRequest());
    }
}

