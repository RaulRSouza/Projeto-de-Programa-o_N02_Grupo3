package br.com.unit.gerenciamentoAulas.servicos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Local;

public class GestaoAula {
    private Map<Long, Aula> aulas;
    private ValidacaoConflitos validacaoConflitos;
    private Long proximoId;

    public GestaoAula() {
        this.aulas = new HashMap<>();
        this.validacaoConflitos = new ValidacaoConflitos(new ArrayList<>(aulas.values()));
        this.proximoId = 1L;
    }

    public Optional<Aula> criarAula(Curso curso, Instrutor instrutor, Local local,
                                    LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                                    int vagasTotais, String observacoes) {
        
        if (curso == null || instrutor == null || local == null) {
            System.out.println("Curso, instrutor e local são obrigatórios");
            return Optional.empty();
        }

        Aula novaAula = new Aula(proximoId, curso, instrutor, local, 
                                dataHoraInicio, dataHoraFim, vagasTotais);
        novaAula.setObservacoes(observacoes);

        if (!validacaoConflitos.validarAula(novaAula)) {
            return Optional.empty();
        }

        aulas.put(proximoId, novaAula);
        validacaoConflitos.adicionarAulaAgendada(novaAula);
        
        curso.adicionarAula(novaAula);
        instrutor.adicionarAula(novaAula);

        System.out.println("Aula criada com sucesso - ID: " + proximoId);
        proximoId++;
        
        return Optional.of(novaAula);
    }

    public boolean editarAula(Long aulaId, Curso curso, Instrutor instrutor, Local local,
                             LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                             int vagasTotais, String observacoes) {
        
        Aula aulaExistente = aulas.get(aulaId);
        
        if (aulaExistente == null) {
            System.out.println("Aula não encontrada");
            return false;
        }

        if ("CANCELADA".equals(aulaExistente.getStatus())) {
            System.out.println("Não é possível editar uma aula cancelada");
            return false;
        }

        Aula aulaAtualizada = new Aula(aulaId, curso, instrutor, local,
                                       dataHoraInicio, dataHoraFim, vagasTotais);
        aulaAtualizada.setObservacoes(observacoes);
        aulaAtualizada.setStatus(aulaExistente.getStatus());
        aulaAtualizada.setInscricoes(aulaExistente.getInscricoes());
        aulaAtualizada.setVagasDisponiveis(vagasTotais - aulaExistente.getInscricoes().size());

        if (vagasTotais < aulaExistente.getInscricoes().size()) {
            System.out.println("Número de vagas insuficiente. Há " + 
                             aulaExistente.getInscricoes().size() + " alunos inscritos");
            return false;
        }

        if (!validacaoConflitos.validarAula(aulaAtualizada)) {
            return false;
        }

        if (aulaExistente.getCurso() != null) {
            aulaExistente.getCurso().removerAula(aulaExistente);
        }
        if (aulaExistente.getInstrutor() != null) {
            aulaExistente.getInstrutor().removerAula(aulaExistente);
        }

        aulas.put(aulaId, aulaAtualizada);
        validacaoConflitos.setAulasAgendadas(new ArrayList<>(aulas.values()));

        curso.adicionarAula(aulaAtualizada);
        instrutor.adicionarAula(aulaAtualizada);

        System.out.println("Aula editada com sucesso");
        return true;
    }

    public boolean cancelarAula(Long aulaId, String motivo) {
        Aula aula = aulas.get(aulaId);
        
        if (aula == null) {
            System.out.println("Aula não encontrada");
            return false;
        }

        if ("CANCELADA".equals(aula.getStatus())) {
            System.out.println("Aula já está cancelada");
            return false;
        }

        aula.setStatus("CANCELADA");
        aula.setObservacoes(aula.getObservacoes() + " | Motivo do cancelamento: " + motivo);

        aula.getInscricoes().forEach(inscricao -> {
            inscricao.setStatus("CANCELADA");
            inscricao.setObservacoes("Aula cancelada: " + motivo);
        });

        System.out.println("Aula cancelada com sucesso");
        return true;
    }

    public Optional<Aula> buscarAulaPorId(Long aulaId) {
        return Optional.ofNullable(aulas.get(aulaId));
    }

    public List<Aula> listarTodasAulas() {
        return new ArrayList<>(aulas.values());
    }

    public List<Aula> listarAulasPorCurso(Curso curso) {
        return aulas.values().stream()
                .filter(aula -> aula.getCurso().equals(curso))
                .collect(Collectors.toList());
    }

    public List<Aula> listarAulasPorInstrutor(Instrutor instrutor) {
        return aulas.values().stream()
                .filter(aula -> aula.getInstrutor().equals(instrutor))
                .collect(Collectors.toList());
    }

    public List<Aula> listarAulasPorLocal(Local local) {
        return aulas.values().stream()
                .filter(aula -> aula.getLocal().equals(local))
                .collect(Collectors.toList());
    }

    public List<Aula> listarAulasPorStatus(String status) {
        return aulas.values().stream()
                .filter(aula -> status.equals(aula.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Aula> listarAulasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return aulas.values().stream()
                .filter(aula -> !aula.getDataHoraInicio().isBefore(inicio) && 
                              !aula.getDataHoraFim().isAfter(fim))
                .collect(Collectors.toList());
    }

    public List<Aula> listarAulasDisponiveis() {
        return aulas.values().stream()
                .filter(aula -> "AGENDADA".equals(aula.getStatus()) && 
                              aula.temVagasDisponiveis() &&
                              aula.getDataHoraInicio().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    public List<Aula> listarAulasFuturas() {
        return aulas.values().stream()
                .filter(aula -> aula.getDataHoraInicio().isAfter(LocalDateTime.now()) &&
                              !"CANCELADA".equals(aula.getStatus()))
                .sorted((a1, a2) -> a1.getDataHoraInicio().compareTo(a2.getDataHoraInicio()))
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getEstatisticas() {
        Map<String, Integer> stats = new HashMap<>();
        
        stats.put("total", aulas.size());
        stats.put("agendadas", (int) aulas.values().stream()
                .filter(a -> "AGENDADA".equals(a.getStatus())).count());
        stats.put("canceladas", (int) aulas.values().stream()
                .filter(a -> "CANCELADA".equals(a.getStatus())).count());
        stats.put("comVagas", (int) aulas.values().stream()
                .filter(Aula::temVagasDisponiveis).count());
        stats.put("lotadas", (int) aulas.values().stream()
                .filter(a -> !a.temVagasDisponiveis() && !"CANCELADA".equals(a.getStatus())).count());
        
        return stats;
    }
}