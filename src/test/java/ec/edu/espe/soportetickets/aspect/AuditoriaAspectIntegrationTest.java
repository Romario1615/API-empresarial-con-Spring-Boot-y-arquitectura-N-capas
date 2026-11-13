package ec.edu.espe.soportetickets.aspect;

import ec.edu.espe.soportetickets.dto.request.*;
import ec.edu.espe.soportetickets.entity.RegistroAuditoria;
import ec.edu.espe.soportetickets.exception.RecursoNoEncontradoException;
import ec.edu.espe.soportetickets.repository.RegistroAuditoriaRepository;
import ec.edu.espe.soportetickets.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auditoría AOP")
class AuditoriaAspectIntegrationTest {
    @Autowired CategoriaService categoriaService;
    @Autowired TicketService ticketService;
    @Autowired AuditoriaService auditoriaService;
    @Autowired RegistroAuditoriaRepository repository;
    @Autowired MockMvc mvc;
    private long ultimoId;

    @BeforeEach
    void preparar() {
        ultimoId = repository.findAll().stream().mapToLong(RegistroAuditoria::getId).max().orElse(0);
    }

    @Test @DisplayName("Una llamada exitosa registra método, duración y OK")
    void auditarExito() throws Exception {
        categoriaService.listar();
        assertThat(repository.findAll().stream().filter(r -> r.getId() > ultimoId).toList())
                .anySatisfy(r -> {
                    assertThat(r.getMetodo()).isEqualTo("CategoriaServiceImpl.listar");
                    assertThat(r.getResultado()).isEqualTo("OK");
                    assertThat(r.getDuracionMs()).isGreaterThanOrEqualTo(0);
                    assertThat(r.getFecha()).isNotNull();
                    assertThat(r.getMensajeError()).isNull();
                });
        mvc.perform(get("/api/auditoria")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("La auditoría ERROR sobrevive al rollback de la transacción")
    void auditarErrorPersistente() {
        assertThatThrownBy(() -> ticketService.obtener(99999999L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        TestTransaction.flagForRollback();
        TestTransaction.end();
        assertThat(repository.findAll().stream().filter(r -> r.getId() > ultimoId).toList())
                .anySatisfy(r -> {
                    assertThat(r.getMetodo()).isEqualTo("TicketServiceImpl.obtener");
                    assertThat(r.getResultado()).isEqualTo("ERROR");
                    assertThat(r.getMensajeError()).contains("99999999");
                });
    }

    @Test @DisplayName("Consultar auditoría no genera auditoría recursiva")
    void excluirAuditoria() {
        long antes = repository.count();
        auditoriaService.listarUltimos();
        assertThat(repository.count()).isEqualTo(antes);
    }

    @Test @DisplayName("La consulta de auditoría devuelve solo los últimos cincuenta")
    void limiteCincuenta() {
        for (int i = 0; i < 55; i++) {
            RegistroAuditoria registro = new RegistroAuditoria();
            registro.setMetodo("Prueba.metodo"); registro.setResultado("OK");
            registro.setDuracionMs(i); repository.save(registro);
        }
        var registros = auditoriaService.listarUltimos();
        assertThat(registros).hasSize(50);
        assertThat(registros).isSortedAccordingTo(
                java.util.Comparator.comparing(ec.edu.espe.soportetickets.dto.response.AuditoriaResponse::fecha).reversed());
    }
}

