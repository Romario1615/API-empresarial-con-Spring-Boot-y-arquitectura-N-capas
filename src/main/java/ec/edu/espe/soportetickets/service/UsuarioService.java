package ec.edu.espe.soportetickets.service;

import ec.edu.espe.soportetickets.dto.request.*;
import ec.edu.espe.soportetickets.dto.response.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import java.util.List;

public interface UsuarioService {
    List<UsuarioResponse> listar();
    UsuarioResponse obtener(Long id);
    UsuarioResponse crear(UsuarioRequest request);
    UsuarioResponse actualizar(Long id, UsuarioRequest request);
}

