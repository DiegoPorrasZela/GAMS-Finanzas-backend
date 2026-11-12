package com.gams.finanzas.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Convierte las excepciones en respuestas JSON con el formato estándar ProblemDetail (RFC 9457).
 * Así los controladores no tienen try/catch y todos los errores tienen la misma forma.
 *
 * Hereda de ResponseEntityExceptionHandler, que ya maneja los errores propios de Spring MVC
 * (método no permitido, tipo de contenido, etc.). Patrón Template Method: solo se sobrescriben
 * los pasos que queremos cambiar (mensajes en español y errores por campo).
 */
@RestControllerAdvice
public class ManejadorGlobalErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    // ---------- Errores del negocio ----------

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ProblemDetail datosInvalidos(DatosInvalidosException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail credencialesInvalidas(CredencialesInvalidasException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail conflicto(ConflictoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DemasiadosIntentosException.class)
    public ProblemDetail demasiadosIntentos(DemasiadosIntentosException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    /** Respaldo por si la base de datos rechaza algo que el servicio no detectó (por ejemplo, un duplicado). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridad(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La operación no se puede realizar porque rompe una regla de la base de datos");
    }

    /** Cualquier error no previsto: 500 sin mostrar detalles internos al cliente (el detalle queda en el log). */
    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Inténtalo otra vez en unos minutos.");
    }

    // ---------- Errores de Spring MVC con mensajes propios ----------

    /** Errores de @Valid: devuelve un mensaje por cada campo inválido. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Hay datos inválidos");
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }

    /** JSON mal formado o un dato con tipo incorrecto (ej. texto donde va un número, tipo "OTRO"). */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido o algún dato tiene un formato incorrecto");
    }

    /** Parámetro de la URL con formato incorrecto (ej. /api/movimientos/abc). */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String parametro = ex instanceof MethodArgumentTypeMismatchException conNombre
                ? conNombre.getName()
                : ex.getPropertyName();
        return respuesta(HttpStatus.BAD_REQUEST, "El parámetro \"" + parametro + "\" tiene un valor inválido");
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED,
                "El método " + ex.getMethod() + " no está permitido en esta ruta");
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, "La ruta solicitada no existe");
    }

    private static ResponseEntity<Object> respuesta(HttpStatus estado, String detalle) {
        return ResponseEntity.status(estado).body(ProblemDetail.forStatusAndDetail(estado, detalle));
    }
}
