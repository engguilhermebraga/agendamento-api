package com.guilhermebraga.agendamento_api.exception;

/**
 * Exceção lançada quando um recurso não é encontrado no banco.
 * Mapeada para HTTP 404 Not Found.
 *
 * @author Guilherme Braga
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " com ID " + id + " não encontrado(a).");
    }
}