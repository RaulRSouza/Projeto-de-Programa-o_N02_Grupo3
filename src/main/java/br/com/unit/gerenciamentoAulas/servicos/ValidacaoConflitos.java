package br.com.unit.gerenciamentoAulas.servicos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Local;

public class ValidacaoConflitos {
    private List<Aula> aulasAgendadas;

    public ValidacaoConflitos() {
        this.aulasAgendadas = new ArrayList<>();
    }

    public ValidacaoConflitos(List<Aula> aulasAgendadas) {
        this.aulasAgendadas = aulasAgendadas != null ? aulasAgendadas : new ArrayList<>();
    }

    public boolean validarAula(Aula novaAula) {
        if (novaAula == null) {
            System.out.println("Aula não pode ser nula");
            return false;
        }

        if (!validarHorarios(novaAula)) {
            return false;
        }

        if (temConflitoInstrutor(novaAula)) {
            System.out.println("Conflito: Instrutor já possui aula agendada neste horário");
            return false;
        }

        if (temConflitoLocal(novaAula)) {
            System.out.println("Conflito: Local já está ocupado neste horário");
            return false;
        }

        if (!validarCapacidadeLocal(novaAula)) {
            return false;
        }

        return true;
    }

    private boolean validarHorarios(Aula aula) {
        if (aula.getDataHoraInicio() == null || aula.getDataHoraFim() == null) {
            System.out.println("Data/hora de início e fim são obrigatórias");
            return false;
        }

        if (aula.getDataHoraInicio().isAfter(aula.getDataHoraFim())) {
            System.out.println("Data/hora de início deve ser anterior à data/hora de fim");
            return false;
        }

        if (aula.getDataHoraInicio().isBefore(LocalDateTime.now())) {
            System.out.println("Não é possível agendar aula no passado");
            return false;
        }

        return true;
    }

    public boolean temConflitoInstrutor(Aula novaAula) {
        if (novaAula.getInstrutor() == null) {
            return false;
        }

        Instrutor instrutor = novaAula.getInstrutor();

        for (Aula aulaExistente : aulasAgendadas) {
            if (aulaExistente.getId().equals(novaAula.getId())) {
                continue;
            }

            if (!"CANCELADA".equals(aulaExistente.getStatus()) && 
                aulaExistente.getInstrutor() != null &&
                aulaExistente.getInstrutor().getId().equals(instrutor.getId())) {
                
                if (horariosSobrepostos(novaAula, aulaExistente)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean temConflitoLocal(Aula novaAula) {
        if (novaAula.getLocal() == null) {
            return false;
        }

        Local local = novaAula.getLocal();

        for (Aula aulaExistente : aulasAgendadas) {
            if (aulaExistente.getId().equals(novaAula.getId())) {
                continue;
            }

            if (!"CANCELADA".equals(aulaExistente.getStatus()) &&
                aulaExistente.getLocal() != null &&
                aulaExistente.getLocal().getId().equals(local.getId())) {
                
                if (horariosSobrepostos(novaAula, aulaExistente)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean horariosSobrepostos(Aula aula1, Aula aula2) {
        LocalDateTime inicio1 = aula1.getDataHoraInicio();
        LocalDateTime fim1 = aula1.getDataHoraFim();
        LocalDateTime inicio2 = aula2.getDataHoraInicio();
        LocalDateTime fim2 = aula2.getDataHoraFim();

        return inicio1.isBefore(fim2) && fim1.isAfter(inicio2);
    }

    private boolean validarCapacidadeLocal(Aula aula) {
        if (aula.getLocal() == null) {
            System.out.println("Local é obrigatório");
            return false;
        }

        if (aula.getVagasTotais() > aula.getLocal().getCapacidade()) {
            System.out.println("Número de vagas excede a capacidade do local (" + 
                             aula.getLocal().getCapacidade() + " vagas)");
            return false;
        }

        return true;
    }

    public List<String> listarConflitos(Aula novaAula) {
        List<String> conflitos = new ArrayList<>();

        if (novaAula == null) {
            conflitos.add("Aula não pode ser nula");
            return conflitos;
        }

        if (novaAula.getDataHoraInicio() == null || novaAula.getDataHoraFim() == null) {
            conflitos.add("Data/hora de início e fim são obrigatórias");
        } else if (novaAula.getDataHoraInicio().isAfter(novaAula.getDataHoraFim())) {
            conflitos.add("Data/hora de início deve ser anterior à data/hora de fim");
        } else if (novaAula.getDataHoraInicio().isBefore(LocalDateTime.now())) {
            conflitos.add("Não é possível agendar aula no passado");
        }

        if (temConflitoInstrutor(novaAula)) {
            conflitos.add("Instrutor já possui aula agendada neste horário");
        }

        if (temConflitoLocal(novaAula)) {
            conflitos.add("Local já está ocupado neste horário");
        }

        if (novaAula.getLocal() != null && 
            novaAula.getVagasTotais() > novaAula.getLocal().getCapacidade()) {
            conflitos.add("Número de vagas excede a capacidade do local");
        }

        return conflitos;
    }

    public void adicionarAulaAgendada(Aula aula) {
        if (aula != null && !aulasAgendadas.contains(aula)) {
            aulasAgendadas.add(aula);
        }
    }

    public void removerAulaAgendada(Aula aula) {
        aulasAgendadas.remove(aula);
    }

    public void setAulasAgendadas(List<Aula> aulas) {
        this.aulasAgendadas = aulas != null ? aulas : new ArrayList<>();
    }

    public List<Aula> getAulasAgendadas() {
        return new ArrayList<>(aulasAgendadas);
    }
}