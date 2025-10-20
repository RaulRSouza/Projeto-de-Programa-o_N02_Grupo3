package br.com.unit.gerenciamentoAulas.servicos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.entidades.Aula;

/**
 * Serviço que centraliza as regras de negócio relacionadas às inscrições
 * de alunos nas aulas presenciais. Trabalha em conjunto com {@link GestaoAula}
 * para garantir consistência de vagas, horários e status de inscrição.
 */
public class Inscricao {
    private final GestaoAula gestaoAula;
    private final Map<Long, br.com.unit.gerenciamentoAulas.entidades.Inscricao> inscricoesPorId;
    private final AtomicLong sequenciaInscricao;

    public Inscricao(GestaoAula gestaoAula) {
        this.gestaoAula = gestaoAula;
        this.inscricoesPorId = new HashMap<>();
        this.sequenciaInscricao = new AtomicLong(1L);
    }

    /**
     * Fluxo principal do modo Aluno: realiza a inscrição em uma aula,
     * respeitando limite de vagas, conflitos de horário e status da aula.
     *
     * @param aulaId id da aula desejada
     * @param aluno aluno que está solicitando a vaga
     * @param observacoes anotações opcionais fornecidas pelo aluno
     * @return inscrição criada quando válida
     */
    public Optional<br.com.unit.gerenciamentoAulas.entidades.Inscricao> inscreverAluno(Long aulaId,
                                                                                       Aluno aluno,
                                                                                       String observacoes) {
        if (aulaId == null || aluno == null) {
            System.out.println("Dados obrigatórios ausentes para inscrição.");
            return Optional.empty();
        }

        Optional<Aula> aulaOpt = gestaoAula.buscarAulaPorId(aulaId);
        if (!aulaOpt.isPresent()) {
            System.out.println("Aula informada não foi encontrada.");
            return Optional.empty();
        }

        Aula aula = aulaOpt.get();
        if (!"AGENDADA".equalsIgnoreCase(aula.getStatus())) {
            System.out.println("Inscrição não permitida. Aula está com status " + aula.getStatus());
            return Optional.empty();
        }

        if (!aula.temVagasDisponiveis()) {
            System.out.println("Inscrição negada. Não há vagas disponíveis.");
            return Optional.empty();
        }

        if (alunoJaInscritoNaAula(aluno, aula)) {
            System.out.println("Aluno já possui inscrição ativa nesta aula.");
            return Optional.empty();
        }

        if (temConflitoHorario(aluno, aula)) {
            System.out.println("Inscrição negada. Há conflito de horário com outra aula confirmada.");
            return Optional.empty();
        }

        br.com.unit.gerenciamentoAulas.entidades.Inscricao novaInscricao =
                new br.com.unit.gerenciamentoAulas.entidades.Inscricao(
                        sequenciaInscricao.getAndIncrement(),
                        aluno,
                        aula
                );

        novaInscricao.setObservacoes(observacoes);

        aula.adicionarInscricao(novaInscricao);
        aluno.getInscricoes().add(novaInscricao);
        inscricoesPorId.put(novaInscricao.getId(), novaInscricao);

        System.out.println("Inscrição confirmada para a aula " + aula.getId()
                + " - Aluno: " + aluno.getNome());
        return Optional.of(novaInscricao);
    }

    /**
     * Fluxo complementar do modo Aluno: cancelamento de inscrição
     * com reaproveitamento da vaga.
     *
     * @param aulaId id da aula
     * @param alunoId id do aluno
     * @param motivo justificativa informada
     * @return verdadeiro quando o cancelamento é efetuado
     */
    public boolean cancelarInscricao(Long aulaId, Long alunoId, String motivo) {
        Optional<br.com.unit.gerenciamentoAulas.entidades.Inscricao> inscricaoOpt =
                localizarInscricao(aulaId, alunoId);

        if (!inscricaoOpt.isPresent()) {
            System.out.println("Nenhuma inscrição ativa encontrada para cancelamento.");
            return false;
        }

        br.com.unit.gerenciamentoAulas.entidades.Inscricao inscricao = inscricaoOpt.get();
        if ("CANCELADA".equalsIgnoreCase(inscricao.getStatus())) {
            System.out.println("Inscrição já se encontra cancelada.");
            return false;
        }

        Aula aula = inscricao.getAula();
        aula.setVagasDisponiveis(aula.getVagasDisponiveis() + 1);

        inscricao.setStatus("CANCELADA");
        inscricao.setObservacoes(
                (inscricao.getObservacoes() != null ? inscricao.getObservacoes() + " | " : "")
                        + "Cancelada em " + LocalDateTime.now() + " Motivo: " + motivo
        );

        System.out.println("Inscrição cancelada com sucesso para a aula " + aulaId);
        return true;
    }

    /**
     * Fluxo do modo Instrutor/Admin: confirmação de presença manual dos inscritos.
     *
     * @param aulaId id da aula
     * @param alunoId id do aluno cujo comparecimento foi confirmado
     * @param dataConfirmacao horário da confirmação
     * @return verdadeiro quando o status é alterado para PRESENTE
     */
    public boolean confirmarPresenca(Long aulaId, Long alunoId, LocalDateTime dataConfirmacao) {
        Optional<br.com.unit.gerenciamentoAulas.entidades.Inscricao> inscricaoOpt =
                localizarInscricao(aulaId, alunoId);

        if (!inscricaoOpt.isPresent()) {
            System.out.println("Inscrição não encontrada para confirmação de presença.");
            return false;
        }

        br.com.unit.gerenciamentoAulas.entidades.Inscricao inscricao = inscricaoOpt.get();
        if (!"CONFIRMADA".equalsIgnoreCase(inscricao.getStatus())) {
            System.out.println("Presença só pode ser confirmada para inscrições em status CONFIRMADA.");
            return false;
        }

        inscricao.setStatus("PRESENTE");
        inscricao.setObservacoes(
                (inscricao.getObservacoes() != null ? inscricao.getObservacoes() + " | " : "")
                        + "Presença confirmada em " + (dataConfirmacao != null ? dataConfirmacao : LocalDateTime.now())
        );

        System.out.println("Presença confirmada para aluno " + alunoId + " na aula " + aulaId);
        return true;
    }

    /**
     * Recupera as inscrições ativas (CONFIRMADA ou PRESENTE) de um aluno.
     */
    public List<br.com.unit.gerenciamentoAulas.entidades.Inscricao> listarInscricoesAtivas(Aluno aluno) {
        if (aluno == null) {
            return Collections.emptyList();
        }

        return aluno.getInscricoes().stream()
                .filter(inscricao -> !"CANCELADA".equalsIgnoreCase(inscricao.getStatus()))
                .sorted(Comparator.comparing(inscricao -> inscricao.getAula().getDataHoraInicio()))
                .collect(Collectors.toList());
    }

    /**
     * Lista as inscrições do dia para um aluno – utilizado no fluxo rápido da agenda.
     */
    public List<br.com.unit.gerenciamentoAulas.entidades.Inscricao> listarInscricoesDoDia(Aluno aluno, LocalDate data) {
        if (aluno == null || data == null) {
            return Collections.emptyList();
        }

        return listarInscricoesAtivas(aluno).stream()
                .filter(inscricao -> inscricao.getAula().getDataHoraInicio().toLocalDate().equals(data))
                .collect(Collectors.toList());
    }

    /**
     * Fornece um snapshot consolidado por status das inscrições de uma aula.
     */
    public Map<String, Long> consolidarPorStatus(Long aulaId) {
        Optional<Aula> aulaOpt = gestaoAula.buscarAulaPorId(aulaId);
        if (!aulaOpt.isPresent()) {
            return Collections.emptyMap();
        }

        return aulaOpt.get().getInscricoes().stream()
                .collect(Collectors.groupingBy(
                        br.com.unit.gerenciamentoAulas.entidades.Inscricao::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * Sugere aulas com vagas disponíveis para o aluno considerando evitar conflitos.
     */
    public List<Aula> sugerirAulasParaAluno(Aluno aluno) {
        if (aluno == null) {
            return Collections.emptyList();
        }

        List<Aula> disponiveis = gestaoAula.listarAulasDisponiveis();
        if (disponiveis.isEmpty()) {
            return Collections.emptyList();
        }

        return disponiveis.stream()
                .filter(aula -> !temConflitoHorario(aluno, aula))
                .sorted(Comparator.comparing(Aula::getDataHoraInicio))
                .collect(Collectors.toList());
    }

    public Optional<br.com.unit.gerenciamentoAulas.entidades.Inscricao> buscarPorId(Long inscricaoId) {
        return Optional.ofNullable(inscricoesPorId.get(inscricaoId));
    }

    public List<br.com.unit.gerenciamentoAulas.entidades.Inscricao> listarTodas() {
        return new ArrayList<>(inscricoesPorId.values());
    }

    private Optional<br.com.unit.gerenciamentoAulas.entidades.Inscricao> localizarInscricao(Long aulaId, Long alunoId) {
        return inscricoesPorId.values().stream()
                .filter(inscricao -> inscricao.getAula() != null
                        && inscricao.getAula().getId().equals(aulaId)
                        && inscricao.getAluno() != null
                        && inscricao.getAluno().getId() != null
                        && inscricao.getAluno().getId().equals(alunoId)
                        && !"CANCELADA".equalsIgnoreCase(inscricao.getStatus()))
                .findFirst();
    }

    private boolean alunoJaInscritoNaAula(Aluno aluno, Aula aula) {
        return aula.getInscricoes().stream()
                .anyMatch(inscricao -> inscricao.getAluno() != null
                        && inscricao.getAluno().equals(aluno)
                        && !"CANCELADA".equalsIgnoreCase(inscricao.getStatus()));
    }

    private boolean temConflitoHorario(Aluno aluno, Aula novaAula) {
        return aluno.getInscricoes().stream()
                .filter(inscricao -> !"CANCELADA".equalsIgnoreCase(inscricao.getStatus()))
                .map(br.com.unit.gerenciamentoAulas.entidades.Inscricao::getAula)
                .filter(aulaExistente -> aulaExistente != null && !"CANCELADA".equalsIgnoreCase(aulaExistente.getStatus()))
                .anyMatch(aulaExistente -> {
                    LocalDateTime inicioExistente = aulaExistente.getDataHoraInicio();
                    LocalDateTime fimExistente = aulaExistente.getDataHoraFim();
                    LocalDateTime inicioNova = novaAula.getDataHoraInicio();
                    LocalDateTime fimNova = novaAula.getDataHoraFim();
                    return inicioNova.isBefore(fimExistente) && fimNova.isAfter(inicioExistente);
                });
    }
}
