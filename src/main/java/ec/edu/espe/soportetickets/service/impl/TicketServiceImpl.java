package ec.edu.espe.soportetickets.service.impl;

import ec.edu.espe.soportetickets.dto.request.*;
import ec.edu.espe.soportetickets.dto.response.*;
import ec.edu.espe.soportetickets.entity.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.exception.*;
import ec.edu.espe.soportetickets.mapper.*;
import ec.edu.espe.soportetickets.notification.NotificacionService;
import ec.edu.espe.soportetickets.repository.*;
import ec.edu.espe.soportetickets.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {
    private static final List<EstadoTicket> ACTIVOS = List.of(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO);
    private static final Map<EstadoTicket, Set<EstadoTicket>> TRANSICIONES = Map.of(
            EstadoTicket.ABIERTO, Set.of(EstadoTicket.EN_PROCESO, EstadoTicket.CANCELADO),
            EstadoTicket.EN_PROCESO, Set.of(EstadoTicket.RESUELTO, EstadoTicket.CANCELADO),
            EstadoTicket.RESUELTO, Set.of(EstadoTicket.CERRADO, EstadoTicket.EN_PROCESO));
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ObservacionRepository observacionRepository;
    private final NotificacionService notificacionService;

    public TicketServiceImpl(TicketRepository ticketRepository, UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository, TecnicoRepository tecnicoRepository,
            ObservacionRepository observacionRepository, NotificacionService notificacionService) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.observacionRepository = observacionRepository;
        this.notificacionService = notificacionService;
    }

    public List<TicketResponse> listar(EstadoTicket estado, Prioridad prioridad) {
        return ticketRepository.buscarFiltrados(estado, prioridad).stream().map(TicketMapper::toResponse).toList();
    }

    public TicketResponse obtener(Long id) { return TicketMapper.toResponse(buscar(id)); }

    public List<TicketResponse> pendientes() {
        return ticketRepository.buscarPendientesOrdenados(ACTIVOS).stream().map(TicketMapper::toResponse).toList();
    }

    public List<EstadisticaEstadoResponse> estadisticas() { return ticketRepository.contarPorEstado(); }

    @Transactional
    public TicketResponse crear(TicketRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + request.usuarioId()));
        if (!usuario.isActivo()) throw new ReglaNegocioException("El usuario solicitante debe estar activo");
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada: " + request.categoriaId()));
        Ticket ticket = new Ticket();
        ticket.setTitulo(request.titulo().trim());
        ticket.setDescripcion(request.descripcion().trim());
        ticket.setPrioridad(request.prioridad());
        ticket.setEstado(EstadoTicket.ABIERTO);
        ticket.setUsuario(usuario);
        ticket.setCategoria(categoria);
        // IDENTITY necesita insertar antes de conocer el ID; el temporal satisface NOT NULL y UNIQUE.
        ticket.setCodigo("TMP-" + UUID.randomUUID());
        ticket = ticketRepository.save(ticket);
        ticket.setCodigo(String.format(Locale.ROOT, "TCK-%06d", ticket.getId()));
        return TicketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse asignarTecnico(Long id, AsignarTecnicoRequest request) {
        Ticket ticket = bloquear(id);
        Tecnico tecnico = tecnicoActivo(request.tecnicoId());
        boolean mismaAsignacion = ticket.getTecnico() != null
                && ticket.getTecnico().getId().equals(tecnico.getId());
        if (ACTIVOS.contains(ticket.getEstado()) && !mismaAsignacion) validarCapacidad(tecnico);
        ticket.setTecnico(tecnico);
        ticket.setFechaActualizacion(LocalDateTime.now());
        Ticket guardado = ticketRepository.save(ticket);
        notificacionService.notificarAsignacionAsync(guardado.getCodigo(), tecnico.getEmail(), tecnico.getNombres());
        return TicketMapper.toResponse(guardado);
    }

    @Transactional
    public TicketResponse cambiarEstado(Long id, CambiarEstadoRequest request) {
        Ticket ticket = bloquear(id);
        EstadoTicket anterior = ticket.getEstado();
        if (!TRANSICIONES.getOrDefault(anterior, Set.of()).contains(request.estado()))
            throw new TransicionEstadoInvalidaException("No se permite cambiar de " + anterior + " a " + request.estado());
        if (request.estado() == EstadoTicket.EN_PROCESO) {
            if (ticket.getTecnico() == null)
                throw new ReglaNegocioException("Debe asignar un técnico antes de pasar a EN_PROCESO");
            Tecnico tecnico = tecnicoActivo(ticket.getTecnico().getId());
            if (!ACTIVOS.contains(anterior)) validarCapacidad(tecnico);
        }
        // El comentario pertenece a la transición y se registra antes del estado terminal.
        if (request.comentario() != null && !request.comentario().isBlank()) {
            Observacion observacion = new Observacion();
            observacion.setAutor("Sistema");
            observacion.setContenido(request.comentario().trim());
            observacion.setFechaRegistro(LocalDateTime.now());
            ticket.agregarObservacion(observacion);
        }
        ticket.setEstado(request.estado());
        ticket.setFechaActualizacion(LocalDateTime.now());
        if (request.estado() == EstadoTicket.CERRADO) ticket.setFechaCierre(LocalDateTime.now());
        return TicketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public void eliminar(Long id) {
        Ticket ticket = bloquear(id);
        if (ticket.getEstado() != EstadoTicket.ABIERTO && ticket.getEstado() != EstadoTicket.CANCELADO)
            throw new ReglaNegocioException("Solo se eliminan tickets ABIERTO o CANCELADO");
        ticketRepository.delete(ticket);
    }

    @Transactional
    public ObservacionResponse agregarObservacion(Long id, ObservacionRequest request) {
        Ticket ticket = bloquear(id);
        if (ticket.getEstado() == EstadoTicket.CERRADO || ticket.getEstado() == EstadoTicket.CANCELADO)
            throw new ReglaNegocioException("No se permiten observaciones en tickets CERRADO o CANCELADO");
        Observacion observacion = new Observacion();
        observacion.setAutor(request.autor().trim());
        observacion.setContenido(request.contenido().trim());
        ticket.agregarObservacion(observacion);
        ticket.setFechaActualizacion(LocalDateTime.now());
        return ObservacionMapper.toResponse(observacionRepository.save(observacion));
    }

    public List<ObservacionResponse> listarObservaciones(Long id) {
        buscar(id);
        return observacionRepository.findByTicketIdOrderByFechaRegistroAscIdAsc(id)
                .stream().map(ObservacionMapper::toResponse).toList();
    }

    private Ticket buscar(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ticket no encontrado: " + id));
    }

    private Ticket bloquear(Long id) {
        return ticketRepository.buscarPorIdConBloqueo(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ticket no encontrado: " + id));
    }

    private Tecnico tecnicoActivo(Long id) {
        // Serializa la comprobación de carga y la asignación para un mismo técnico.
        Tecnico tecnico = tecnicoRepository.buscarPorIdConBloqueo(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado: " + id));
        if (!tecnico.isActivo()) throw new ReglaNegocioException("El técnico debe estar activo");
        return tecnico;
    }

    private void validarCapacidad(Tecnico tecnico) {
        if (ticketRepository.countByTecnicoIdAndEstadoIn(tecnico.getId(), ACTIVOS) >= 5)
            throw new TecnicoSobrecargadoException("El técnico ya tiene cinco tickets activos");
    }
}

