package com.guilhermebraga.agendamento_api.exception;

/**
 * Exceção lançada quando uma regra de negócio é violada.
 * Mapeada para HTTP 409 Conflict.
 * <p>
 * Exemplos: CPF duplicado, e-mail já cadastrado,
 * horário de agendamento já ocupado.
 *
 * @author Guilherme Braga
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensagem) {
        super(mensagem);
    }
}