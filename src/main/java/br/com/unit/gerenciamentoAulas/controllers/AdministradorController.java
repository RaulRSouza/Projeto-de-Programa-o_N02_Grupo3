package br.com.unit.gerenciamentoAulas.controllers;

import java.util.List;
import java.util.Optional;

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

import br.com.unit.gerenciamentoAulas.entidades.Administrador;
import br.com.unit.gerenciamentoAulas.repositories.AdministradorRepository;
import br.com.unit.gerenciamentoAulas.repositories.UsuarioRepository;

@RestController
@RequestMapping("/api/administradores")
@CrossOrigin(origins = "*")
public class AdministradorController {

    @Autowired
    private AdministradorRepository administradorRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<Administrador>> listarTodos() {
        return ResponseEntity.ok(administradorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Administrador> buscarPorId(@PathVariable Long id) {
        return administradorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Administrador administrador) {
        try {
            if (administrador.getNome() == null || administrador.getNome().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nome é obrigatório");
            }
            if (administrador.getEmail() == null || administrador.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email é obrigatório");
            }

            if (administradorRepository.findByEmail(administrador.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("E-mail já cadastrado para outro administrador");
            }
            
            if (administrador.getCpf() != null && !administrador.getCpf().trim().isEmpty()
                    && usuarioRepository.existsByCpf(administrador.getCpf())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("CPF já cadastrado para outro usuário");
            }

            Administrador administradorSalvo = administradorRepository.save(administrador);
            return ResponseEntity.status(HttpStatus.CREATED).body(administradorSalvo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar administrador: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Administrador dadosAtualizados) {
        return administradorRepository.findById(id)
                .map(adm -> {
                    if (dadosAtualizados.getNome() != null) {
                        adm.setNome(dadosAtualizados.getNome());
                    }
                    if (dadosAtualizados.getEmail() != null) {
                        Optional<Administrador> existente = administradorRepository.findByEmail(dadosAtualizados.getEmail());
                        if (existente.isPresent() && !existente.get().getId().equals(adm.getId())) {
                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body("E-mail já cadastrado para outro administrador");
                        }
                        adm.setEmail(dadosAtualizados.getEmail());
                    }
                    if (dadosAtualizados.getSenha() != null) {
                        adm.setSenha(dadosAtualizados.getSenha());
                    }
                    if (dadosAtualizados.getCpf() != null) {
                        if (usuarioRepository.existsByCpf(dadosAtualizados.getCpf()) && 
                                !adm.getCpf().equals(dadosAtualizados.getCpf())) {
                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body("CPF já cadastrado para outro usuário");
                        }
                        adm.setCpf(dadosAtualizados.getCpf());
                    }
                    if (dadosAtualizados.getTelefone() != null) {
                        adm.setTelefone(dadosAtualizados.getTelefone());
                    }
                    if (dadosAtualizados.getSetor() != null) {
                        adm.setSetor(dadosAtualizados.getSetor());
                    }
                    if (dadosAtualizados.getNivelAcesso() != null) {
                        adm.setNivelAcesso(dadosAtualizados.getNivelAcesso());
                    }
                    
                    Administrador administradorSalvo = administradorRepository.save(adm);
                    return ResponseEntity.ok(administradorSalvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        return administradorRepository.findById(id)
                .map(adm -> {
                    administradorRepository.delete(adm);
                    return ResponseEntity.ok("Administrador removido com sucesso");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}