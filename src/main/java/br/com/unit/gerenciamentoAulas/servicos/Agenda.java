package br.com.unit.gerenciamentoAulas.servicos;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;

/**
 * Orquestra os fluxos internos da agenda do sistema contemplando
 * perspectivas de administrador, instrutor e aluno.
 */
public class Agenda {
    private final GestaoAula gestaoAula;
    private final Inscricao servicoInscricao;
    private final Autenticacao autenticacao;

    public Agenda(GestaoAula gestaoAula, Inscricao servicoInscricao, Autenticacao autenticacao) {
        this.gestaoAula = gestaoAula;
        this.servicoInscricao = servicoInscricao;
        this.autenticacao = autenticacao;
    }

    /**
     * Fluxo principal da agenda. Retorna a visão adequada ao perfil logado.
     */
    public List<Aula> listarAgendaPrincipal() {
        if (!autenticacao.isUsuarioLogado()) {
            System.out.println("Nenhum usuário autenticado. Agenda vazia.");
            return Collections.emptyList();
        }

        if (autenticacao.isAdministrador()) {
            return ordenarPorData(gestaoAula.listarTodasAulas());
        }

        if (autenticacao.isInstrutor()) {
            Optional<Instrutor> instrutorOpt = autenticacao.getInstrutorLogado();
            return instrutorOpt.map(gestaoAula::listarAulasPorInstrutor)
                    .map(this::ordenarPorData)
                    .orElse(Collections.emptyList());
        }

        if (autenticacao.isAluno()) {
            Optional<Aluno> alunoOpt = autenticacao.getAlunoLogado();
            return alunoOpt.map(aluno -> servicoInscricao.listarInscricoesAtivas(aluno).stream()
                    .map(br.com.unit.gerenciamentoAulas.entidades.Inscricao::getAula)
                    .filter(aula -> aula != null && !"CANCELADA".equalsIgnoreCase(aula.getStatus()))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), this::ordenarPorData)))
                    .orElse(Collections.emptyList());
        }

        return Collections.emptyList();
    }

    /**
     * Lista aulas do dia para o usuário corrente.
     */
    public List<Aula> listarAgendaDoDia(LocalDate data) {
        if (data == null) {
            return Collections.emptyList();
        }

        return listarAgendaPrincipal().stream()
                .filter(aula -> aula.getDataHoraInicio().toLocalDate().equals(data))
                .collect(Collectors.toList());
    }

    /**
     * Gera uma visão semanal considerando segunda-feira como início.
     */
    public List<Aula> listarAgendaSemanal(LocalDate referencia) {
        if (referencia == null) {
            referencia = LocalDate.now();
        }

        LocalDate inicioSemana = referencia.with(DayOfWeek.MONDAY);
        LocalDate fimSemana = inicioSemana.plusDays(6);

        return listarPorPeriodo(inicioSemana.atStartOfDay(), fimSemana.atTime(LocalTime.MAX));
    }

    /**
     * Fluxo administrativo para visão consolidada da agenda.
     */
    public AgendaResumo gerarResumoAdministrativo() {
        List<Aula> todas = gestaoAula.listarTodasAulas();
        if (todas.isEmpty()) {
            return new AgendaResumo(0, 0, 0, 0, 0);
        }

        int total = todas.size();
        long agendadas = todas.stream()
                .filter(aula -> "AGENDADA".equalsIgnoreCase(aula.getStatus()))
                .count();
        long canceladas = todas.stream()
                .filter(aula -> "CANCELADA".equalsIgnoreCase(aula.getStatus()))
                .count();
        long disponiveis = todas.stream()
                .filter(Aula::temVagasDisponiveis)
                .count();
        double ocupacaoMedia = todas.stream()
                .filter(aula -> aula.getVagasTotais() > 0)
                .mapToDouble(aula -> (double) (aula.getVagasTotais() - aula.getVagasDisponiveis()) / aula.getVagasTotais())
                .average()
                .orElse(0);

        return new AgendaResumo(
                total,
                (int) agendadas,
                (int) canceladas,
                (int) disponiveis,
                Math.round(ocupacaoMedia * 100)
        );
    }

    /**
     * Fluxo de apoio ao modo aluno – aulas disponíveis sem conflito com agenda atual.
     */
    public List<Aula> listarAulasDisponiveisParaAlunoLogado() {
        if (!autenticacao.isAluno()) {
            return Collections.emptyList();
        }

        return autenticacao.getAlunoLogado()
                .map(servicoInscricao::sugerirAulasParaAluno)
                .orElse(Collections.emptyList());
    }

    /**
     * Apoio ao modo instrutor – agenda restrita a um período.
     */
    public List<Aula> listarAgendaInstrutorPeriodo(Long instrutorId, LocalDateTime inicio, LocalDateTime fim) {
        if (instrutorId == null) {
            return Collections.emptyList();
        }

        List<Aula> aulasInstrutor = gestaoAula.listarTodasAulas().stream()
                .filter(aula -> aula.getInstrutor() != null && instrutorId.equals(aula.getInstrutor().getId()))
                .collect(Collectors.toList());

        return aulasInstrutor.stream()
                .filter(aula -> {
                    LocalDateTime inicioAula = aula.getDataHoraInicio();
                    LocalDateTime fimAula = aula.getDataHoraFim();
                    boolean aposInicio = inicio == null || !fimAula.isBefore(inicio);
                    boolean antesFim = fim == null || !inicioAula.isAfter(fim);
                    return aposInicio && antesFim;
                })
                .sorted(Comparator.comparing(Aula::getDataHoraInicio))
                .collect(Collectors.toList());
    }

    /**
     * Apoio ao modo administrador – agrupamento por curso.
     */
    public Map<String, Long> agruparPorCurso() {
        return gestaoAula.listarTodasAulas().stream()
                .collect(Collectors.groupingBy(
                        aula -> aula.getCurso() != null ? aula.getCurso().getNome() : "Curso não informado",
                        Collectors.counting()
                ));
    }

    /**
     * Recupera aulas em um intervalo independente do perfil.
     */
    public List<Aula> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        List<Aula> todas = gestaoAula.listarTodasAulas();

        return todas.stream()
                .filter(aula -> {
                    LocalDateTime inicioAula = aula.getDataHoraInicio();
                    LocalDateTime fimAula = aula.getDataHoraFim();
                    boolean aposInicio = inicio == null || !fimAula.isBefore(inicio);
                    boolean antesFim = fim == null || !inicioAula.isAfter(fim);
                    return aposInicio && antesFim;
                })
                .sorted(Comparator.comparing(Aula::getDataHoraInicio))
                .collect(Collectors.toList());
    }

    private List<Aula> ordenarPorData(List<Aula> aulas) {
        return aulas.stream()
                .sorted(Comparator.comparing(Aula::getDataHoraInicio))
                .collect(Collectors.toList());
    }

    /**
     * Estrutura simplificada de dashboard para camada de apresentação.
     */
    public static class AgendaResumo {
        private final int total;
        private final int agendadas;
        private final int canceladas;
        private final int comVagas;
        private final int ocupacaoPercentualMedio;

        public AgendaResumo(int total, int agendadas, int canceladas, int comVagas, int ocupacaoPercentualMedio) {
            this.total = total;
            this.agendadas = agendadas;
            this.canceladas = canceladas;
            this.comVagas = comVagas;
            this.ocupacaoPercentualMedio = ocupacaoPercentualMedio;
        }

        public int getTotal() {
            return total;
        }

        public int getAgendadas() {
            return agendadas;
        }

        public int getCanceladas() {
            return canceladas;
        }

        public int getComVagas() {
            return comVagas;
        }

        public int getOcupacaoPercentualMedio() {
            return ocupacaoPercentualMedio;
        }
    }
}
