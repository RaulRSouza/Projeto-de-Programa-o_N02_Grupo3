package br.com.unit.gerenciamentoAulas.servicos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.unit.gerenciamentoAulas.entidades.Inscricao;
import br.com.unit.gerenciamentoAulas.entidades.Notificacao;
import br.com.unit.gerenciamentoAulas.repositories.AlunoRepository;
import br.com.unit.gerenciamentoAulas.repositories.InscricaoRepository;
import br.com.unit.gerenciamentoAulas.repositories.NotificacaoRepository;

@Service
public class NotificacaoService {

    private static final DateTimeFormatter DATA_HORA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AlunoRepository alunoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final InscricaoRepository inscricaoRepository;

    public NotificacaoService(AlunoRepository alunoRepository,
                              NotificacaoRepository notificacaoRepository,
                              InscricaoRepository inscricaoRepository) {
        this.alunoRepository = alunoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Transactional
    public void notificarReagendamento(Long aulaId,
                                       LocalDateTime dataAnteriorInicio,
                                       LocalDateTime dataAnteriorFim,
                                       LocalDateTime novaDataInicio,
                                       LocalDateTime novaDataFim) {

        List<Inscricao> inscricoes = inscricaoRepository.findInscricoesConfirmadasPorAula(aulaId);

        for (Inscricao inscricao : inscricoes) {
            String mensagem = montarMensagemReagendamento(
                    inscricao.getAula().getCurso().getNome(),
                    dataAnteriorInicio,
                    dataAnteriorFim,
                    novaDataInicio,
                    novaDataFim
            );

            Notificacao notificacao = new Notificacao(
                    inscricao.getAluno(),
                    inscricao.getAula(),
                    "REAGENDAMENTO",
                    mensagem
            );

            notificacaoRepository.save(notificacao);
        }
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listarPorAluno(Long alunoId) {
        return alunoRepository.findById(alunoId)
                .map(notificacaoRepository::findByAlunoOrderByDataEnvioDesc)
                .orElse(List.of());
    }

    @Transactional
    public boolean marcarComoLida(Long notificacaoId) {
        Optional<Notificacao> notificacaoOpt = notificacaoRepository.findById(notificacaoId);
        if (notificacaoOpt.isEmpty()) {
            return false;
        }

        Notificacao notificacao = notificacaoOpt.get();
        if (!notificacao.isLida()) {
            notificacao.setLida(true);
            notificacaoRepository.save(notificacao);
        }
        return true;
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long alunoId) {
        return notificacaoRepository.countByAlunoIdAndLidaFalse(alunoId);
    }

    private String montarMensagemReagendamento(String nomeCurso,
                                               LocalDateTime dataAnteriorInicio,
                                               LocalDateTime dataAnteriorFim,
                                               LocalDateTime novaDataInicio,
                                               LocalDateTime novaDataFim) {

        return new StringBuilder()
                .append("A aula do curso ")
                .append(nomeCurso)
                .append(" foi reagendada de ")
                .append(DATA_HORA_FORMATTER.format(dataAnteriorInicio))
                .append(" - ")
                .append(DATA_HORA_FORMATTER.format(dataAnteriorFim))
                .append(" para ")
                .append(DATA_HORA_FORMATTER.format(novaDataInicio))
                .append(" - ")
                .append(DATA_HORA_FORMATTER.format(novaDataFim))
                .append(".")
                .toString();
    }
}
