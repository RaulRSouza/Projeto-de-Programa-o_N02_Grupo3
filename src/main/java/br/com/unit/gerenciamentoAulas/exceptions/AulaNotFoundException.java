package br.com.unit.gerenciamentoAulas.exceptions;

public class AulaNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AulaNotFoundException(String message) {
        super(message);
    }

    public AulaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}