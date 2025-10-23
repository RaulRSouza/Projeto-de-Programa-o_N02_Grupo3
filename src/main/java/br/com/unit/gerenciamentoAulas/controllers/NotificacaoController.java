package br.com.unit.gerenciamentoAulas.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.unit.gerenciamentoAulas.entidades.Notificacao;
import br.com.unit.gerenciamentoAulas.servicos.NotificacaoService;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Notificacao>> listarPorAluno(@PathVariable Long alunoId) {
        List<Notificacao> notificacoes = notificacaoService.listarPorAluno(alunoId);
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/aluno/{alunoId}/nao-lidas")
    public ResponseEntity<Long> contarNaoLidas(@PathVariable Long alunoId) {
        long contador = notificacaoService.contarNaoLidas(alunoId);
        return ResponseEntity.ok(contador);
    }

    @PatchMapping("/{id}/ler")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        boolean atualizada = notificacaoService.marcarComoLida(id);
        return atualizada ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}