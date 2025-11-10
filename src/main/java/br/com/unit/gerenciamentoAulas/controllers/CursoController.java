package br.com.unit.gerenciamentoAulas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.repositories.CursoRepository;
import br.com.unit.gerenciamentoAulas.repositories.UsuarioRepository;
import br.com.unit.gerenciamentoAulas.servicos.AuditoriaService;
import br.com.unit.gerenciamentoAulas.entidades.Usuario;
import br.com.unit.gerenciamentoAulas.entidades.AcaoSistema;
import br.com.unit.gerenciamentoAulas.entidades.PerfilUsuario;

@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
public class CursoController {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<Curso>> listarTodos() {
        List<Curso> cursos = cursoRepository.findAll();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Curso> buscarPorId(@PathVariable Long id) {
        return cursoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Curso curso, @RequestParam Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
            
            auditoriaService.validarPermissao(usuario, AcaoSistema.CRIAR_CURSO);
            auditoriaService.registrarAcao(usuario, AcaoSistema.CRIAR_CURSO, 
                    "Criando curso: " + curso.getNome());
            
            if (curso.getNome() == null || curso.getNome().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nome do curso é obrigatório");
            }
            if (curso.getCargaHoraria() <= 0) {
                return ResponseEntity.badRequest().body("Carga horária deve ser maior que zero");
            }

            Curso cursoSalvo = cursoRepository.save(curso);
            return ResponseEntity.status(HttpStatus.CREATED).body(cursoSalvo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar curso: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Curso cursoAtualizado,
                                       @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarPermissao(usuario, AcaoSistema.EDITAR_CURSO);
        auditoriaService.registrarAcao(usuario, AcaoSistema.EDITAR_CURSO, 
                "Editando curso ID: " + id);
        
        return cursoRepository.findById(id)
                .map(curso -> {
                    if (cursoAtualizado.getNome() != null) {
                        curso.setNome(cursoAtualizado.getNome());
                    }
                    if (cursoAtualizado.getDescricao() != null) {
                        curso.setDescricao(cursoAtualizado.getDescricao());
                    }
                    if (cursoAtualizado.getCargaHoraria() > 0) {
                        curso.setCargaHoraria(cursoAtualizado.getCargaHoraria());
                    }
                    if (cursoAtualizado.getCategoria() != null) {
                        curso.setCategoria(cursoAtualizado.getCategoria());
                    }
                    
                    Curso cursoSalvo = cursoRepository.save(curso);
                    return ResponseEntity.ok(cursoSalvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> alterarStatus(@PathVariable Long id, @RequestParam boolean ativo,
                                          @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarPermissao(usuario, AcaoSistema.EDITAR_CURSO);
        auditoriaService.registrarAcao(usuario, AcaoSistema.EDITAR_CURSO, 
                "Alterando status do curso ID: " + id + " para " + (ativo ? "ATIVO" : "INATIVO"));
        
        return cursoRepository.findById(id)
                .map(curso -> {
                    curso.setAtivo(ativo);
                    cursoRepository.save(curso);
                    return ResponseEntity.ok("Status do curso atualizado");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id, @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarPermissao(usuario, AcaoSistema.EXCLUIR_CURSO);
        auditoriaService.registrarAcao(usuario, AcaoSistema.EXCLUIR_CURSO, 
                "Deletando curso ID: " + id);
        
        if (!cursoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cursoRepository.deleteById(id);
        return ResponseEntity.ok("Curso deletado com sucesso");
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Curso>> buscarAtivos() {
        List<Curso> cursos = cursoRepository.findAllAtivos();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Curso>> buscarPorCategoria(@PathVariable String categoria) {
        List<Curso> cursos = cursoRepository.findByCategoria(categoria);
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Curso>> buscarPorNome(@RequestParam String nome) {
        List<Curso> cursos = cursoRepository.findByNomeContainingIgnoreCase(nome);
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<String>> listarCategorias() {
        List<String> categorias = cursoRepository.findAllCategorias();
        return ResponseEntity.ok(categorias);
    }
}