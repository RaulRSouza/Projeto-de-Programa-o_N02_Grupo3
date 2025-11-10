package br.com.unit.gerenciamentoAulas.servicos;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.unit.gerenciamentoAulas.entidades.AcaoSistema;
import br.com.unit.gerenciamentoAulas.entidades.PerfilUsuario;
import br.com.unit.gerenciamentoAulas.exceptions.PermissaoNegadaException;

/**
 * Service responsável por gerenciar permissões de cada perfil de usuário
 * 
 * REGRAS DE NEGÓCIO:
 * 
 * ADMINISTRADOR:
 * - Criar, editar e cancelar aulas
 * - Associar instrutores e definir locais
 * - Gerenciar capacidade (ajustar vagas)
 * - Visualizar todas as informações
 * - Gerenciar cursos e usuários
 * 
 * INSTRUTOR:
 * - Visualizar suas aulas
 * - Editar conteúdos das aulas
 * - Solicitar cancelamentos (não cancelar diretamente)
 * - Visualizar inscrições de suas aulas
 * 
 * ALUNO:
 * - Visualizar aulas disponíveis
 * - Inscrever-se em aulas
 * - Cancelar suas próprias inscrições
 * - Visualizar cursos
 */
@Service
public class PermissaoService {
    
    private final Map<PerfilUsuario, Set<AcaoSistema>> permissoes;
    
    public PermissaoService() {
        this.permissoes = new HashMap<>();
        inicializarPermissoes();
    }
    
    private void inicializarPermissoes() {
        // ===== PERMISSÕES DO ADMINISTRADOR =====
        permissoes.put(PerfilUsuario.ADMINISTRADOR, EnumSet.of(
            // Aulas
            AcaoSistema.CRIAR_AULA,
            AcaoSistema.EDITAR_AULA,
            AcaoSistema.CANCELAR_AULA,
            AcaoSistema.VISUALIZAR_AULA,
            
            // Instrutores
            AcaoSistema.ASSOCIAR_INSTRUTOR,
            AcaoSistema.REMOVER_INSTRUTOR,
            
            // Capacidade
            AcaoSistema.GERENCIAR_CAPACIDADE,
            AcaoSistema.AJUSTAR_VAGAS,
            
            // Local
            AcaoSistema.DEFINIR_LOCAL,
            AcaoSistema.ALTERAR_LOCAL,
            
            // Inscrições
            AcaoSistema.VISUALIZAR_INSCRICOES,
            
            // Cursos
            AcaoSistema.CRIAR_CURSO,
            AcaoSistema.EDITAR_CURSO,
            AcaoSistema.EXCLUIR_CURSO,
            AcaoSistema.VISUALIZAR_CURSO,
            
            // Gestão
            AcaoSistema.GERENCIAR_USUARIOS,
            AcaoSistema.VISUALIZAR_RELATORIOS,
            AcaoSistema.EXPORTAR_DADOS
        ));
        
        // ===== PERMISSÕES DO INSTRUTOR =====
        permissoes.put(PerfilUsuario.INSTRUTOR, EnumSet.of(
            // Aulas (somente visualização e edição de conteúdo)
            AcaoSistema.VISUALIZAR_AULA,
            AcaoSistema.EDITAR_CONTEUDO_AULA,
            AcaoSistema.SOLICITAR_CANCELAMENTO_AULA, // NÃO pode cancelar diretamente
            
            // Inscrições (somente visualização)
            AcaoSistema.VISUALIZAR_INSCRICOES,
            
            // Cursos (somente visualização)
            AcaoSistema.VISUALIZAR_CURSO
        ));
        
        // ===== PERMISSÕES DO ALUNO =====
        permissoes.put(PerfilUsuario.ALUNO, EnumSet.of(
            // Aulas (somente visualização)
            AcaoSistema.VISUALIZAR_AULA,
            
            // Inscrições
            AcaoSistema.INSCREVER_SE,
            AcaoSistema.CANCELAR_INSCRICAO,
            AcaoSistema.VISUALIZAR_INSCRICOES, // Apenas suas próprias
            
            // Cursos (somente visualização)
            AcaoSistema.VISUALIZAR_CURSO
        ));
    }
    
    /**
     * Verifica se um perfil tem permissão para realizar determinada ação
     */
    public boolean temPermissao(PerfilUsuario perfil, AcaoSistema acao) {
        Set<AcaoSistema> acoesPermitidas = permissoes.get(perfil);
        return acoesPermitidas != null && acoesPermitidas.contains(acao);
    }
    
    /**
     * Valida se um perfil pode executar uma ação, lançando exceção se não puder
     */
    public void validarPermissao(PerfilUsuario perfil, AcaoSistema acao) {
        if (!temPermissao(perfil, acao)) {
            throw new PermissaoNegadaException(
                String.format("Perfil '%s' não tem permissão para '%s'",
                    perfil.getDescricao(),
                    acao.getDescricao())
            );
        }
    }
    
    /**
     * Retorna todas as ações permitidas para um perfil
     */
    public Set<AcaoSistema> obterPermissoes(PerfilUsuario perfil) {
        return permissoes.getOrDefault(perfil, EnumSet.noneOf(AcaoSistema.class));
    }
    
    /**
     * Verifica se o perfil pode criar/editar/cancelar aulas
     */
    public boolean podeGerenciarAulas(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.ADMINISTRADOR;
    }
    
    /**
     * Verifica se o perfil pode editar conteúdos de aula
     */
    public boolean podeEditarConteudo(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.ADMINISTRADOR || perfil == PerfilUsuario.INSTRUTOR;
    }
    
    /**
     * Verifica se o perfil pode gerenciar inscrições (criar para outros usuários)
     */
    public boolean podeGerenciarInscricoes(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.ADMINISTRADOR;
    }
    
    /**
     * Verifica se o perfil pode se inscrever em aulas
     */
    public boolean podeSeInscrever(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.ALUNO;
    }
}
