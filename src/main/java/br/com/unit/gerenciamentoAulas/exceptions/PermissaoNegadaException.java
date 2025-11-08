package br.com.unit.gerenciamentoAulas.exceptions;

/**
 * Exceção lançada quando um usuário tenta executar uma ação sem permissão
 */
public class PermissaoNegadaException extends RuntimeException {
    
    public PermissaoNegadaException(String mensagem) {
        super(mensagem);
    }
    
    public PermissaoNegadaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
