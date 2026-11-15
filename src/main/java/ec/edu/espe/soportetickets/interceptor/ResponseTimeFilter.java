package ec.edu.espe.soportetickets.interceptor;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ResponseTimeFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        long inicio = System.nanoTime();
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrapper);
        } finally {
            // La copia diferida permite añadir la cabecera antes de comprometer el cuerpo, incluso en 401.
            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
            wrapper.setHeader("X-Response-Time", ms + " ms");
            wrapper.copyBodyToResponse();
        }
    }
}

