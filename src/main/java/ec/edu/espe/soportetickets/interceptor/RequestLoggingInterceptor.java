package ec.edu.espe.soportetickets.interceptor;

import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String INICIO = RequestLoggingInterceptor.class.getName() + ".inicio";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("requestId", requestId);
        request.setAttribute(INICIO, System.nanoTime());
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            Long inicio = (Long) request.getAttribute(INICIO);
            long ms = inicio == null ? 0 : TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
            log.info("[HTTP] {} {} {} {} ms ip={} ua={}", request.getMethod(), request.getRequestURI(),
                    response.getStatus(), ms, request.getRemoteAddr(), limpiar(request.getHeader("User-Agent")));
        } finally {
            MDC.remove("requestId");
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "-" : valor.replace('\r', ' ').replace('\n', ' ');
    }
}

