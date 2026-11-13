package ec.edu.espe.soportetickets.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContratoHttpIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    @DisplayName("Crear categoría devuelve 201 y permite consultarla")
    void crearCategoria() throws Exception {
        mvc.perform(post("/api/categorias").header("X-API-KEY", "grupo8-soporte-2026")
                .contentType("application/json").content("""
                    {"nombre":"Hardware","descripcion":"Equipos","tiempoRespuestaHoras":24}
                    """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nombre").value("Hardware"));
        mvc.perform(get("/api/categorias")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tiempoRespuestaHoras").value(24));
    }

    @Test
    @DisplayName("Escribir sin clave devuelve 401 con trazabilidad")
    void rechazarSinClave() throws Exception {
        mvc.perform(post("/api/categorias").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
                .andExpect(header().exists("X-Request-Id")).andExpect(header().exists("X-Response-Time"));
    }
}
