package ec.edu.espe.soportetickets.exception;

import ec.edu.espe.soportetickets.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return error(404, "No encontrado", ex.getMessage(), request, null);
    }

    @ExceptionHandler({RecursoDuplicadoException.class, TransicionEstadoInvalidaException.class,
            TecnicoSobrecargadoException.class})
    public ResponseEntity<ErrorResponse> conflicto(RuntimeException ex, HttpServletRequest request) {
        return error(409, "Conflicto", ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> regla(ReglaNegocioException ex, HttpServletRequest request) {
        return error(422, "Regla de negocio", ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> noAutorizado(AccesoNoAutorizadoException ex, HttpServletRequest request) {
        return error(401, "No autorizado", ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                detalles.putIfAbsent(e.getField(), Objects.requireNonNullElse(e.getDefaultMessage(), "Valor no válido")));
        return error(400, "Solicitud inválida", "Revise los campos indicados", request, detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return error(400, "Solicitud inválida", "El JSON está mal formado o contiene valores no válidos", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return error(400, "Solicitud inválida", "El parámetro '" + ex.getName() + "' no tiene un valor válido", request, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> parametroAusente(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return error(400, "Solicitud inválida", "Falta el parámetro '" + ex.getParameterName() + "'", request, null);
    }

    @ExceptionHandler(ParametroInvalidoException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(ParametroInvalidoException ex, HttpServletRequest request) {
        return error(400, "Solicitud inválida", ex.getMessage(), request, null);
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> rutaInexistente(Exception ex, HttpServletRequest request) {
        return error(404, "No encontrado", "La ruta solicitada no existe", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoInvalido(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        var respuesta = error(405, "Método no permitido", "El método HTTP no está permitido en esta ruta", request, null);
        return ResponseEntity.status(405).header("Allow",
                String.join(", ", Objects.requireNonNullElse(ex.getSupportedMethods(), new String[0])))
                .body(respuesta.getBody());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        return error(409, "Conflicto", "La operación viola una restricción de integridad: revise duplicados y relaciones", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return error(500, "Error interno", "Ocurrió un error inesperado al procesar la solicitud", request, null);
    }

    private ResponseEntity<ErrorResponse> error(int status, String tipo, String mensaje,
            HttpServletRequest request, Map<String, String> detalles) {
        String requestId = MDC.get("requestId");
        if (requestId == null) requestId = (String) request.getAttribute("requestId");
        return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(), status, tipo,
                mensaje, request.getRequestURI(), requestId, detalles));
    }
}

