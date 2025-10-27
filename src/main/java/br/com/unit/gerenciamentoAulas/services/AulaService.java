package br.com.unit.gerenciamentoAulas.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.repositories.AulaRepository;

@Service
public class AulaService {

    private final AulaRepository aulaRepository;

    public AulaService(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }

    public List<Aula> listarTodas() {
        return aulaRepository.findAll();
    }

    public List<Aula> listarAulasFuturas() {
        return aulaRepository.findAulasFuturas(LocalDateTime.now());
    }

    public List<Aula> listarAulasDisponiveis() {
        return aulaRepository.findAulasComVagasDisponiveis(LocalDateTime.now());
    }
}
