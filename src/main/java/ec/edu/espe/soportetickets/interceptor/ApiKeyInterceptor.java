package ec.edu.espe.soportetickets.interceptor;

import ec.edu.espe.soportetickets.dto.response.ErrorResponse;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Set;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {
    private static final Set<String> ESCRITURAS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public ApiKeyInterceptor(@Value("${app.security.api-key}") String apiKey, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!ESCRITURAS.contains(request.getMethod())) return true;
        String recibida = request.getHeader("X-API-KEY");
        if (recibida != null && MessageDigest.isEqual(apiKey.getBytes(StandardCharsets.UTF_8),
                recibida.getBytes(StandardCharsets.UTF_8))) return true;
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), 401, "No autorizado",
                "Debe proporcionar una clave X-API-KEY válida", request.getRequestURI(),
                (String) request.getAttribute("requestId"), null);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
        return false;
    }
}

