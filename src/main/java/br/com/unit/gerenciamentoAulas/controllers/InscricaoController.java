package br.com.unit.gerenciamentoAulas.controllers;

import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Inscricao;
import br.com.unit.gerenciamentoAulas.repositories.AlunoRepository;
import br.com.unit.gerenciamentoAulas.repositories.AulaRepository;
import br.com.unit.gerenciamentoAulas.repositories.InscricaoRepository;
import br.com.unit.gerenciamentoAulas.repositories.UsuarioRepository;
import br.com.unit.gerenciamentoAulas.servicos.AuditoriaService;
import br.com.unit.gerenciamentoAulas.entidades.Usuario;
import br.com.unit.gerenciamentoAulas.entidades.AcaoSistema;
import br.com.unit.gerenciamentoAulas.entidades.PerfilUsuario;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/inscricoes")
@CrossOrigin(origins = "*")
public class InscricaoController {

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<Inscricao>> listarTodas() {
        List<Inscricao> inscricoes = inscricaoRepository.findAll();
        return ResponseEntity.ok(inscricoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inscricao> buscarPorId(@PathVariable Long id) {
        return inscricaoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Inscricao inscricao, @RequestParam Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
            
            auditoriaService.validarInscricao(usuario);
            
            if (inscricao.getAluno() == null || inscricao.getAluno().getId() == null) {
                return ResponseEntity.badRequest().body("Aluno é obrigatório");
            }
            if (inscricao.getAula() == null || inscricao.getAula().getId() == null) {
                return ResponseEntity.badRequest().body("Aula é obrigatória");
            }

            Aluno aluno = alunoRepository.findById(inscricao.getAluno().getId())
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
            
            if (!usuario.getId().equals(aluno.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Aluno so pode se inscrever em aulas para si mesmo");
            }
            
            auditoriaService.registrarAcao(usuario, AcaoSistema.INSCREVER_SE, 
                    "Inscrevendo-se na aula ID: " + inscricao.getAula().getId());
            Aula aula = aulaRepository.findById(inscricao.getAula().getId())
                    .orElseThrow(() -> new RuntimeException("Aula não encontrada"));

            if (inscricaoRepository.existsByAlunoAndAula(aluno, aula)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Aluno já está inscrito nesta aula");
            }

            if (!aula.temVagasDisponiveis()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não há vagas disponíveis nesta aula");
            }

            boolean conflitoHorario = inscricaoRepository.existsConflitoHorarioAluno(
                    aluno, aula.getDataHoraInicio(), aula.getDataHoraFim());
            if (conflitoHorario) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Aluno já possui inscrição em aula neste horário");
            }

            inscricao.setAluno(aluno);
            inscricao.setAula(aula);
            inscricao.setDataInscricao(LocalDateTime.now());
            inscricao.setStatus("CONFIRMADA");

            Inscricao inscricaoSalva = inscricaoRepository.save(inscricao);

            aula.setVagasDisponiveis(aula.getVagasDisponiveis() - 1);
            aulaRepository.save(aula);

            return ResponseEntity.status(HttpStatus.CREATED).body(inscricaoSalva);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar inscrição: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id, @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        if (usuario.getPerfil() != PerfilUsuario.ALUNO) {
            auditoriaService.registrarAcessoNegado(usuario, AcaoSistema.CANCELAR_INSCRICAO,
                    "Apenas Alunos podem cancelar inscricoes");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas Alunos podem cancelar inscricoes");
        }
        
        return inscricaoRepository.findById(id)
                .map(inscricao -> {
                    if (!inscricao.getAluno().getId().equals(usuario.getId())) {
                        auditoriaService.registrarAcessoNegado(usuario, AcaoSistema.CANCELAR_INSCRICAO,
                                "Aluno tentou cancelar inscricao de outro aluno");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Voce so pode cancelar suas proprias inscricoes");
                    }
                    
                    auditoriaService.registrarAcao(usuario, AcaoSistema.CANCELAR_INSCRICAO, 
                            "Cancelando inscricao ID: " + id + " da aula ID: " + inscricao.getAula().getId());
                    
                    inscricao.setStatus("CANCELADA");
                    inscricaoRepository.save(inscricao);

                    Aula aula = inscricao.getAula();
                    aula.setVagasDisponiveis(aula.getVagasDisponiveis() + 1);
                    aulaRepository.save(aula);

                    return ResponseEntity.ok("Inscrição cancelada com sucesso");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id, @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        return inscricaoRepository.findById(id)
                .map(inscricao -> {
                    if (usuario.getPerfil() != PerfilUsuario.ADMINISTRADOR &&
                        (usuario.getPerfil() != PerfilUsuario.ALUNO || 
                         !inscricao.getAluno().getId().equals(usuario.getId()))) {
                        auditoriaService.registrarAcessoNegado(usuario, AcaoSistema.CANCELAR_INSCRICAO,
                                "Usuario tentou deletar inscricao sem permissao");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Voce nao tem permissao para deletar esta inscricao");
                    }
                    
                    auditoriaService.registrarAcao(usuario, AcaoSistema.CANCELAR_INSCRICAO, 
                            "Deletando inscricao ID: " + id);
                    
                    if ("CONFIRMADA".equalsIgnoreCase(inscricao.getStatus())) {
                        Aula aula = inscricao.getAula();
                        aula.setVagasDisponiveis(aula.getVagasDisponiveis() + 1);
                        aulaRepository.save(aula);
                    }

                    inscricaoRepository.delete(inscricao);
                    return ResponseEntity.ok("Inscrição deletada com sucesso");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}