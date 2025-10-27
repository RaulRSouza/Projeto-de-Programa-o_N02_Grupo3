package br.com.unit.gerenciamentoAulas.servicos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.entidades.Inscricao;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Local;
import br.com.unit.gerenciamentoAulas.exceptions.AulaNotFoundException;
import br.com.unit.gerenciamentoAulas.exceptions.BusinessException;
import br.com.unit.gerenciamentoAulas.exceptions.ConflictException;
import br.com.unit.gerenciamentoAulas.repositories.AulaRepository;
import br.com.unit.gerenciamentoAulas.repositories.CursoRepository;
import br.com.unit.gerenciamentoAulas.repositories.InstrutorRepository;
import br.com.unit.gerenciamentoAulas.repositories.LocalRepository;

/**
 * Serviço responsável pela gestão completa de aulas.
 * Implementa todas as regras de negócio, validações e controle de conflitos.
 *
 * @author Grupo 3 - Sistema de Gerenciamento de Aulas de Véridia
 */
@Service
@Transactional
public class AulaService {

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InstrutorRepository instrutorRepository;

    @Autowired
    private LocalRepository localRepository;

    /**
     * Cria uma nova aula no sistema com validações completas.
     *
     * @param cursoId ID do curso associado
     * @param instrutorId ID do instrutor responsável
     * @param localId ID do local onde será ministrada
     * @param dataHoraInicio Data e hora de início
     * @param dataHoraFim Data e hora de término
     * @param vagasTotais Quantidade total de vagas
     * @param observacoes Observações adicionais (opcional)
     * @return Aula criada
     * @throws BusinessException se houver erro de validação
     * @throws ConflictException se houver conflito de horário
     */
    public Aula criarAula(Long cursoId, Long instrutorId, Long localId,
                          LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                          int vagasTotais, String observacoes) {

        // Validações básicas
        validarDatasAula(dataHoraInicio, dataHoraFim);
        validarVagasTotais(vagasTotais);

        // Buscar entidades relacionadas
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("Curso não encontrado com ID: " + cursoId));

        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new BusinessException("Instrutor não encontrado com ID: " + instrutorId));

        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new BusinessException("Local não encontrado com ID: " + localId));

        // Validações de regras de negócio
        validarCursoAtivo(curso);
        validarLocalDisponivel(local);
        validarCapacidadeLocal(vagasTotais, local);

        // Validações de conflitos
        validarConflitoInstrutor(instrutor, dataHoraInicio, dataHoraFim, null);
        validarConflitoLocal(local, dataHoraInicio, dataHoraFim, null);

        // Criar nova aula
        Aula novaAula = new Aula();
        novaAula.setCurso(curso);
        novaAula.setInstrutor(instrutor);
        novaAula.setLocal(local);
        novaAula.setDataHoraInicio(dataHoraInicio);
        novaAula.setDataHoraFim(dataHoraFim);
        novaAula.setVagasTotais(vagasTotais);
        novaAula.setVagasDisponiveis(vagasTotais);
        novaAula.setStatus("AGENDADA");
        novaAula.setObservacoes(observacoes);

        Aula aulaSalva = aulaRepository.save(novaAula);

        return aulaSalva;
    }

    /**
     * Edita uma aula existente com revalidação completa.
     *
     * @param aulaId ID da aula a ser editada
     * @param cursoId ID do curso associado
     * @param instrutorId ID do instrutor responsável
     * @param localId ID do local onde será ministrada
     * @param dataHoraInicio Nova data e hora de início
     * @param dataHoraFim Nova data e hora de término
     * @param vagasTotais Nova quantidade total de vagas
     * @param observacoes Novas observações
     * @return Aula atualizada
     * @throws AulaNotFoundException se a aula não for encontrada
     * @throws BusinessException se houver erro de validação
     * @throws ConflictException se houver conflito de horário
     */
    public Aula editarAula(Long aulaId, Long cursoId, Long instrutorId, Long localId,
                          LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                          int vagasTotais, String observacoes) {

        // Buscar aula existente
        Aula aulaExistente = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new AulaNotFoundException("Aula não encontrada com ID: " + aulaId));

        // Validar se a aula pode ser editada
        validarAulaEditavel(aulaExistente);

        // Validações básicas
        validarDatasAula(dataHoraInicio, dataHoraFim);
        validarVagasTotais(vagasTotais);

        // Buscar entidades relacionadas
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("Curso não encontrado com ID: " + cursoId));

        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new BusinessException("Instrutor não encontrado com ID: " + instrutorId));

        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new BusinessException("Local não encontrado com ID: " + localId));

        // Validar vagas em relação às inscrições existentes
        int inscricoesAtivas = (int) aulaExistente.getInscricoes().stream()
                .filter(i -> "CONFIRMADA".equals(i.getStatus()))
                .count();

        if (vagasTotais < inscricoesAtivas) {
            throw new BusinessException(
                String.format("Não é possível reduzir vagas para %d. Há %d alunos inscritos.",
                             vagasTotais, inscricoesAtivas)
            );
        }

        // Validações de regras de negócio
        validarCursoAtivo(curso);
        validarLocalDisponivel(local);
        validarCapacidadeLocal(vagasTotais, local);

        // Validações de conflitos (excluindo a própria aula)
        validarConflitoInstrutor(instrutor, dataHoraInicio, dataHoraFim, aulaId);
        validarConflitoLocal(local, dataHoraInicio, dataHoraFim, aulaId);

        // Atualizar aula
        aulaExistente.setCurso(curso);
        aulaExistente.setInstrutor(instrutor);
        aulaExistente.setLocal(local);
        aulaExistente.setDataHoraInicio(dataHoraInicio);
        aulaExistente.setDataHoraFim(dataHoraFim);
        aulaExistente.setVagasTotais(vagasTotais);
        aulaExistente.setVagasDisponiveis(vagasTotais - inscricoesAtivas);
        aulaExistente.setObservacoes(observacoes);

        Aula aulaAtualizada = aulaRepository.save(aulaExistente);

        // Notificar alunos sobre alteração (implementação futura)
        notificarAlteracaoAula(aulaAtualizada);

        return aulaAtualizada;
    }

    /**
     * Cancela uma aula e notifica todos os alunos inscritos.
     *
     * @param aulaId ID da aula a ser cancelada
     * @param motivo Motivo do cancelamento
     * @return Aula cancelada
     * @throws AulaNotFoundException se a aula não for encontrada
     * @throws BusinessException se a aula já estiver cancelada
     */
    public Aula cancelarAula(Long aulaId, String motivo) {
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new AulaNotFoundException("Aula não encontrada com ID: " + aulaId));

        if ("CANCELADA".equals(aula.getStatus())) {
            throw new BusinessException("Aula já está cancelada");
        }

        // Atualizar status da aula
        aula.setStatus("CANCELADA");

        String observacoesAtualizadas = (aula.getObservacoes() != null ? aula.getObservacoes() + " | " : "")
                                       + "Cancelada em: " + LocalDateTime.now()
                                       + " | Motivo: " + motivo;
        aula.setObservacoes(observacoesAtualizadas);

        // Cancelar todas as inscrições
        for (Inscricao inscricao : aula.getInscricoes()) {
            inscricao.setStatus("CANCELADA");
            inscricao.setObservacoes("Aula cancelada: " + motivo);
        }

        Aula aulaCancelada = aulaRepository.save(aula);

        // Notificar alunos sobre cancelamento
        notificarCancelamentoAula(aulaCancelada, motivo);

        return aulaCancelada;
    }

    /**
     * Busca uma aula por ID.
     *
     * @param aulaId ID da aula
     * @return Aula encontrada
     * @throws AulaNotFoundException se a aula não for encontrada
     */
    @Transactional(readOnly = true)
    public Aula buscarPorId(Long aulaId) {
        return aulaRepository.findById(aulaId)
                .orElseThrow(() -> new AulaNotFoundException("Aula não encontrada com ID: " + aulaId));
    }

    /**
     * Lista todas as aulas do sistema.
     *
     * @return Lista de todas as aulas
     */
    @Transactional(readOnly = true)
    public List<Aula> listarTodas() {
        return aulaRepository.findAll();
    }

    /**
     * Lista aulas por curso.
     *
     * @param cursoId ID do curso
     * @return Lista de aulas do curso
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorCurso(Long cursoId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("Curso não encontrado com ID: " + cursoId));
        return aulaRepository.findByCurso(curso);
    }

    /**
     * Lista aulas por instrutor.
     *
     * @param instrutorId ID do instrutor
     * @return Lista de aulas do instrutor
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorInstrutor(Long instrutorId) {
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new BusinessException("Instrutor não encontrado com ID: " + instrutorId));
        return aulaRepository.findByInstrutor(instrutor);
    }

    /**
     * Lista aulas por local.
     *
     * @param localId ID do local
     * @return Lista de aulas do local
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorLocal(Long localId) {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new BusinessException("Local não encontrado com ID: " + localId));
        return aulaRepository.findByLocal(local);
    }

    /**
     * Lista aulas por status.
     *
     * @param status Status das aulas (AGENDADA, CANCELADA, etc.)
     * @return Lista de aulas com o status especificado
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorStatus(String status) {
        return aulaRepository.findByStatus(status);
    }

    /**
     * Lista aulas futuras (não canceladas e com data de início posterior à atual).
     *
     * @return Lista de aulas futuras
     */
    @Transactional(readOnly = true)
    public List<Aula> listarAulasFuturas() {
        return aulaRepository.findAulasFuturas(LocalDateTime.now());
    }

    /**
     * Lista aulas com vagas disponíveis.
     *
     * @return Lista de aulas com vagas
     */
    @Transactional(readOnly = true)
    public List<Aula> listarAulasDisponiveis() {
        return aulaRepository.findAulasComVagasDisponiveis(LocalDateTime.now());
    }

    /**
     * Lista aulas em um período específico.
     *
     * @param inicio Data/hora de início do período
     * @param fim Data/hora de fim do período
     * @return Lista de aulas no período
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        validarDatasAula(inicio, fim);
        return aulaRepository.findAulasPorPeriodo(inicio, fim);
    }

    // ==================== MÉTODOS DE VALIDAÇÃO ====================

    /**
     * Valida se as datas de início e fim da aula são válidas.
     */
    private void validarDatasAula(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new BusinessException("Data de início e fim são obrigatórias");
        }

        if (inicio.isAfter(fim)) {
            throw new BusinessException("Data de início não pode ser posterior à data de fim");
        }

        if (fim.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível criar aula com data no passado");
        }

        if (inicio.isEqual(fim)) {
            throw new BusinessException("Data de início não pode ser igual à data de fim");
        }
    }

    /**
     * Valida quantidade de vagas totais.
     */
    private void validarVagasTotais(int vagasTotais) {
        if (vagasTotais <= 0) {
            throw new BusinessException("Quantidade de vagas deve ser maior que zero");
        }

        if (vagasTotais > 1000) {
            throw new BusinessException("Quantidade de vagas não pode exceder 1000");
        }
    }

    /**
     * Valida se o curso está ativo.
     */
    private void validarCursoAtivo(Curso curso) {
        if (!curso.isAtivo()) {
            throw new BusinessException("Não é possível criar aula para curso inativo: " + curso.getNome());
        }
    }

    /**
     * Valida se o local está disponível.
     */
    private void validarLocalDisponivel(Local local) {
        if (!local.isDisponivel()) {
            throw new BusinessException("Local não está disponível: " + local.getNome());
        }
    }

    /**
     * Valida se a quantidade de vagas não excede a capacidade do local.
     */
    private void validarCapacidadeLocal(int vagasTotais, Local local) {
        if (vagasTotais > local.getCapacidade()) {
            throw new BusinessException(
                String.format("Vagas solicitadas (%d) excedem a capacidade do local (%d)",
                             vagasTotais, local.getCapacidade())
            );
        }
    }

    /**
     * Valida se há conflito de horário para o instrutor.
     *
     * @param aulaIdExcluir ID da aula a ser excluída da verificação (para edição)
     */
    private void validarConflitoInstrutor(Instrutor instrutor, LocalDateTime inicio,
                                         LocalDateTime fim, Long aulaIdExcluir) {
        List<Aula> aulasInstrutor = aulaRepository.findByInstrutor(instrutor);

        for (Aula aula : aulasInstrutor) {
            // Pular a própria aula em caso de edição
            if (aulaIdExcluir != null && aula.getId().equals(aulaIdExcluir)) {
                continue;
            }

            // Ignorar aulas canceladas
            if ("CANCELADA".equals(aula.getStatus())) {
                continue;
            }

            // Verificar sobreposição de horários
            if (verificarSobreposicaoHorarios(inicio, fim, aula.getDataHoraInicio(), aula.getDataHoraFim())) {
                throw new ConflictException(
                    String.format("Instrutor %s já possui aula agendada entre %s e %s",
                                 instrutor.getNome(),
                                 aula.getDataHoraInicio(),
                                 aula.getDataHoraFim())
                );
            }
        }
    }

    /**
     * Valida se há conflito de horário para o local.
     *
     * @param aulaIdExcluir ID da aula a ser excluída da verificação (para edição)
     */
    private void validarConflitoLocal(Local local, LocalDateTime inicio,
                                     LocalDateTime fim, Long aulaIdExcluir) {
        List<Aula> aulasLocal = aulaRepository.findByLocal(local);

        for (Aula aula : aulasLocal) {
            // Pular a própria aula em caso de edição
            if (aulaIdExcluir != null && aula.getId().equals(aulaIdExcluir)) {
                continue;
            }

            // Ignorar aulas canceladas
            if ("CANCELADA".equals(aula.getStatus())) {
                continue;
            }

            // Verificar sobreposição de horários
            if (verificarSobreposicaoHorarios(inicio, fim, aula.getDataHoraInicio(), aula.getDataHoraFim())) {
                throw new ConflictException(
                    String.format("Local %s já está ocupado entre %s e %s",
                                 local.getNome(),
                                 aula.getDataHoraInicio(),
                                 aula.getDataHoraFim())
                );
            }
        }
    }

    /**
     * Verifica se há sobreposição entre dois intervalos de tempo.
     */
    private boolean verificarSobreposicaoHorarios(LocalDateTime inicio1, LocalDateTime fim1,
                                                  LocalDateTime inicio2, LocalDateTime fim2) {
        return inicio1.isBefore(fim2) && fim1.isAfter(inicio2);
    }

    /**
     * Valida se a aula pode ser editada.
     */
    private void validarAulaEditavel(Aula aula) {
        if ("CANCELADA".equals(aula.getStatus())) {
            throw new BusinessException("Não é possível editar uma aula cancelada");
        }

        // Não permitir edição de aulas já concluídas
        if (aula.getDataHoraFim().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível editar uma aula que já foi concluída");
        }
    }

    // ==================== MÉTODOS DE NOTIFICAÇÃO ====================

    /**
     * Notifica alunos sobre alteração em aula.
     * TODO: Implementar sistema de notificações (email, SMS, etc.)
     */
    private void notificarAlteracaoAula(Aula aula) {
        // Implementação futura: enviar notificações para alunos inscritos
        System.out.println("NOTIFICAÇÃO: Aula " + aula.getId() + " foi alterada");
    }

    /**
     * Notifica alunos sobre cancelamento de aula.
     * TODO: Implementar sistema de notificações (email, SMS, etc.)
     */
    private void notificarCancelamentoAula(Aula aula, String motivo) {
        // Implementação futura: enviar notificações para alunos inscritos
        System.out.println("NOTIFICAÇÃO: Aula " + aula.getId() + " foi cancelada. Motivo: " + motivo);
    }
}