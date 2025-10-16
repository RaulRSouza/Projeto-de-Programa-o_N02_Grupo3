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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.repositories.InstrutorRepository;

@RestController
@RequestMapping("/api/instrutores")
@CrossOrigin(origins = "*")
public class InstrutorController {

    @Autowired
    private InstrutorRepository instrutorRepository;

    @GetMapping
    public ResponseEntity<List<Instrutor>> listarTodos() {
        List<Instrutor> instrutores = instrutorRepository.findAll();
        return ResponseEntity.ok(instrutores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instrutor> buscarPorId(@PathVariable Long id) {
        return instrutorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Instrutor instrutor) {
        try {
            if (instrutor.getNome() == null || instrutor.getNome().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nome é obrigatório");
            }
            if (instrutor.getEmail() == null || instrutor.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email é obrigatório");
            }
            if (instrutor.getRegistro() == null || instrutor.getRegistro().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Registro é obrigatório");
            }

            if (instrutorRepository.existsByRegistro(instrutor.getRegistro())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Registro já cadastrado");
            }

            Instrutor instrutorSalvo = instrutorRepository.save(instrutor);
            return ResponseEntity.status(HttpStatus.CREATED).body(instrutorSalvo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar instrutor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Instrutor instrutorAtualizado) {
        return instrutorRepository.findById(id)
                .map(instrutor -> {
                    if (instrutorAtualizado.getNome() != null) {
                        instrutor.setNome(instrutorAtualizado.getNome());
                    }
                    if (instrutorAtualizado.getEmail() != null) {
                        instrutor.setEmail(instrutorAtualizado.getEmail());
                    }
                    if (instrutorAtualizado.getTelefone() != null) {
                        instrutor.setTelefone(instrutorAtualizado.getTelefone());
                    }
                    if (instrutorAtualizado.getEspecialidade() != null) {
                        instrutor.setEspecialidade(instrutorAtualizado.getEspecialidade());
                    }
                    
                    Instrutor instrutorSalvo = instrutorRepository.save(instrutor);
                    return ResponseEntity.ok(instrutorSalvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!instrutorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        instrutorRepository.deleteById(id);
        return ResponseEntity.ok("Instrutor deletado com sucesso");
    }

    @GetMapping("/registro/{registro}")
    public ResponseEntity<Instrutor> buscarPorRegistro(@PathVariable String registro) {
        return instrutorRepository.findByRegistro(registro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/especialidade/{especialidade}")
    public ResponseEntity<List<Instrutor>> buscarPorEspecialidade(@PathVariable String especialidade) {
        List<Instrutor> instrutores = instrutorRepository.findByEspecialidade(especialidade);
        return ResponseEntity.ok(instrutores);
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Instrutor>> buscarDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<Instrutor> instrutores = instrutorRepository.findInstrutoresDisponiveis(inicio, fim);
        return ResponseEntity.ok(instrutores);
    }
}