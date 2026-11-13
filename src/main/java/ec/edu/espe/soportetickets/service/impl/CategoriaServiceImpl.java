package ec.edu.espe.soportetickets.service.impl;

import ec.edu.espe.soportetickets.dto.request.CategoriaRequest;
import ec.edu.espe.soportetickets.dto.response.CategoriaResponse;
import ec.edu.espe.soportetickets.entity.Categoria;
import ec.edu.espe.soportetickets.exception.*;
import ec.edu.espe.soportetickets.mapper.CategoriaMapper;
import ec.edu.espe.soportetickets.repository.*;
import ec.edu.espe.soportetickets.service.CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final TicketRepository ticketRepository;
    private final TecnicoRepository tecnicoRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, TicketRepository ticketRepository,
                                TecnicoRepository tecnicoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.ticketRepository = ticketRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(CategoriaMapper::toResponse).toList();
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre))
            throw new RecursoDuplicadoException("Ya existe una categoría con ese nombre");
        Categoria categoria = new Categoria();
        aplicar(categoria, request);
        return CategoriaMapper.toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscar(id);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre().trim(), id))
            throw new RecursoDuplicadoException("Ya existe otra categoría con ese nombre");
        aplicar(categoria, request);
        return CategoriaMapper.toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        if (ticketRepository.existsByCategoriaId(id))
            throw new ReglaNegocioException("No se puede eliminar una categoría que tiene tickets");
        // La especialidad es opcional: al eliminarla se conservan los técnicos.
        tecnicoRepository.findByEspecialidadId(id).forEach(tecnico -> tecnico.setEspecialidad(null));
        tecnicoRepository.flush();
        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada: " + id));
    }

    private void aplicar(Categoria categoria, CategoriaRequest request) {
        categoria.setNombre(request.nombre().trim());
        categoria.setDescripcion(request.descripcion());
        categoria.setTiempoRespuestaHoras(request.tiempoRespuestaHoras());
    }
}

