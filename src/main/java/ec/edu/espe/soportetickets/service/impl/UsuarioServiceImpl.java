package ec.edu.espe.soportetickets.service.impl;

import ec.edu.espe.soportetickets.dto.request.UsuarioRequest;
import ec.edu.espe.soportetickets.dto.response.UsuarioResponse;
import ec.edu.espe.soportetickets.entity.Usuario;
import ec.edu.espe.soportetickets.exception.*;
import ec.edu.espe.soportetickets.mapper.UsuarioMapper;
import ec.edu.espe.soportetickets.repository.UsuarioRepository;
import ec.edu.espe.soportetickets.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) { this.usuarioRepository = usuarioRepository; }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioMapper::toResponse).toList();
    }

    public UsuarioResponse obtener(Long id) { return UsuarioMapper.toResponse(buscar(id)); }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email().trim()))
            throw new RecursoDuplicadoException("Ya existe un usuario con ese correo");
        Usuario usuario = new Usuario();
        aplicar(usuario, request);
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id))
            throw new RecursoDuplicadoException("Ya existe otro usuario con ese correo");
        aplicar(usuario, request);
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    private void aplicar(Usuario usuario, UsuarioRequest request) {
        usuario.setNombres(request.nombres().trim());
        usuario.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        usuario.setDepartamento(request.departamento().trim());
        usuario.setActivo(request.activo());
    }
}

