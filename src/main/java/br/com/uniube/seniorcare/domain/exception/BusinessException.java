package br.com.uniube.seniorcare.domain.exception;

/**
 * BusinessException is a custom runtime exception used to signal business rule violations.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
