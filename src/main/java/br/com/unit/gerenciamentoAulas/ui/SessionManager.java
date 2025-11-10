package br.com.unit.gerenciamentoAulas.ui;

import org.springframework.stereotype.Component;

import br.com.unit.gerenciamentoAulas.entidades.PerfilUsuario;
import javafx.scene.control.Alert;

@Component
public class SessionManager {
    
    private static SessionManager instance;
    private PerfilUsuario perfilAtual = PerfilUsuario.ADMINISTRADOR;
    private Long usuarioAtualId = 1L;
    
    public SessionManager() {
        instance = this;
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public PerfilUsuario getPerfilAtual() {
        return perfilAtual;
    }
    
    public void setPerfilAtual(PerfilUsuario perfil) {
        this.perfilAtual = perfil;
        System.out.println("🔄 Perfil alterado para: " + perfil.getDescricao());
    }
    
    public Long getUsuarioAtualId() {
        return usuarioAtualId;
    }
    
    public void setUsuarioAtualId(Long usuarioId) {
        this.usuarioAtualId = usuarioId;
    }
    
    public boolean podeCriarAula() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeEditarAula() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeEditarConteudo() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR || 
               perfilAtual == PerfilUsuario.INSTRUTOR;
    }
    
    public boolean podeCancelarAula() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeDeletarAula() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeCriarCurso() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeEditarCurso() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeDeletarCurso() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeCriarInstrutor() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeEditarInstrutor() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeDeletarInstrutor() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeCriarLocal() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeEditarLocal() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeDeletarLocal() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeCriarInscricao() {
        return perfilAtual == PerfilUsuario.ALUNO;
    }
    
    public boolean podeCancelarInscricao() {
        return perfilAtual == PerfilUsuario.ALUNO;
    }
    
    public boolean podeGerenciarCapacidade() {
        return perfilAtual == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeEditarAulaDoInstrutor(Long instrutorIdDaAula) {
        if (perfilAtual == PerfilUsuario.ADMINISTRADOR) {
            return true;
        }
        
        if (perfilAtual == PerfilUsuario.INSTRUTOR) {
            return instrutorIdDaAula != null && instrutorIdDaAula.equals(usuarioAtualId);
        }
        
        return false;
    }
    
    public void mostrarAcessoNegadoAulaNaoPropria() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Acesso Negado");
        alert.setHeaderText("⛔ Você não pode editar esta aula");
        alert.setContentText(
            "Como Instrutor, você só pode editar ou cancelar suas próprias aulas.\n\n" +
            "Esta aula pertence a outro instrutor."
        );
        alert.showAndWait();
        
        System.err.println("❌ ACESSO NEGADO: Instrutor tentou editar aula de outro instrutor");
    }
    
    public void mostrarAcessoNegado(String acao) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Acesso Negado");
        alert.setHeaderText("⛔ Você não tem permissão para esta ação");
        alert.setContentText(String.format(
            "Ação: %s\n\n" +
            "Seu perfil atual: %s\n\n" +
            "Esta funcionalidade está disponível apenas para:\n%s",
            acao,
            perfilAtual.getDescricao(),
            obterPerfisPermitidos(acao)
        ));
        alert.showAndWait();
        
        System.err.println("❌ ACESSO NEGADO: " + perfilAtual.getDescricao() + 
                          " tentou executar: " + acao);
    }
    
    private String obterPerfisPermitidos(String acao) {
        if (acao.contains("Criar Aula") || acao.contains("Editar Aula") || 
            acao.contains("Cancelar Aula") || acao.contains("Deletar Aula")) {
            return "• Administrador";
        }
        if (acao.contains("Editar Conteúdo") || acao.contains("Material")) {
            return "• Administrador\n• Instrutor";
        }
        if (acao.contains("Inscrição") || acao.contains("Inscrever")) {
            return "• Aluno";
        }
        if (acao.contains("Curso") || acao.contains("Instrutor") || 
            acao.contains("Local") || acao.contains("Capacidade")) {
            return "• Administrador";
        }
        return "• Administrador";
    }
    
    public boolean validarPermissao(String acao, PermissaoCallback callback) {
        boolean temPermissao = false;
        
        switch (acao) {
            case "CRIAR_AULA":
                temPermissao = podeCriarAula();
                break;
            case "EDITAR_AULA":
                temPermissao = podeEditarAula();
                break;
            case "DELETAR_AULA":
                temPermissao = podeDeletarAula();
                break;
            case "CRIAR_CURSO":
                temPermissao = podeCriarCurso();
                break;
            case "EDITAR_CURSO":
                temPermissao = podeEditarCurso();
                break;
            case "DELETAR_CURSO":
                temPermissao = podeDeletarCurso();
                break;
            case "CRIAR_INSTRUTOR":
                temPermissao = podeCriarInstrutor();
                break;
            case "EDITAR_INSTRUTOR":
                temPermissao = podeEditarInstrutor();
                break;
            case "DELETAR_INSTRUTOR":
                temPermissao = podeDeletarInstrutor();
                break;
            case "CRIAR_LOCAL":
                temPermissao = podeCriarLocal();
                break;
            case "EDITAR_LOCAL":
                temPermissao = podeEditarLocal();
                break;
            case "DELETAR_LOCAL":
                temPermissao = podeDeletarLocal();
                break;
            case "CRIAR_INSCRICAO":
                temPermissao = podeCriarInscricao();
                break;
            default:
                temPermissao = false;
        }
        
        if (temPermissao) {
            callback.executar();
        } else {
            mostrarAcessoNegado(acao.replace("_", " "));
        }
        
        return temPermissao;
    }
    
    @FunctionalInterface
    public interface PermissaoCallback {
        void executar();
    }
}
