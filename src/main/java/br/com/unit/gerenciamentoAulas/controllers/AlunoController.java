package br.com.unit.gerenciamentoAulas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.repositories.AlunoRepository;

@RestController
@RequestMapping("/api/alunos")
@CrossOrigin(origins = "*")
public class AlunoController {

    @Autowired
    private AlunoRepository alunoRepository;

    @GetMapping
    public ResponseEntity<List<Aluno>> listarTodos() {
        List<Aluno> alunos = alunoRepository.findAll();
        return ResponseEntity.ok(alunos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aluno> buscarPorId(@PathVariable Long id) {
        return alunoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Aluno aluno) {
        try {
            if (aluno.getNome() == null || aluno.getNome().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nome é obrigatório");
            }
            if (aluno.getEmail() == null || aluno.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email é obrigatório");
            }
            if (aluno.getCpf() == null || aluno.getCpf().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("CPF é obrigatório");
            }
            if (aluno.getMatricula() == null || aluno.getMatricula().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Matrícula é obrigatória");
            }

            if (alunoRepository.existsByMatricula(aluno.getMatricula())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Matrícula já cadastrada");
            }

            Aluno alunoSalvo = alunoRepository.save(aluno);
            return ResponseEntity.status(HttpStatus.CREATED).body(alunoSalvo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar aluno: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Aluno alunoAtualizado) {
        return alunoRepository.findById(id)
                .map(aluno -> {
                    if (alunoAtualizado.getNome() != null) {
                        aluno.setNome(alunoAtualizado.getNome());
                    }
                    if (alunoAtualizado.getEmail() != null) {
                        aluno.setEmail(alunoAtualizado.getEmail());
                    }
                    if (alunoAtualizado.getTelefone() != null) {
                        aluno.setTelefone(alunoAtualizado.getTelefone());
                    }
                    if (alunoAtualizado.getCurso() != null) {
                        aluno.setCurso(alunoAtualizado.getCurso());
                    }
                    
                    Aluno alunoSalvo = alunoRepository.save(aluno);
                    return ResponseEntity.ok(alunoSalvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!alunoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        alunoRepository.deleteById(id);
        return ResponseEntity.ok("Aluno deletado com sucesso");
    }

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<Aluno> buscarPorMatricula(@PathVariable String matricula) {
        return alunoRepository.findByMatricula(matricula)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Aluno> buscarPorEmail(@PathVariable String email) {
        return alunoRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/curso/{curso}")
    public ResponseEntity<List<Aluno>> buscarPorCurso(@PathVariable String curso) {
        List<Aluno> alunos = alunoRepository.findByCurso(curso);
        return ResponseEntity.ok(alunos);
    }
}