package ec.edu.espe.soportetickets.support;

import ec.edu.espe.soportetickets.entity.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.Locale;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {
    protected static final String API_KEY = "grupo8-soporte-2026";
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected CategoriaRepository categoriaRepository;
    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected TecnicoRepository tecnicoRepository;
    @Autowired protected TicketRepository ticketRepository;
    @Autowired protected ObservacionRepository observacionRepository;
    @Autowired protected EntityManager entityManager;
    protected Categoria categoria;
    protected Usuario usuario;
    protected Tecnico tecnico;
    protected Ticket ticket;

    @BeforeEach
    void crearDatos() {
        categoria = new Categoria();
        categoria.setNombre("Hardware"); categoria.setDescripcion("Equipos");
        categoria.setTiempoRespuestaHoras(24); categoriaRepository.save(categoria);
        usuario = new Usuario(); usuario.setNombres("Ana");
        usuario.setEmail("ana@espe.edu.ec"); usuario.setDepartamento("TI"); usuario.setActivo(true);
        usuarioRepository.save(usuario);
        tecnico = new Tecnico(); tecnico.setNombres("Luis");
        tecnico.setEmail("luis@espe.edu.ec"); tecnico.setActivo(true); tecnico.setEspecialidad(categoria);
        tecnicoRepository.save(tecnico);
        ticket = nuevoTicket(EstadoTicket.ABIERTO, Prioridad.MEDIA, null);
    }

    protected Ticket nuevoTicket(EstadoTicket estado, Prioridad prioridad, Tecnico asignado) {
        Ticket nuevo = new Ticket(); nuevo.setCodigo("TMP-" + UUID.randomUUID());
        nuevo.setTitulo("Falla de equipo"); nuevo.setDescripcion("El equipo no enciende");
        nuevo.setEstado(estado); nuevo.setPrioridad(prioridad);
        nuevo.setUsuario(usuario); nuevo.setCategoria(categoria); nuevo.setTecnico(asignado);
        ticketRepository.save(nuevo);
        nuevo.setCodigo(String.format(Locale.ROOT, "TCK-%06d", nuevo.getId()));
        return ticketRepository.saveAndFlush(nuevo);
    }
}

