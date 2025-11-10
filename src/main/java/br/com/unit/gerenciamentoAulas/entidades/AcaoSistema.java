package br.com.unit.gerenciamentoAulas.entidades;

/**
 * Enum que define todas as ações possíveis no sistema
 */
public enum AcaoSistema {
    
    // ===== AÇÕES DE AULA =====
    CRIAR_AULA("Criar Aula"),
    EDITAR_AULA("Editar Aula"),
    CANCELAR_AULA("Cancelar Aula"),
    VISUALIZAR_AULA("Visualizar Aula"),
    SOLICITAR_CANCELAMENTO_AULA("Solicitar Cancelamento de Aula"),
    
    // ===== AÇÕES DE INSTRUTOR =====
    ASSOCIAR_INSTRUTOR("Associar Instrutor a Aula"),
    REMOVER_INSTRUTOR("Remover Instrutor de Aula"),
    EDITAR_CONTEUDO_AULA("Editar Conteúdo da Aula"),
    
    // ===== AÇÕES DE CAPACIDADE =====
    GERENCIAR_CAPACIDADE("Gerenciar Capacidade de Aula"),
    AJUSTAR_VAGAS("Ajustar Vagas"),
    
    // ===== AÇÕES DE LOCAL =====
    DEFINIR_LOCAL("Definir Local da Aula"),
    ALTERAR_LOCAL("Alterar Local da Aula"),
    
    // ===== AÇÕES DE INSCRIÇÃO =====
    VISUALIZAR_INSCRICOES("Visualizar Inscrições"),
    INSCREVER_SE("Inscrever-se em Aula"),
    CANCELAR_INSCRICAO("Cancelar Inscrição"),
    
    // ===== AÇÕES DE CURSO =====
    CRIAR_CURSO("Criar Curso"),
    EDITAR_CURSO("Editar Curso"),
    EXCLUIR_CURSO("Excluir Curso"),
    VISUALIZAR_CURSO("Visualizar Curso"),
    
    // ===== AÇÕES DE GESTÃO =====
    GERENCIAR_USUARIOS("Gerenciar Usuários"),
    VISUALIZAR_RELATORIOS("Visualizar Relatórios"),
    EXPORTAR_DADOS("Exportar Dados");
    
    private final String descricao;
    
    AcaoSistema(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
