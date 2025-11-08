package br.com.unit.gerenciamentoAulas.servicos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.unit.gerenciamentoAulas.entidades.AcaoSistema;
import br.com.unit.gerenciamentoAulas.entidades.PerfilUsuario;
import br.com.unit.gerenciamentoAulas.entidades.Usuario;
import br.com.unit.gerenciamentoAulas.exceptions.PermissaoNegadaException;

@Service
public class AuditoriaService {
    
    @Autowired
    private PermissaoService permissaoService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    /**
     * 
     * @param usuario 
     * @param acao 
     * @param detalhes
     * @throws PermissaoNegadaException 
     */
    public void registrarAcao(Usuario usuario, AcaoSistema acao, String detalhes) {
        PerfilUsuario perfil = usuario.getPerfil();
        LocalDateTime dataHora = LocalDateTime.now();

        boolean temPermissao = permissaoService.temPermissao(perfil, acao);

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

        if (!temPermissao) {
            throw new PermissaoNegadaException(
                String.format("Usuário '%s' (%s) não tem permissão para '%s'",
                    usuario.getNome(),
                    perfil.getDescricao(),
                    acao.getDescricao())
            );
        }
    }

    public void validarPermissao(Usuario usuario, AcaoSistema acao) {
        PerfilUsuario perfil = usuario.getPerfil();
        permissaoService.validarPermissao(perfil, acao);
    }

    public boolean temPermissao(Usuario usuario, AcaoSistema acao) {
        PerfilUsuario perfil = usuario.getPerfil();
        return permissaoService.temPermissao(perfil, acao);
    }

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