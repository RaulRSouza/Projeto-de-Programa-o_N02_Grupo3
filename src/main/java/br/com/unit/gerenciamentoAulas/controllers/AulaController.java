package br.com.unit.gerenciamentoAulas.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Local;
import br.com.unit.gerenciamentoAulas.repositories.AulaRepository;
import br.com.unit.gerenciamentoAulas.repositories.CursoRepository;
import br.com.unit.gerenciamentoAulas.repositories.InstrutorRepository;
import br.com.unit.gerenciamentoAulas.repositories.LocalRepository;

@RestController
@RequestMapping("/api/aulas")
@CrossOrigin(origins = "*")
public class AulaController {

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InstrutorRepository instrutorRepository;

    @Autowired
    private LocalRepository localRepository;

    @GetMapping
    public ResponseEntity<List<Aula>> listarTodas() {
        List<Aula> aulas = aulaRepository.findAll();
        return ResponseEntity.ok(aulas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aula> buscarPorId(@PathVariable Long id) {
        return aulaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Aula aula) {
        try {
            if (aula.getCurso() == null || aula.getCurso().getId() == null) {
                return ResponseEntity.badRequest().body("Curso é obrigatório");
            }
            if (aula.getInstrutor() == null || aula.getInstrutor().getId() == null) {
                return ResponseEntity.badRequest().body("Instrutor é obrigatório");
            }
            if (aula.getLocal() == null || aula.getLocal().getId() == null) {
                return ResponseEntity.badRequest().body("Local é obrigatório");
            }

            Curso curso = cursoRepository.findById(aula.getCurso().getId())
                    .orElseThrow(() -> new RuntimeException("Curso não encontrado"));
            Instrutor instrutor = instrutorRepository.findById(aula.getInstrutor().getId())
                    .orElseThrow(() -> new RuntimeException("Instrutor não encontrado"));
            Local local = localRepository.findById(aula.getLocal().getId())
                    .orElseThrow(() -> new RuntimeException("Local não encontrado"));

            boolean conflitoInstrutor = aulaRepository.existsConflitoInstrutor(
                    instrutor, aula.getDataHoraInicio(), aula.getDataHoraFim());
            if (conflitoInstrutor) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Instrutor já possui aula neste horário");
            }

            boolean conflitoLocal = aulaRepository.existsConflitoLocal(
                    local, aula.getDataHoraInicio(), aula.getDataHoraFim());
            if (conflitoLocal) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Local já está ocupado neste horário");
            }

            if (aula.getVagasTotais() > local.getCapacidade()) {
                return ResponseEntity.badRequest()
                        .body("Número de vagas excede a capacidade do local");
            }

            aula.setCurso(curso);
            aula.setInstrutor(instrutor);
            aula.setLocal(local);
            aula.setVagasDisponiveis(aula.getVagasTotais());

            Aula aulaSalva = aulaRepository.save(aula);
            return ResponseEntity.status(HttpStatus.CREATED).body(aulaSalva);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar aula: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Aula aulaAtualizada) {
        return aulaRepository.findById(id)
                .map(aula -> {
                    if (aulaAtualizada.getDataHoraInicio() != null) {
                        aula.setDataHoraInicio(aulaAtualizada.getDataHoraInicio());
                    }
                    if (aulaAtualizada.getDataHoraFim() != null) {
                        aula.setDataHoraFim(aulaAtualizada.getDataHoraFim());
                    }
                    if (aulaAtualizada.getStatus() != null) {
                        aula.setStatus(aulaAtualizada.getStatus());
                    }
                    if (aulaAtualizada.getObservacoes() != null) {
                        aula.setObservacoes(aulaAtualizada.getObservacoes());
                    }
                    
                    Aula aulaSalva = aulaRepository.save(aula);
                    return ResponseEntity.ok(aulaSalva);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        return aulaRepository.findById(id)
                .map(aula -> {
                    aula.setStatus("CANCELADA");
                    aulaRepository.save(aula);
                    return ResponseEntity.ok("Aula cancelada com sucesso");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!aulaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        aulaRepository.deleteById(id);
        return ResponseEntity.ok("Aula deletada com sucesso");
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Aula>> buscarPorStatus(@PathVariable String status) {
        List<Aula> aulas = aulaRepository.findByStatus(status);
        return ResponseEntity.ok(aulas);
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<Aula>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<Aula> aulas = aulaRepository.findAulasPorPeriodo(inicio, fim);
        return ResponseEntity.ok(aulas);
    }

    @GetMapping("/futuras")
    public ResponseEntity<List<Aula>> buscarFuturas() {
        List<Aula> aulas = aulaRepository.findAulasFuturas(LocalDateTime.now());
        return ResponseEntity.ok(aulas);
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Aula>> buscarComVagasDisponiveis() {
        List<Aula> aulas = aulaRepository.findAulasComVagasDisponiveis(LocalDateTime.now());
        return ResponseEntity.ok(aulas);
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Aula>> buscarPorCurso(@PathVariable Long cursoId) {
        return cursoRepository.findById(cursoId)
                .map(curso -> {
                    List<Aula> aulas = aulaRepository.findByCurso(curso);
                    return ResponseEntity.ok(aulas);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/instrutor/{instrutorId}")
    public ResponseEntity<List<Aula>> buscarPorInstrutor(@PathVariable Long instrutorId) {
        return instrutorRepository.findById(instrutorId)
                .map(instrutor -> {
                    List<Aula> aulas = aulaRepository.findByInstrutor(instrutor);
                    return ResponseEntity.ok(aulas);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}