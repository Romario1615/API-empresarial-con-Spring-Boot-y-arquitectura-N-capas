package ec.edu.espe.soportetickets.controller;

import ec.edu.espe.soportetickets.dto.request.*;
import ec.edu.espe.soportetickets.dto.response.*;
import ec.edu.espe.soportetickets.service.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }

    @GetMapping
    public List<UsuarioResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        var respuesta = service.crear(request);
        return ResponseEntity.created(URI.create("/api/usuarios" + "/" + respuesta.id())).body(respuesta);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return service.actualizar(id, request);
    }

}

