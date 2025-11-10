package br.com.unit.gerenciamentoAulas.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.unit.gerenciamentoAulas.dtos.ReagendamentoRequest;
import br.com.unit.gerenciamentoAulas.dtos.MaterialComplementarLinkRequest;
import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Local;
import br.com.unit.gerenciamentoAulas.repositories.AulaRepository;
import br.com.unit.gerenciamentoAulas.repositories.CursoRepository;
import br.com.unit.gerenciamentoAulas.repositories.InstrutorRepository;
import br.com.unit.gerenciamentoAulas.repositories.LocalRepository;
import br.com.unit.gerenciamentoAulas.servicos.NotificacaoService;
import br.com.unit.gerenciamentoAulas.servicos.CsvExportService;
import br.com.unit.gerenciamentoAulas.servicos.AuditoriaService;
import br.com.unit.gerenciamentoAulas.entidades.Usuario;
import br.com.unit.gerenciamentoAulas.entidades.AcaoSistema;
import br.com.unit.gerenciamentoAulas.repositories.UsuarioRepository;

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

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private CsvExportService csvExportService;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
    public ResponseEntity<?> criar(@RequestBody Aula aula, @RequestParam Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
            
            auditoriaService.validarCriacaoAula(usuario);
            auditoriaService.registrarAcao(usuario, AcaoSistema.CRIAR_AULA, 
                    "Criando nova aula");
            
            if (aula.getCurso() == null || aula.getCurso().getId() == null) {
                return ResponseEntity.badRequest().body("Curso eh obrigatorio");
            }
            if (aula.getInstrutor() == null || aula.getInstrutor().getId() == null) {
                return ResponseEntity.badRequest().body("Instrutor eh obrigatorio");
            }
            if (aula.getLocal() == null || aula.getLocal().getId() == null) {
                return ResponseEntity.badRequest().body("Local eh obrigatorio");
            }
            if (aula.getDataHoraInicio() == null || aula.getDataHoraFim() == null) {
                return ResponseEntity.badRequest().body("Datas de inicio e fim sao obrigatorias");
            }
            if (!aula.getDataHoraFim().isAfter(aula.getDataHoraInicio())) {
                return ResponseEntity.badRequest().body("Data/hora final deve ser posterior a inicial");
            }
            if (aula.getVagasTotais() <= 0) {
                return ResponseEntity.badRequest().body("Numero de vagas deve ser maior que zero");
            }

            Curso curso = cursoRepository.findById(aula.getCurso().getId())
                    .orElseThrow(() -> new RuntimeException("Curso nao encontrado"));
            Instrutor instrutor = instrutorRepository.findById(aula.getInstrutor().getId())
                    .orElseThrow(() -> new RuntimeException("Instrutor nao encontrado"));
            Local local = localRepository.findById(aula.getLocal().getId())
                    .orElseThrow(() -> new RuntimeException("Local nao encontrado"));

            if (!local.isDisponivel()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Local esta indisponivel para agendamentos");
            }

            boolean conflitoInstrutor = aulaRepository.existsConflitoInstrutor(
                    instrutor, aula.getDataHoraInicio(), aula.getDataHoraFim());
            if (conflitoInstrutor) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Instrutor ja possui aula neste horario");
            }

            boolean conflitoLocal = aulaRepository.existsConflitoLocal(
                    local, aula.getDataHoraInicio(), aula.getDataHoraFim());
            if (conflitoLocal) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Local ja esta ocupado neste horario");
            }

            if (aula.getVagasTotais() > local.getCapacidade()) {
                return ResponseEntity.badRequest()
                        .body("Numero de vagas excede a capacidade do local");
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
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Aula aulaAtualizada, 
                                       @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarEdicaoAula(usuario);
        auditoriaService.registrarAcao(usuario, AcaoSistema.EDITAR_AULA, 
                "Editando aula ID: " + id);
        
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


    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendar(@PathVariable Long id, @RequestBody ReagendamentoRequest request,
                                       @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarEdicaoAula(usuario);
        auditoriaService.registrarAcao(usuario, AcaoSistema.EDITAR_AULA, 
                "Reagendando aula ID: " + id);
        
        if (request == null) {
            return ResponseEntity.badRequest().body("Dados de reagendamento sao obrigatorios");
        }

        Optional<Aula> aulaOpt = aulaRepository.findById(id);
        if (aulaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Aula aula = aulaOpt.get();

        if ("CANCELADA".equalsIgnoreCase(aula.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Nao eh possivel reagendar uma aula cancelada");
        }

        LocalDateTime novoInicio = request.getNovaDataHoraInicio() != null
                ? request.getNovaDataHoraInicio()
                : aula.getDataHoraInicio();
        LocalDateTime novoFim = request.getNovaDataHoraFim() != null
                ? request.getNovaDataHoraFim()
                : aula.getDataHoraFim();

        if (novoInicio == null || novoFim == null) {
            return ResponseEntity.badRequest().body("Datas de inicio e fim sao obrigatorias");
        }
        if (!novoFim.isAfter(novoInicio)) {
            return ResponseEntity.badRequest().body("Data/hora final deve ser posterior a inicial");
        }

        Local localAtual = aula.getLocal();
        Local localDestino = localAtual;
        if (request.getNovoLocalId() != null && !request.getNovoLocalId().equals(localAtual.getId())) {
            localDestino = localRepository.findById(request.getNovoLocalId())
                    .orElse(null);
            if (localDestino == null) {
                return ResponseEntity.badRequest().body("Novo local nao encontrado");
            }
        }

        if (!localDestino.isDisponivel()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Local selecionado esta indisponivel");
        }

        boolean conflitoInstrutor = aulaRepository.existsConflitoInstrutorExcluindoAula(
                aula.getInstrutor(), novoInicio, novoFim, aula.getId());
        if (conflitoInstrutor) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Instrutor ja possui aula neste horario");
        }

        boolean conflitoLocal = aulaRepository.existsConflitoLocalExcluindoAula(
                localDestino, novoInicio, novoFim, aula.getId());
        if (conflitoLocal) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Local ja esta ocupado neste horario");
        }

        if (request.getNovasVagasTotais() != null) {
            auditoriaService.validarGerenciamentoCapacidade(usuario);
            auditoriaService.registrarAcao(usuario, AcaoSistema.GERENCIAR_CAPACIDADE, 
                    "Alterando capacidade da aula ID: " + id + " para " + request.getNovasVagasTotais() + " vagas");
            
            int novasVagas = request.getNovasVagasTotais();
            if (novasVagas <= 0) {
                return ResponseEntity.badRequest().body("Numero de vagas deve ser maior que zero");
            }
            if (novasVagas > localDestino.getCapacidade()) {
                return ResponseEntity.badRequest()
                        .body("Numero de vagas excede a capacidade do local");
            }
            if (novasVagas < aula.getInscricoes().size()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Numero de vagas nao pode ser menor que o total de inscricoes");
            }
            aula.setVagasTotais(novasVagas);
            aula.setVagasDisponiveis(novasVagas - aula.getInscricoes().size());
        } else if (aula.getVagasTotais() > localDestino.getCapacidade()) {
            return ResponseEntity.badRequest()
                    .body("Numero de vagas excede a capacidade do novo local");
        }

        if (request.getObservacoes() != null) {
            aula.setObservacoes(request.getObservacoes());
        }

        LocalDateTime inicioAnterior = aula.getDataHoraInicio();
        LocalDateTime fimAnterior = aula.getDataHoraFim();

        aula.setLocal(localDestino);
        aula.setDataHoraInicio(novoInicio);
        aula.setDataHoraFim(novoFim);

        Aula aulaAtualizada = aulaRepository.save(aula);
        notificacaoService.notificarReagendamento(
                aula.getId(),
                inicioAnterior,
                fimAnterior,
                novoInicio,
                novoFim
        );

        return ResponseEntity.ok(aulaAtualizada);
    }


    @PatchMapping("/{id}/material/link")
    public ResponseEntity<?> definirMaterialLink(@PathVariable Long id,
                                                 @RequestBody MaterialComplementarLinkRequest request,
                                                 @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarEdicaoConteudo(usuario);
        auditoriaService.registrarAcao(usuario, AcaoSistema.EDITAR_CONTEUDO_AULA, 
                "Definindo material complementar (link) para aula ID: " + id);
        
        if (request == null || request.getUrl() == null || request.getUrl().isBlank()) {
            return ResponseEntity.badRequest().body("URL do material eh obrigatoria");
        }

        return aulaRepository.findById(id)
                .map(aula -> {
                    aula.limparMaterialComplementar();
                    aula.setMaterialComplementarUrl(request.getUrl().trim());
                    Aula aulaAtualizada = aulaRepository.save(aula);
                    return ResponseEntity.ok(aulaAtualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/{id}/material/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMaterial(@PathVariable Long id,
                                            @RequestPart("arquivo") MultipartFile arquivo,
                                            @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarEdicaoConteudo(usuario);
        auditoriaService.registrarAcao(usuario, AcaoSistema.EDITAR_CONTEUDO_AULA, 
                "Upload de material complementar para aula ID: " + id);
        
        if (arquivo == null || arquivo.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo do material eh obrigatorio");
        }
        if (arquivo.getContentType() == null ||
                !MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(arquivo.getContentType())) {
            return ResponseEntity.badRequest().body("Somente arquivos PDF sao aceitos");
        }

        Optional<Aula> aulaOpt = aulaRepository.findById(id);
        if (aulaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Aula aula = aulaOpt.get();
        try {
            aula.setMaterialComplementarArquivo(arquivo.getBytes());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao processar o arquivo: " + e.getMessage());
        }

        aula.setMaterialComplementarTipo(arquivo.getContentType());
        aula.setMaterialComplementarNomeArquivo(arquivo.getOriginalFilename());
        aula.setMaterialComplementarUrl(null);

        Aula aulaAtualizada = aulaRepository.save(aula);
        return ResponseEntity.ok(aulaAtualizada);
    }

    @GetMapping("/{id}/material/download")
    public ResponseEntity<byte[]> downloadMaterial(@PathVariable Long id) {
        Optional<Aula> aulaOpt = aulaRepository.findById(id);
        if (aulaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Aula aula = aulaOpt.get();
        if (aula.getMaterialComplementarArquivo() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = aula.getMaterialComplementarTipo() != null
                    ? MediaType.parseMediaType(aula.getMaterialComplementarTipo())
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        String nomeArquivo = Optional.ofNullable(aula.getMaterialComplementarNomeArquivo())
                .orElse("material.pdf");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(mediaType)
                .body(aula.getMaterialComplementarArquivo());
    }

    @DeleteMapping("/{id}/material")
    public ResponseEntity<?> removerMaterial(@PathVariable Long id, @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarEdicaoConteudo(usuario);
        auditoriaService.registrarAcao(usuario, AcaoSistema.EDITAR_CONTEUDO_AULA, 
                "Removendo material complementar da aula ID: " + id);
        
        return aulaRepository.findById(id)
                .map(aula -> {
                    aula.limparMaterialComplementar();
                    aulaRepository.save(aula);
                    return ResponseEntity.ok("Material complementar removido com sucesso");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id, @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarCancelamentoAula(usuario);
        auditoriaService.registrarAcao(usuario, AcaoSistema.CANCELAR_AULA, 
                "Cancelando aula ID: " + id);
        
        return aulaRepository.findById(id)
                .map(aula -> {
                    aula.setStatus("CANCELADA");
                    aulaRepository.save(aula);
                    return ResponseEntity.ok("Aula cancelada com sucesso");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/solicitar-cancelamento")
    public ResponseEntity<?> solicitarCancelamento(@PathVariable Long id, 
                                                    @RequestParam Long usuarioId,
                                                    @RequestParam String motivo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.registrarSolicitacaoCancelamento(usuario, id, motivo);
        
        return ResponseEntity.ok("Solicitacao de cancelamento registrada. Aguarde aprovacao do Administrador.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id, @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        
        auditoriaService.validarCancelamentoAula(usuario);
        auditoriaService.registrarAcao(usuario, AcaoSistema.CANCELAR_AULA, 
                "Deletando aula ID: " + id);
        
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

    @GetMapping("/curso/{cursoId}/exportar-csv")
    public ResponseEntity<String> exportarAulasPorCursoCsv(@PathVariable Long cursoId) {

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso nao encontrado com ID: " + cursoId));

        List<Aula> aulas = aulaRepository.findByCurso(curso);

        String csvData = csvExportService.exportAulasToCsv(aulas);

        HttpHeaders headers = new HttpHeaders();
        String nomeArquivo = "aulas_curso_" + curso.getId() + ".csv";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeArquivo);
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8"); // Garante a codificação correta

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
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