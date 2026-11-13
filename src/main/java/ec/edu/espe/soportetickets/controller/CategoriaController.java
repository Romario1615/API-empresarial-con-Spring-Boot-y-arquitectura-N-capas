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
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService service;
    public CategoriaController(CategoriaService service) { this.service = service; }

    @GetMapping
    public List<CategoriaResponse> listar() { return service.listar(); }

    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        var respuesta = service.crear(request);
        return ResponseEntity.created(URI.create("/api/categorias")).body(respuesta);
    }

    @PutMapping("/{id}")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}

