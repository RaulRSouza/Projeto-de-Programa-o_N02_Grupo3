package br.com.unit.gerenciamentoAulas.servicos;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.unit.gerenciamentoAulas.entidades.AcaoSistema;
import br.com.unit.gerenciamentoAulas.entidades.PerfilUsuario;
import br.com.unit.gerenciamentoAulas.exceptions.PermissaoNegadaException;

@Service
public class PermissaoService {
    
    private final Map<PerfilUsuario, Set<AcaoSistema>> permissoes;
    
    public PermissaoService() {
        this.permissoes = new HashMap<>();
        inicializarPermissoes();
    }
    
    private void inicializarPermissoes() {
        permissoes.put(PerfilUsuario.ADMINISTRADOR, EnumSet.of(
            AcaoSistema.CRIAR_AULA,
            AcaoSistema.EDITAR_AULA,
            AcaoSistema.CANCELAR_AULA,
            AcaoSistema.VISUALIZAR_AULA,
            AcaoSistema.ASSOCIAR_INSTRUTOR,
            AcaoSistema.REMOVER_INSTRUTOR,
            AcaoSistema.GERENCIAR_CAPACIDADE,
            AcaoSistema.AJUSTAR_VAGAS,
            AcaoSistema.DEFINIR_LOCAL,
            AcaoSistema.ALTERAR_LOCAL,
            AcaoSistema.VISUALIZAR_INSCRICOES,
            AcaoSistema.CRIAR_CURSO,
            AcaoSistema.EDITAR_CURSO,
            AcaoSistema.EXCLUIR_CURSO,
            AcaoSistema.VISUALIZAR_CURSO,
            AcaoSistema.GERENCIAR_USUARIOS,
            AcaoSistema.VISUALIZAR_RELATORIOS,
            AcaoSistema.EXPORTAR_DADOS
        ));
        
        permissoes.put(PerfilUsuario.INSTRUTOR, EnumSet.of(
            AcaoSistema.VISUALIZAR_AULA,
            AcaoSistema.EDITAR_CONTEUDO_AULA,
            AcaoSistema.SOLICITAR_CANCELAMENTO_AULA,
            AcaoSistema.VISUALIZAR_INSCRICOES,
            AcaoSistema.VISUALIZAR_CURSO
        ));

        permissoes.put(PerfilUsuario.ALUNO, EnumSet.of(
            AcaoSistema.VISUALIZAR_AULA,
            AcaoSistema.INSCREVER_SE,
            AcaoSistema.CANCELAR_INSCRICAO,
            AcaoSistema.VISUALIZAR_INSCRICOES,
            AcaoSistema.VISUALIZAR_CURSO
        ));
    }

    public boolean temPermissao(PerfilUsuario perfil, AcaoSistema acao) {
        Set<AcaoSistema> acoesPermitidas = permissoes.get(perfil);
        return acoesPermitidas != null && acoesPermitidas.contains(acao);
    }

    public void validarPermissao(PerfilUsuario perfil, AcaoSistema acao) {
        if (!temPermissao(perfil, acao)) {
            throw new PermissaoNegadaException(
                String.format("Perfil '%s' não tem permissão para '%s'",
                    perfil.getDescricao(),
                    acao.getDescricao())
            );
        }
    }

    public Set<AcaoSistema> obterPermissoes(PerfilUsuario perfil) {
        return permissoes.getOrDefault(perfil, EnumSet.noneOf(AcaoSistema.class));
    }

    public boolean podeGerenciarAulas(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.ADMINISTRADOR;
    }

    public boolean podeEditarConteudo(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.ADMINISTRADOR || perfil == PerfilUsuario.INSTRUTOR;
    }

    public boolean podeGerenciarInscricoes(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.ADMINISTRADOR;
    }
    
    public boolean podeSeInscrever(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.ALUNO;
    }
}