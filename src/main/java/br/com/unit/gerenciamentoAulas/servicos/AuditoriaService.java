package br.com.unit.gerenciamentoAulas.servicos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.unit.gerenciamentoAulas.entidades.AcaoSistema;
import br.com.unit.gerenciamentoAulas.entidades.PerfilUsuario;
import br.com.unit.gerenciamentoAulas.entidades.Usuario;
import br.com.unit.gerenciamentoAulas.exceptions.PermissaoNegadaException;

/**
 * Service responsável por auditar ações dos usuários no sistema
 * 
 * Registra:
 * - Quem executou a ação (usuário e perfil)
 * - Qual ação foi executada
 * - Quando foi executada
 * - Se foi permitida ou negada
 * - Detalhes adicionais
 */
@Service
public class AuditoriaService {
    
    @Autowired
    private PermissaoService permissaoService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Registra e valida uma ação executada por um usuário
     * 
     * @param usuario Usuário que está executando a ação
     * @param acao Ação sendo executada
     * @param detalhes Detalhes adicionais sobre a ação
     * @throws PermissaoNegadaException se o usuário não tem permissão
     */
    public void registrarAcao(Usuario usuario, AcaoSistema acao, String detalhes) {
        PerfilUsuario perfil = usuario.getPerfil();
        LocalDateTime dataHora = LocalDateTime.now();
        
        // Valida se o usuário tem permissão
        boolean temPermissao = permissaoService.temPermissao(perfil, acao);
        
        // Registra no log
        String log = String.format(
            "[AUDITORIA] %s | Usuário: %s (ID: %d) | Perfil: %s | Ação: %s | Status: %s | Detalhes: %s",
            dataHora.format(FORMATTER),
            usuario.getNome(),
            usuario.getId(),
            perfil.getDescricao(),
            acao.getDescricao(),
            temPermissao ? "PERMITIDO" : "NEGADO",
            detalhes != null ? detalhes : "N/A"
        );
        
        System.out.println(log);
        
        // Se não tem permissão, lança exceção
        if (!temPermissao) {
            throw new PermissaoNegadaException(
                String.format("Usuário '%s' (%s) não tem permissão para '%s'",
                    usuario.getNome(),
                    perfil.getDescricao(),
                    acao.getDescricao())
            );
        }
    }
    
    /**
     * Valida permissão sem registrar em log (para validações rápidas)
     */
    public void validarPermissao(Usuario usuario, AcaoSistema acao) {
        PerfilUsuario perfil = usuario.getPerfil();
        permissaoService.validarPermissao(perfil, acao);
    }
    
    /**
     * Verifica se usuário tem permissão sem lançar exceção
     */
    public boolean temPermissao(Usuario usuario, AcaoSistema acao) {
        PerfilUsuario perfil = usuario.getPerfil();
        return permissaoService.temPermissao(perfil, acao);
    }
    
    /**
     * Registra tentativa de acesso negado
     */
    public void registrarAcessoNegado(Usuario usuario, AcaoSistema acao, String motivo) {
        PerfilUsuario perfil = usuario.getPerfil();
        LocalDateTime dataHora = LocalDateTime.now();
        
        String log = String.format(
            "[SEGURANÇA] %s | Usuário: %s (ID: %d) | Perfil: %s | Tentou: %s | ACESSO NEGADO | Motivo: %s",
            dataHora.format(FORMATTER),
            usuario.getNome(),
            usuario.getId(),
            perfil.getDescricao(),
            acao.getDescricao(),
            motivo
        );
        
        System.err.println(log);
    }
    
    /**
     * VALIDAÇÕES ESPECÍFICAS POR PERFIL
     */
    
    /**
     * Valida se o usuário pode criar aulas (apenas Administrador)
     */
    public void validarCriacaoAula(Usuario usuario) {
        if (usuario.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            registrarAcessoNegado(usuario, AcaoSistema.CRIAR_AULA, 
                "Apenas Administradores podem criar aulas");
            throw new PermissaoNegadaException(
                "Apenas Administradores podem criar aulas. " +
                "Perfil atual: " + usuario.getPerfil().getDescricao()
            );
        }
    }
    
    /**
     * Valida se o usuário pode editar aulas (apenas Administrador)
     */
    public void validarEdicaoAula(Usuario usuario) {
        if (usuario.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            registrarAcessoNegado(usuario, AcaoSistema.EDITAR_AULA,
                "Apenas Administradores podem editar aulas completamente");
            throw new PermissaoNegadaException(
                "Apenas Administradores podem editar aulas. " +
                "Instrutores podem editar apenas o conteúdo. " +
                "Perfil atual: " + usuario.getPerfil().getDescricao()
            );
        }
    }
    
    /**
     * Valida se o usuário pode cancelar aulas (apenas Administrador)
     */
    public void validarCancelamentoAula(Usuario usuario) {
        if (usuario.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            registrarAcessoNegado(usuario, AcaoSistema.CANCELAR_AULA,
                "Apenas Administradores podem cancelar aulas diretamente");
            throw new PermissaoNegadaException(
                "Apenas Administradores podem cancelar aulas. " +
                "Instrutores podem solicitar cancelamento. " +
                "Perfil atual: " + usuario.getPerfil().getDescricao()
            );
        }
    }
    
    /**
     * Valida se o usuário pode editar conteúdo de aula (Administrador ou Instrutor)
     */
    public void validarEdicaoConteudo(Usuario usuario) {
        PerfilUsuario perfil = usuario.getPerfil();
        if (perfil != PerfilUsuario.ADMINISTRADOR && perfil != PerfilUsuario.INSTRUTOR) {
            registrarAcessoNegado(usuario, AcaoSistema.EDITAR_CONTEUDO_AULA,
                "Apenas Administradores e Instrutores podem editar conteúdo");
            throw new PermissaoNegadaException(
                "Apenas Administradores e Instrutores podem editar conteúdo de aulas. " +
                "Perfil atual: " + perfil.getDescricao()
            );
        }
    }
    
    /**
     * Valida se o usuário pode se inscrever em aulas (apenas Aluno)
     */
    public void validarInscricao(Usuario usuario) {
        if (usuario.getPerfil() != PerfilUsuario.ALUNO) {
            registrarAcessoNegado(usuario, AcaoSistema.INSCREVER_SE,
                "Apenas Alunos podem se inscrever em aulas");
            throw new PermissaoNegadaException(
                "Apenas Alunos podem se inscrever em aulas. " +
                "Perfil atual: " + usuario.getPerfil().getDescricao()
            );
        }
    }
    
    /**
     * Valida se o usuário pode gerenciar capacidade (apenas Administrador)
     */
    public void validarGerenciamentoCapacidade(Usuario usuario) {
        if (usuario.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            registrarAcessoNegado(usuario, AcaoSistema.GERENCIAR_CAPACIDADE,
                "Apenas Administradores podem gerenciar capacidade");
            throw new PermissaoNegadaException(
                "Apenas Administradores podem gerenciar a capacidade das aulas. " +
                "Perfil atual: " + usuario.getPerfil().getDescricao()
            );
        }
    }
    
    /**
     * Valida se o usuário pode associar instrutores (apenas Administrador)
     */
    public void validarAssociacaoInstrutor(Usuario usuario) {
        if (usuario.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            registrarAcessoNegado(usuario, AcaoSistema.ASSOCIAR_INSTRUTOR,
                "Apenas Administradores podem associar instrutores");
            throw new PermissaoNegadaException(
                "Apenas Administradores podem associar instrutores às aulas. " +
                "Perfil atual: " + usuario.getPerfil().getDescricao()
            );
        }
    }
    
    /**
     * Registra solicitação de cancelamento de instrutor
     */
    public void registrarSolicitacaoCancelamento(Usuario instrutor, Long aulaId, String motivo) {
        if (instrutor.getPerfil() != PerfilUsuario.INSTRUTOR) {
            throw new PermissaoNegadaException("Apenas instrutores podem solicitar cancelamentos");
        }
        
        LocalDateTime dataHora = LocalDateTime.now();
        String log = String.format(
            "[SOLICITAÇÃO] %s | Instrutor: %s (ID: %d) | Aula ID: %d | Ação: Solicitou cancelamento | Motivo: %s",
            dataHora.format(FORMATTER),
            instrutor.getNome(),
            instrutor.getId(),
            aulaId,
            motivo
        );
        
        System.out.println(log);
        System.out.println("[SISTEMA] Solicitação registrada. Aguardando aprovação do Administrador.");
    }
}
