package ec.edu.espe.soportetickets.aspect;

import ec.edu.espe.soportetickets.entity.RegistroAuditoria;
import ec.edu.espe.soportetickets.repository.RegistroAuditoriaRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditoriaAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaAspect.class);
    private final RegistroAuditoriaRepository repository;
    private final TransactionTemplate transaccionAuditoria;

    public AuditoriaAspect(RegistroAuditoriaRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transaccionAuditoria = new TransactionTemplate(transactionManager);
        // Conserva el registro incluso si el llamador tiene una transacción que se revierte.
        this.transaccionAuditoria.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Around("execution(* ec.edu.espe.soportetickets.service.impl..*(..)) && !within(ec.edu.espe.soportetickets.service.impl.AuditoriaServiceImpl)")
    public Object auditar(ProceedingJoinPoint joinPoint) throws Throwable {
        long inicio = System.nanoTime();
        String metodo = joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature().getName();
        try {
            Object resultado = joinPoint.proceed();
            registrar(metodo, inicio, null);
            return resultado;
        } catch (Throwable ex) {
            registrar(metodo, inicio, ex);
            throw ex;
        }
    }

    private void registrar(String metodo, long inicio, Throwable error) {
        long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
        String mensaje = error == null ? null :
                (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
        if (error == null) log.info("[AOP] {} -> OK {} ms", metodo, ms);
        else log.warn("[AOP] {} -> ERROR {} ms: {}", metodo, ms, mensaje);
        try {
            RegistroAuditoria registro = new RegistroAuditoria();
            registro.setMetodo(truncar(metodo, 150));
            registro.setDuracionMs(ms);
            registro.setResultado(error == null ? "OK" : "ERROR");
            registro.setMensajeError(truncar(mensaje, 500));
            transaccionAuditoria.executeWithoutResult(status -> repository.saveAndFlush(registro));
        } catch (Exception ex) {
            log.error("No se pudo guardar la auditoría de {}: {}", metodo, ex.getMessage());
        }
    }

    private String truncar(String texto, int maximo) {
        return texto == null ? null : texto.substring(0, Math.min(texto.length(), maximo));
    }
}

