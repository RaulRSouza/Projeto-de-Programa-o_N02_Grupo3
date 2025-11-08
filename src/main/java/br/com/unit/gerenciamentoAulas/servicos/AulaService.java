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
     * @param cursoId
     * @param instrutorId
     * @param localId
     * @param dataHoraInicio 
     * @param dataHoraFim
     * @param vagasTotais 
     * @param observacoes 
     * @param titulo
     * @param descricao
     * @return
     * @throws BusinessException
     * @throws ConflictException 
     */
    public Aula criarAula(Long cursoId, Long instrutorId, Long localId,
                          LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                          int vagasTotais, String observacoes,
                          String titulo, String descricao) {

        validarDatasAula(dataHoraInicio, dataHoraFim);
        validarVagasTotais(vagasTotais);

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("Curso não encontrado com ID: " + cursoId));

        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new BusinessException("Instrutor não encontrado com ID: " + instrutorId));

        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new BusinessException("Local não encontrado com ID: " + localId));

        validarCursoAtivo(curso);
        validarLocalDisponivel(local);
        validarCapacidadeLocal(vagasTotais, local);

        validarConflitoInstrutor(instrutor, dataHoraInicio, dataHoraFim, null);
        validarConflitoLocal(local, dataHoraInicio, dataHoraFim, null);

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
        novaAula.setTitulo(titulo);
        novaAula.setDescricao(descricao);

        Aula aulaSalva = aulaRepository.save(novaAula);

        return aulaSalva;
    }

    /**
     * @param aulaId
     * @param cursoId 
     * @param instrutorId
     * @param localId
     * @param dataHoraInicio
     * @param dataHoraFim
     * @param vagasTotais
     * @param observacoes
     * @return
     * @throws AulaNotFoundException
     * @throws BusinessException
     * @throws ConflictException
     */
    public Aula editarAula(Long aulaId, Long cursoId, Long instrutorId, Long localId,
                          LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                          int vagasTotais, String observacoes,
                          String titulo, String descricao) {

        Aula aulaExistente = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new AulaNotFoundException("Aula não encontrada com ID: " + aulaId));

        validarAulaEditavel(aulaExistente);

        validarDatasAula(dataHoraInicio, dataHoraFim);
        validarVagasTotais(vagasTotais);

        if (titulo == null || titulo.trim().isEmpty()) {
            throw new BusinessException("Título da aula é obrigatório.");
        }

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("Curso não encontrado com ID: " + cursoId));

        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new BusinessException("Instrutor não encontrado com ID: " + instrutorId));

        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new BusinessException("Local não encontrado com ID: " + localId));

        int inscricoesAtivas = (int) aulaExistente.getInscricoes().stream()
                .filter(i -> "CONFIRMADA".equals(i.getStatus()))
                .count();

        if (vagasTotais < inscricoesAtivas) {
            throw new BusinessException(
                String.format("Não é possível reduzir vagas para %d. Há %d alunos inscritos.",
                             vagasTotais, inscricoesAtivas)
            );
        }

        validarCursoAtivo(curso);
        validarLocalDisponivel(local);
        validarCapacidadeLocal(vagasTotais, local);

        validarConflitoInstrutor(instrutor, dataHoraInicio, dataHoraFim, aulaId);
        validarConflitoLocal(local, dataHoraInicio, dataHoraFim, aulaId);

        aulaExistente.setCurso(curso);
        aulaExistente.setInstrutor(instrutor);
        aulaExistente.setLocal(local);
        aulaExistente.setDataHoraInicio(dataHoraInicio);
        aulaExistente.setDataHoraFim(dataHoraFim);
        aulaExistente.setVagasTotais(vagasTotais);
        aulaExistente.setVagasDisponiveis(vagasTotais - inscricoesAtivas);
        aulaExistente.setObservacoes(observacoes);
        aulaExistente.setTitulo(titulo);
        aulaExistente.setDescricao(descricao);

        Aula aulaAtualizada = aulaRepository.save(aulaExistente);

        notificarAlteracaoAula(aulaAtualizada);

        return aulaAtualizada;
    }

    /**
     * @param aulaId
     * @param motivo
     * @return
     * @throws AulaNotFoundException
     * @throws BusinessException
     */
    public Aula cancelarAula(Long aulaId, String motivo) {
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new AulaNotFoundException("Aula não encontrada com ID: " + aulaId));

        if ("CANCELADA".equals(aula.getStatus())) {
            throw new BusinessException("Aula já está cancelada");
        }

        aula.setStatus("CANCELADA");

        String observacoesAtualizadas = (aula.getObservacoes() != null ? aula.getObservacoes() + " | " : "")
                                       + "Cancelada em: " + LocalDateTime.now()
                                       + " | Motivo: " + motivo;
        aula.setObservacoes(observacoesAtualizadas);

        for (Inscricao inscricao : aula.getInscricoes()) {
            inscricao.setStatus("CANCELADA");
            inscricao.setObservacoes("Aula cancelada: " + motivo);
        }

        Aula aulaCancelada = aulaRepository.save(aula);

        notificarCancelamentoAula(aulaCancelada, motivo);

        return aulaCancelada;
    }

    /**
     * Remove definitivamente uma aula se ela nao estiver em andamento ou concluida.
     */
    public void deletarAula(Long aulaId) {
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new AulaNotFoundException("Aula não encontrada com ID: " + aulaId));

        String status = aula.getStatus() != null ? aula.getStatus().toUpperCase() : "";
        if ("EM_ANDAMENTO".equals(status)) {
            throw new BusinessException("Não é possível deletar uma aula que está em andamento.");
        }
        if ("CONCLUIDA".equals(status)) {
            throw new BusinessException("Não é possível deletar uma aula já concluída.");
        }

        aulaRepository.delete(aula);
    }

    /**
     * @param aulaId
     * @return 
     * @throws AulaNotFoundException
     */
    @Transactional(readOnly = true)
    public Aula buscarPorId(Long aulaId) {
        return aulaRepository.findById(aulaId)
                .orElseThrow(() -> new AulaNotFoundException("Aula não encontrada com ID: " + aulaId));
    }

    /**
     * @return
     */
    @Transactional(readOnly = true)
    public List<Aula> listarTodas() {
        return aulaRepository.findAll();
    }

    /**     
     * @param cursoId 
     * @return
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorCurso(Long cursoId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("Curso não encontrado com ID: " + cursoId));
        return aulaRepository.findByCurso(curso);
    }

    /**
     * @param instrutorId 
     * @return 
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorInstrutor(Long instrutorId) {
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new BusinessException("Instrutor não encontrado com ID: " + instrutorId));
        return aulaRepository.findByInstrutor(instrutor);
    }

    /**
     * @param localId
     * @return
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorLocal(Long localId) {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new BusinessException("Local não encontrado com ID: " + localId));
        return aulaRepository.findByLocal(local);
    }

    /**
     * @param status
     * @return 
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorStatus(String status) {
        return aulaRepository.findByStatus(status);
    }

    /**
     * @return
     */
    @Transactional(readOnly = true)
    public List<Aula> listarAulasFuturas() {
        return aulaRepository.findAulasFuturas(LocalDateTime.now());
    }

    /**
     * @return
     */
    @Transactional(readOnly = true)
    public List<Aula> listarAulasDisponiveis() {
        return aulaRepository.findAulasComVagasDisponiveis(LocalDateTime.now());
    }

    /**
     * @param inicio 
     * @param fim 
     * @return
     */
    @Transactional(readOnly = true)
    public List<Aula> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        validarDatasAula(inicio, fim);
        return aulaRepository.findAulasPorPeriodo(inicio, fim);
    }




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


    private void validarVagasTotais(int vagasTotais) {
        if (vagasTotais <= 0) {
            throw new BusinessException("Quantidade de vagas deve ser maior que zero");
        }

        if (vagasTotais > 1000) {
            throw new BusinessException("Quantidade de vagas não pode exceder 1000");
        }
    }

    private void validarCursoAtivo(Curso curso) {
        if (!curso.isAtivo()) {
            throw new BusinessException("Não é possível criar aula para curso inativo: " + curso.getNome());
        }
    }

    private void validarLocalDisponivel(Local local) {
        if (!local.isDisponivel()) {
            throw new BusinessException("Local não está disponível: " + local.getNome());
        }
    }

    private void validarCapacidadeLocal(int vagasTotais, Local local) {
        if (vagasTotais > local.getCapacidade()) {
            throw new BusinessException(
                String.format("Vagas solicitadas (%d) excedem a capacidade do local (%d)",
                             vagasTotais, local.getCapacidade())
            );
        }
    }

    /**
     * @param aulaIdExcluir
     */
    private void validarConflitoInstrutor(Instrutor instrutor, LocalDateTime inicio,
                                         LocalDateTime fim, Long aulaIdExcluir) {
        List<Aula> aulasInstrutor = aulaRepository.findByInstrutor(instrutor);

        for (Aula aula : aulasInstrutor) {
            if (aulaIdExcluir != null && aula.getId().equals(aulaIdExcluir)) {
                continue;
            }

            if ("CANCELADA".equals(aula.getStatus())) {
                continue;
            }
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
     * @param aulaIdExcluir
     */
    private void validarConflitoLocal(Local local, LocalDateTime inicio,
                                     LocalDateTime fim, Long aulaIdExcluir) {
        List<Aula> aulasLocal = aulaRepository.findByLocal(local);

        for (Aula aula : aulasLocal) {
            if (aulaIdExcluir != null && aula.getId().equals(aulaIdExcluir)) {
                continue;
            }

            if ("CANCELADA".equals(aula.getStatus())) {
                continue;
            }

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

    private boolean verificarSobreposicaoHorarios(LocalDateTime inicio1, LocalDateTime fim1,
                                                  LocalDateTime inicio2, LocalDateTime fim2) {
        return inicio1.isBefore(fim2) && fim1.isAfter(inicio2);
    }


    private void validarAulaEditavel(Aula aula) {
        if ("CANCELADA".equals(aula.getStatus())) {
            throw new BusinessException("Não é possível editar uma aula cancelada");
        }

        if (aula.getDataHoraFim().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível editar uma aula que já foi concluída");
        }
    }


    private void notificarAlteracaoAula(Aula aula) {

        System.out.println("NOTIFICAÇÃO: Aula " + aula.getId() + " foi alterada");
    }


    private void notificarCancelamentoAula(Aula aula, String motivo) {
        System.out.println("NOTIFICAÇÃO: Aula " + aula.getId() + " foi cancelada. Motivo: " + motivo);
    }
}
