package ec.edu.espe.soportetickets.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class NotificacionService {
    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private final long retardo;

    public NotificacionService(@Value("${app.notificacion.retardo-ms:2000}") long retardo) {
        this.retardo = retardo;
    }

    @Async("notificacionExecutor")
    public CompletableFuture<Void> notificarAsignacionAsync(String codigoTicket, String emailTecnico, String nombreTecnico) {
        try {
            simularEnvio(codigoTicket, emailTecnico, nombreTecnico, "ASYNC");
            return CompletableFuture.completedFuture(null);
        } catch (RuntimeException ex) {
            log.error("[ASYNC] Falló la notificación de {}: {}", codigoTicket, ex.getMessage());
            return CompletableFuture.failedFuture(ex);
        }
    }

    public void notificarAsignacionBloqueante(String codigoTicket, String emailTecnico, String nombreTecnico) {
        simularEnvio(codigoTicket, emailTecnico, nombreTecnico, "BLOQUEANTE");
    }

    private void simularEnvio(String codigoTicket, String emailTecnico, String nombreTecnico, String modo) {
        try {
            Thread.sleep(retardo);
            log.info("[{}] Ticket {} asignado a {} <{}> hilo={}", modo, codigoTicket, nombreTecnico,
                    emailTecnico, Thread.currentThread().getName());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La notificación fue interrumpida", ex);
        }
    }
}

