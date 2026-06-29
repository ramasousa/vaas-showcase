package com.bradesco.vaas.exception;

import com.bradesco.vaas.dto.Dtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PagamentoNaoAprovadoException.class)
    public ResponseEntity<ErrorResponse> handlePagamentoNaoAprovado(PagamentoNaoAprovadoException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("PAGAMENTO_BLOQUEADO_PELO_GATE", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Requisição inválida");
        return ResponseEntity.badRequest().body(new ErrorResponse("REQUISICAO_INVALIDA", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERRO_INTERNO", ex.getMessage()));
    }
}
