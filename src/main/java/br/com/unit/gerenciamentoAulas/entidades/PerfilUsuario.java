package br.com.unit.gerenciamentoAulas.entidades;

/**
 * Enum que define os perfis de usuário e suas permissões no sistema
 */
public enum PerfilUsuario {
    
    ADMINISTRADOR("Administrador"),
    INSTRUTOR("Instrutor"),
    ALUNO("Aluno");
    
    private final String descricao;
    
    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
