package ec.edu.espe.soportetickets.service.impl;

import ec.edu.espe.soportetickets.dto.response.DemoNotificacionResponse;
import ec.edu.espe.soportetickets.exception.ParametroInvalidoException;
import ec.edu.espe.soportetickets.notification.NotificacionService;
import ec.edu.espe.soportetickets.service.DemoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
public class DemoServiceImpl implements DemoService {
    private final NotificacionService notificacionService;
    public DemoServiceImpl(NotificacionService notificacionService) { this.notificacionService = notificacionService; }

    public DemoNotificacionResponse notificar(String modo) {
        long inicio = System.nanoTime();
        switch (modo) {
            case "bloqueante" -> notificacionService.notificarAsignacionBloqueante("DEMO", "demo@espe.edu.ec", "Técnico demo");
            case "asincrono" -> notificacionService.notificarAsignacionAsync("DEMO", "demo@espe.edu.ec", "Técnico demo");
            default -> throw new ParametroInvalidoException("El modo debe ser bloqueante o asincrono");
        }
        return new DemoNotificacionResponse(modo, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio),
                Thread.currentThread().getName());
    }
}

