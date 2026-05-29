package com.guilhermebraga.agendamento_api.exception;

import com.guilhermebraga.agendamento_api.controller.AgendamentoController;
import com.guilhermebraga.agendamento_api.controller.ClienteController;
import com.guilhermebraga.agendamento_api.controller.ProfissionalController;
import com.guilhermebraga.agendamento_api.controller.ServicoController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Intercepta e trata exceções lançadas APENAS nos controllers REST da API.
 * <p>
 * O escopo é restrito via assignableTypes para evitar que o handler
 * interfira no H2 Console, Swagger UI e outros servlets internos do Spring.
 *
 * @author Guilherme Braga
 */
@RestControllerAdvice(assignableTypes = {
        AgendamentoController.class,
        ClienteController.class,
        ProfissionalController.class,
        ServicoController.class
})
public class GlobalExceptionHandler {

    /**
     * Trata erros de validação do @Valid — retorna 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST, "Erro de validação", erros));
    }

    /**
     * Trata recurso não encontrado — retorna 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND, ex.getMessage(), null));
    }

    /**
     * Trata violação de regra de negócio — retorna 409.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT, ex.getMessage(), null));
    }

    /**
     * Captura qualquer exceção não tratada nos controllers REST — retorna 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro interno: " + ex.getMessage(), null));
    }

    /**
     * Monta o corpo padronizado de todas as respostas de erro.
     */
    private Map<String, Object> buildBody(HttpStatus status,
                                          String message,
                                          Object detalhes) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("erro", status.getReasonPhrase());
        body.put("mensagem", message);
        if (detalhes != null) {
            body.put("detalhes", detalhes);
        }
        return body;
    }
}