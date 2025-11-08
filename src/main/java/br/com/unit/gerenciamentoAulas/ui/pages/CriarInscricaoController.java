package br.com.unit.gerenciamentoAulas.ui.pages;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Inscricao;
import br.com.unit.gerenciamentoAulas.repositories.AlunoRepository;
import br.com.unit.gerenciamentoAulas.repositories.AulaRepository;
import br.com.unit.gerenciamentoAulas.repositories.InscricaoRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

@Controller
public class CriarInscricaoController {
    
    @FXML private ComboBox<Aluno> alunoComboBox;
    @FXML private ComboBox<Aula> aulaComboBox;
    @FXML private DatePicker dataInscricaoDatePicker;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    @Autowired
    private InscricaoRepository inscricaoRepository;
    
    @Autowired
    private AlunoRepository alunoRepository;
    
    @Autowired
    private AulaRepository aulaRepository;

    private Inscricao inscricaoAtual = null;
    private boolean modoEdicao = false;

    @FXML
    public void initialize() {
        carregarAlunos();
        carregarAulas();

        configurarComboBox(alunoComboBox, aluno -> aluno.getNome() + " • Matrícula: " + aluno.getMatricula());
        configurarComboBox(aulaComboBox, aula -> {
            String curso = aula.getCurso() != null ? aula.getCurso().getNome() : "Curso não informado";
            String data = aula.getDataHoraInicio() != null
                    ? aula.getDataHoraInicio().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "Sem data";
            return curso + " • " + data;
        });

        if (dataInscricaoDatePicker != null && dataInscricaoDatePicker.getValue() == null) {
            dataInscricaoDatePicker.setValue(LocalDate.now());
        }
    }

    private void carregarAlunos() {
        try {
            if (alunoComboBox != null) {
                List<Aluno> alunos = alunoRepository.findAll().stream()
                        .sorted(java.util.Comparator.comparing(Aluno::getNome, String.CASE_INSENSITIVE_ORDER))
                        .toList();
                alunoComboBox.setItems(FXCollections.observableArrayList(alunos));
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar alunos: " + e.getMessage());
        }
    }

    private void carregarAulas() {
        try {
            if (aulaComboBox != null) {
                List<Aula> aulas = aulaRepository.findAll().stream()
                        .sorted(java.util.Comparator.comparing(aula -> aula.getDataHoraInicio(), java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                        .toList();
                aulaComboBox.setItems(FXCollections.observableArrayList(aulas));
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar aulas: " + e.getMessage());
        }
    }

    @FXML
    private void salvarInscricao() {
        try {
            if (!validarCampos()) {
                return;
            }

            Inscricao inscricao = obterInscricaoPersistente();
            inscricao.setAluno(alunoComboBox.getValue());
            inscricao.setAula(aulaComboBox.getValue());

            LocalDate data = dataInscricaoDatePicker.getValue();
            inscricao.setDataInscricao(data != null ? data.atStartOfDay() : java.time.LocalDateTime.now());

            if (!modoEdicao || inscricao.getStatus() == null) {
                inscricao.setStatus("CONFIRMADA");
            }

            inscricaoRepository.save(inscricao);

            String mensagem = modoEdicao ? "Inscrição atualizada com sucesso!" : "Inscrição criada com sucesso!";
            mostrarAlerta("Sucesso", mensagem, Alert.AlertType.INFORMATION);
            resetarFormulario();
            fecharJanela();
            
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao criar inscrição: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() {
        fecharJanela();
    }

    private boolean validarCampos() {
        if (alunoComboBox.getValue() == null) {
            mostrarAlerta("Validação", "Selecione um aluno!", Alert.AlertType.WARNING);
            return false;
        }
        if (aulaComboBox.getValue() == null) {
            mostrarAlerta("Validação", "Selecione uma aula!", Alert.AlertType.WARNING);
            return false;
        }
        if (dataInscricaoDatePicker.getValue() == null) {
            mostrarAlerta("Validação", "Selecione a data da inscrição!", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void fecharJanela() {
        Stage stage = (Stage) salvarButton.getScene().getWindow();
        stage.close();
    }

    public void prepararEdicao(Inscricao inscricao) {
        if (inscricao == null) {
            return;
        }
        this.inscricaoAtual = inscricaoRepository.findById(inscricao.getId()).orElse(inscricao);
        this.modoEdicao = true;
        preencherCamposSeEdicao();
    }

    private void preencherCamposSeEdicao() {
        if (!modoEdicao || inscricaoAtual == null) {
            return;
        }

        if (salvarButton != null) {
            salvarButton.setText("Salvar alterações");
        }

        if (alunoComboBox != null && inscricaoAtual.getAluno() != null) {
            alunoComboBox.getSelectionModel().select(inscricaoAtual.getAluno());
        }

        if (aulaComboBox != null && inscricaoAtual.getAula() != null) {
            aulaComboBox.getSelectionModel().select(inscricaoAtual.getAula());
        }

        if (dataInscricaoDatePicker != null && inscricaoAtual.getDataInscricao() != null) {
            dataInscricaoDatePicker.setValue(inscricaoAtual.getDataInscricao().toLocalDate());
        }
    }

    private Inscricao obterInscricaoPersistente() {
        if (!modoEdicao || inscricaoAtual == null) {
            return new Inscricao();
        }
        return inscricaoRepository.findById(inscricaoAtual.getId())
                .orElseThrow(() -> new IllegalStateException("Inscrição não encontrada para edição."));
    }

    private void resetarFormulario() {
        modoEdicao = false;
        inscricaoAtual = null;
        if (salvarButton != null) {
            salvarButton.setText("Salvar");
        }
    }

    private <T> void configurarComboBox(ComboBox<T> comboBox, Function<T, String> labelProvider) {
        comboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : labelProvider.apply(object);
            }

            @Override
            public T fromString(String string) {
                return comboBox.getItems().stream()
                        .filter(item -> labelProvider.apply(item).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : labelProvider.apply(item));
            }
        });
    }
}
