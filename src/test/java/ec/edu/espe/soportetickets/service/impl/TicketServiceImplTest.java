package ec.edu.espe.soportetickets.service.impl;

import ec.edu.espe.soportetickets.dto.request.*;
import ec.edu.espe.soportetickets.entity.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.exception.*;
import ec.edu.espe.soportetickets.notification.NotificacionService;
import ec.edu.espe.soportetickets.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Reglas del servicio de tickets")
class TicketServiceImplTest {
    @Mock TicketRepository ticketRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock TecnicoRepository tecnicoRepository;
    @Mock ObservacionRepository observacionRepository;
    @Mock NotificacionService notificacionService;
    @InjectMocks TicketServiceImpl servicio;
    Ticket ticket;
    Tecnico tecnico;
    Usuario usuario;
    Categoria categoria;

    @BeforeEach
    void preparar() {
        usuario = new Usuario(); usuario.setId(1L); usuario.setNombres("Ana");
        usuario.setEmail("ana@espe.edu.ec"); usuario.setDepartamento("TI"); usuario.setActivo(true);
        categoria = new Categoria(); categoria.setId(1L); categoria.setNombre("Redes");
        categoria.setTiempoRespuestaHoras(8);
        tecnico = new Tecnico(); tecnico.setId(1L); tecnico.setNombres("Luis");
        tecnico.setEmail("luis@espe.edu.ec"); tecnico.setActivo(true);
        ticket = new Ticket(); ticket.setId(1L); ticket.setCodigo("TCK-000001");
        ticket.setTitulo("Sin red"); ticket.setDescripcion("No hay conexión");
        ticket.setPrioridad(Prioridad.ALTA); ticket.setEstado(EstadoTicket.ABIERTO);
        ticket.setUsuario(usuario); ticket.setCategoria(categoria);
    }

    void buscarTicket() {
        when(ticketRepository.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(ticket));
    }
    void guardarTicket() {
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test @DisplayName("ABIERTO pasa a EN_PROCESO con técnico activo")
    void transicionValida() {
        buscarTicket(); guardarTicket(); ticket.setTecnico(tecnico);
        when(tecnicoRepository.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(tecnico));
        assertThat(servicio.cambiarEstado(1L, new CambiarEstadoRequest(EstadoTicket.EN_PROCESO, null)).estado())
                .isEqualTo(EstadoTicket.EN_PROCESO);
    }

    @Test @DisplayName("ABIERTO no puede pasar directamente a CERRADO")
    void transicionInvalida() {
        buscarTicket();
        assertThatThrownBy(() -> servicio.cambiarEstado(1L, new CambiarEstadoRequest(EstadoTicket.CERRADO, null)))
                .isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test @DisplayName("EN_PROCESO requiere técnico asignado")
    void sinTecnico() {
        buscarTicket();
        assertThatThrownBy(() -> servicio.cambiarEstado(1L, new CambiarEstadoRequest(EstadoTicket.EN_PROCESO, null)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test @DisplayName("Un técnico con cinco tickets activos no admite otro")
    void tecnicoSobrecargado() {
        buscarTicket();
        when(tecnicoRepository.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(tecnico));
        when(ticketRepository.countByTecnicoIdAndEstadoIn(eq(1L), anyCollection())).thenReturn(5L);
        assertThatThrownBy(() -> servicio.asignarTecnico(1L, new AsignarTecnicoRequest(1L)))
                .isInstanceOf(TecnicoSobrecargadoException.class);
    }

    @Test @DisplayName("No se asignan técnicos inactivos")
    void tecnicoInactivo() {
        buscarTicket(); tecnico.setActivo(false);
        when(tecnicoRepository.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(tecnico));
        assertThatThrownBy(() -> servicio.asignarTecnico(1L, new AsignarTecnicoRequest(1L)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test @DisplayName("No se agregan observaciones a tickets cerrados")
    void observacionEnCerrado() {
        buscarTicket(); ticket.setEstado(EstadoTicket.CERRADO);
        assertThatThrownBy(() -> servicio.agregarObservacion(1L, new ObservacionRequest("Ana", "Comentario")))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test @DisplayName("RESUELTO pasa a CERRADO y registra fecha de cierre")
    void fechaCierre() {
        buscarTicket(); guardarTicket(); ticket.setEstado(EstadoTicket.RESUELTO);
        var respuesta = servicio.cambiarEstado(1L, new CambiarEstadoRequest(EstadoTicket.CERRADO, null));
        assertThat(respuesta.estado()).isEqualTo(EstadoTicket.CERRADO);
        assertThat(respuesta.fechaCierre()).isNotNull();
    }

    @Test @DisplayName("Un usuario inactivo no puede crear tickets")
    void usuarioInactivo() {
        usuario.setActivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        assertThatThrownBy(() -> servicio.crear(new TicketRequest("Sin red", "No hay red", Prioridad.ALTA, 1L, 1L)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test @DisplayName("El ticket nace ABIERTO y obtiene el código de su ID")
    void crearTicket() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> {
            Ticket nuevo = i.getArgument(0);
            assertThat(nuevo.getCodigo()).isNotBlank();
            nuevo.setId(1L); return nuevo;
        });
        var respuesta = servicio.crear(new TicketRequest("Sin red", "No hay red", Prioridad.ALTA, 1L, 1L));
        assertThat(respuesta.estado()).isEqualTo(EstadoTicket.ABIERTO);
        assertThat(respuesta.codigo()).isEqualTo("TCK-000001");
    }

    @Test @DisplayName("Reabrir también respeta el máximo de cinco tickets activos")
    void reaperturaSobrecargada() {
        buscarTicket(); ticket.setEstado(EstadoTicket.RESUELTO); ticket.setTecnico(tecnico);
        when(tecnicoRepository.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(tecnico));
        when(ticketRepository.countByTecnicoIdAndEstadoIn(eq(1L), anyCollection())).thenReturn(5L);
        assertThatThrownBy(() -> servicio.cambiarEstado(1L, new CambiarEstadoRequest(EstadoTicket.EN_PROCESO, null)))
                .isInstanceOf(TecnicoSobrecargadoException.class);
    }

    @Test @DisplayName("Reasignar al mismo técnico no duplica su carga")
    void mismaAsignacion() {
        buscarTicket(); guardarTicket(); ticket.setTecnico(tecnico);
        when(tecnicoRepository.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(tecnico));
        assertThat(servicio.asignarTecnico(1L, new AsignarTecnicoRequest(1L)).tecnico().id()).isEqualTo(1L);
    }
}

