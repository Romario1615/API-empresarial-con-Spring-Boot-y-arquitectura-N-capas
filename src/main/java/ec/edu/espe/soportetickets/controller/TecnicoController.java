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
@RequestMapping("/api/tecnicos")
public class TecnicoController {
    private final TecnicoService service;
    public TecnicoController(TecnicoService service) { this.service = service; }

    @GetMapping
    public List<TecnicoResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public TecnicoResponse obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    public ResponseEntity<TecnicoResponse> crear(@Valid @RequestBody TecnicoRequest request) {
        var respuesta = service.crear(request);
        return ResponseEntity.created(URI.create("/api/tecnicos" + "/" + respuesta.id())).body(respuesta);
    }

    @PutMapping("/{id}")
    public TecnicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody TecnicoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tickets-pendientes")
    public List<TicketResponse> ticketsPendientes(@PathVariable Long id) { return service.ticketsPendientes(id); }

}

