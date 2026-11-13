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
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;
    public TicketController(TicketService service) { this.service = service; }

    @GetMapping
    public List<TicketResponse> listar(@RequestParam(required = false) EstadoTicket estado,
            @RequestParam(required = false) Prioridad prioridad) {
        return service.listar(estado, prioridad);
    }

    @GetMapping("/{id}")
    public TicketResponse obtener(@PathVariable Long id) { return service.obtener(id); }

    @GetMapping("/pendientes")
    public List<TicketResponse> pendientes() { return service.pendientes(); }

    @GetMapping("/estadisticas")
    public List<EstadisticaEstadoResponse> estadisticas() { return service.estadisticas(); }

    @PostMapping
    public ResponseEntity<TicketResponse> crear(@Valid @RequestBody TicketRequest request) {
        var respuesta = service.crear(request);
        return ResponseEntity.created(URI.create("/api/tickets/" + respuesta.id())).body(respuesta);
    }

    @PatchMapping("/{id}/asignar")
    public TicketResponse asignarTecnico(@PathVariable Long id, @Valid @RequestBody AsignarTecnicoRequest request) {
        return service.asignarTecnico(id, request);
    }

    @PatchMapping("/{id}/estado")
    public TicketResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        return service.cambiarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/observaciones")
    public ResponseEntity<ObservacionResponse> agregarObservacion(@PathVariable Long id,
            @Valid @RequestBody ObservacionRequest request) {
        var respuesta = service.agregarObservacion(id, request);
        return ResponseEntity.created(URI.create("/api/tickets/" + id + "/observaciones")).body(respuesta);
    }

    @GetMapping("/{id}/observaciones")
    public List<ObservacionResponse> listarObservaciones(@PathVariable Long id) {
        return service.listarObservaciones(id);
    }
}

