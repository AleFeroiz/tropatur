package br.com.tropatur.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.method.ParameterErrors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PontoNaoEncontradoException.class)
    public ResponseEntity<ApiError> naoEncontrado(PontoNaoEncontradoException ex, HttpServletRequest request) {
        return erro(404, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return erro(400, "Confira os campos informados", request, campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> validacaoMetodo(HandlerMethodValidationException ex, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(resultado -> {
            if (resultado instanceof ParameterErrors erros) {
                erros.getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
            } else {
                resultado.getResolvableErrors().forEach(e -> campos.putIfAbsent("id", "O id deve ser um número inteiro positivo"));
            }
        });
        return erro(400, "Confira os campos informados", request, campos);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiError> parametro(Exception ex, HttpServletRequest request) {
        return erro(400, "O id deve ser um número inteiro positivo", request, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return erro(400, "JSON inválido. Confira os tipos, os campos e use ponto como separador decimal", request, Map.of());
    }

    private ResponseEntity<ApiError> erro(int status, String mensagem, HttpServletRequest request, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status, mensagem, request.getRequestURI(), campos));
    }
}
