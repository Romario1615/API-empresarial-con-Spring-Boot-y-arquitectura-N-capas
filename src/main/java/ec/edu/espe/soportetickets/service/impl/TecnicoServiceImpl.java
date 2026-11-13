package ec.edu.espe.soportetickets.service.impl;

import ec.edu.espe.soportetickets.dto.request.TecnicoRequest;
import ec.edu.espe.soportetickets.dto.response.*;
import ec.edu.espe.soportetickets.entity.*;
import ec.edu.espe.soportetickets.entity.enums.EstadoTicket;
import ec.edu.espe.soportetickets.exception.*;
import ec.edu.espe.soportetickets.mapper.*;
import ec.edu.espe.soportetickets.repository.*;
import ec.edu.espe.soportetickets.service.TecnicoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class TecnicoServiceImpl implements TecnicoService {
    private static final List<EstadoTicket> ACTIVOS = List.of(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO);
    private final TecnicoRepository tecnicoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TicketRepository ticketRepository;

    public TecnicoServiceImpl(TecnicoRepository tecnicoRepository, CategoriaRepository categoriaRepository,
                              TicketRepository ticketRepository) {
        this.tecnicoRepository = tecnicoRepository;
        this.categoriaRepository = categoriaRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<TecnicoResponse> listar() {
        return tecnicoRepository.findAll().stream().map(TecnicoMapper::toResponse).toList();
    }

    public TecnicoResponse obtener(Long id) { return TecnicoMapper.toResponse(buscar(id)); }

    @Transactional
    public TecnicoResponse crear(TecnicoRequest request) {
        if (tecnicoRepository.existsByEmailIgnoreCase(request.email().trim()))
            throw new RecursoDuplicadoException("Ya existe un técnico con ese correo");
        Tecnico tecnico = new Tecnico();
        aplicar(tecnico, request);
        return TecnicoMapper.toResponse(tecnicoRepository.save(tecnico));
    }

    @Transactional
    public TecnicoResponse actualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = bloquear(id);
        if (tecnicoRepository.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id))
            throw new RecursoDuplicadoException("Ya existe otro técnico con ese correo");
        aplicar(tecnico, request);
        return TecnicoMapper.toResponse(tecnicoRepository.save(tecnico));
    }

    @Transactional
    public void eliminar(Long id) {
        Tecnico tecnico = bloquear(id);
        if (ticketRepository.countByTecnicoIdAndEstadoIn(id, ACTIVOS) > 0)
            throw new ReglaNegocioException("No se puede eliminar un técnico con tickets activos");
        // Los tickets históricos se conservan y su asignación opcional se libera.
        ticketRepository.findByTecnicoId(id).forEach(ticket -> ticket.setTecnico(null));
        ticketRepository.flush();
        tecnicoRepository.delete(tecnico);
    }

    public List<TicketResponse> ticketsPendientes(Long id) {
        buscar(id);
        // El enum se guarda como texto: se corrige el orden alfabético con su ordinal.
        return ticketRepository.findByTecnicoIdAndEstadoInOrderByPrioridadDescFechaCreacionAsc(id, ACTIVOS)
                .stream().sorted(Comparator.comparingInt((Ticket t) -> t.getPrioridad().ordinal()).reversed()
                        .thenComparing(Ticket::getFechaCreacion).thenComparing(Ticket::getId))
                .map(TicketMapper::toResponse).toList();
    }

    private Tecnico buscar(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado: " + id));
    }

    private Tecnico bloquear(Long id) {
        return tecnicoRepository.buscarPorIdConBloqueo(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Técnico no encontrado: " + id));
    }

    private void aplicar(Tecnico tecnico, TecnicoRequest request) {
        Categoria especialidad = request.especialidadId() == null ? null :
                categoriaRepository.findById(request.especialidadId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));
        tecnico.setNombres(request.nombres().trim());
        tecnico.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        tecnico.setEspecialidad(especialidad);
        tecnico.setActivo(request.activo());
    }
}

