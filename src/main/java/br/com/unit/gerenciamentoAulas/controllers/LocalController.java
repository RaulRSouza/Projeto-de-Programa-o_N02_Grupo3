package br.com.unit.gerenciamentoAulas.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import br.com.unit.gerenciamentoAulas.entidades.Local;
import br.com.unit.gerenciamentoAulas.repositories.AulaRepository;
import br.com.unit.gerenciamentoAulas.repositories.LocalRepository;

@RestController
@RequestMapping("/api/locais")
@CrossOrigin(origins = "*")
public class LocalController {

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private AulaRepository aulaRepository;

    @GetMapping
    public ResponseEntity<List<Local>> listarTodos() {
        return ResponseEntity.ok(localRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Local> buscarPorId(@PathVariable Long id) {
        return localRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Local> criar(@RequestBody Local local) {
        return ResponseEntity.ok(localRepository.save(local));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Local> atualizar(@PathVariable Long id, @RequestBody Local dadosAtualizados) {
        return localRepository.findById(id)
                .map(local -> {
                    local.setNome(dadosAtualizados.getNome());
                    local.setEndereco(dadosAtualizados.getEndereco());
                    local.setCapacidade(dadosAtualizados.getCapacidade());
                    local.setDisponivel(dadosAtualizados.isDisponivel());
                    local.setTipo(dadosAtualizados.getTipo());
                    return ResponseEntity.ok(localRepository.save(local));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/disponibilidade")
    public ResponseEntity<?> verificarDisponibilidade(@PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        if (inicio == null || fim == null) {
            return ResponseEntity.badRequest().body("Parametros inicio e fim sao obrigatorios");
        }

        if (!fim.isAfter(inicio)) {
            return ResponseEntity.badRequest().body("Parametro fim deve ser posterior ao inicio");
        }

        return localRepository.findById(id)
                .map(local -> {
                    Map<String, Object> resposta = new HashMap<>();
                    resposta.put("localId", local.getId());
                    resposta.put("capacidade", local.getCapacidade());
                    resposta.put("tipo", local.getTipo());

                    boolean disponivel = local.isDisponivel();
                    String motivo = "Disponivel no periodo informado";

                    if (!disponivel) {
                        motivo = "Local marcado como indisponivel";
                    } else if (aulaRepository.existsConflitoLocal(local, inicio, fim)) {
                        disponivel = false;
                        motivo = "Ja existe aula agendada no periodo solicitado";
                    }

                    resposta.put("disponivel", disponivel);
                    resposta.put("motivo", motivo);
                    resposta.put("inicio", inicio);
                    resposta.put("fim", fim);

                    return ResponseEntity.ok(resposta);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        return localRepository.findById(id)
                .map(local -> {
                    localRepository.delete(local);
                    return ResponseEntity.ok("Local removido com sucesso");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
