package ec.edu.espe.soportetickets.interceptor;

import ec.edu.espe.soportetickets.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ApiKeyInterceptor en operaciones de escritura")
class ApiKeyEscrituraTest extends IntegrationTestBase {

    @Test
    @DisplayName("PUT sin X-API-KEY se rechaza con 401")
    void putSinClave() throws Exception {
        mvc.perform(put("/api/categorias/{id}", categoria.getId())
                        .contentType("application/json")
                        .content("{\"nombre\":\"Hardware\",\"descripcion\":\"Equipos\",\"tiempoRespuestaHoras\":12}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    @DisplayName("PATCH sin X-API-KEY se rechaza con 401")
    void patchSinClave() throws Exception {
        mvc.perform(patch("/api/tickets/{id}/estado", ticket.getId())
                        .contentType("application/json")
                        .content("{\"estado\":\"CANCELADO\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Con la clave correcta la petición llega al controlador")
    void deleteConClave() throws Exception {
        mvc.perform(delete("/api/tickets/{id}", 999_999L).header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
