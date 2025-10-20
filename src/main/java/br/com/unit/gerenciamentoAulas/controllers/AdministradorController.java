package br.com.unit.gerenciamentoAulas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/administradores")
@CrossOrigin(origins = "*")
public class AdministradorController {

    @Autowired
    private AdministradorRepository administradorRepository;

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
    public ResponseEntity<Administrador> criar(@RequestBody Administrador administrador) {
        return ResponseEntity.ok(administradorRepository.save(administrador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Administrador> atualizar(@PathVariable Long id, @RequestBody Administrador dadosAtualizados) {
        return administradorRepository.findById(id)
                .map(adm -> {
                    adm.setNome(dadosAtualizados.getNome());
                    adm.setEmail(dadosAtualizados.getEmail());
                    adm.setSenha(dadosAtualizados.getSenha());
                    return ResponseEntity.ok(administradorRepository.save(adm));
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
