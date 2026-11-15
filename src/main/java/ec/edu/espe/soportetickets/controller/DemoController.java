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
@RequestMapping("/api/demo")
public class DemoController {
    private final DemoService service;
    public DemoController(DemoService service) { this.service = service; }

    @PostMapping("/notificacion")
    public DemoNotificacionResponse notificar(@RequestParam(defaultValue = "asincrono") String modo) {
        return service.notificar(modo);
    }
}

