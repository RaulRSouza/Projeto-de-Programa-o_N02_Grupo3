package br.com.unit.gerenciamentoAulas.ui.pages;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.repositories.AlunoRepository;
import br.com.unit.gerenciamentoAulas.repositories.InscricaoRepository;
import br.com.unit.gerenciamentoAulas.ui.SessionManager;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

@Controller
public class AlunosController {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private InscricaoRepository inscricaoRepository;
    @Autowired
    private SessionManager sessionManager;

    @FXML private TextField txtBusca;
    @FXML private Label lblTotalAlunos;
    @FXML private Label lblCursos;
    @FXML private Label lblInfo;
    @FXML private Label lblFlash;
    @FXML private TableView<AlunoRow> tabelaAlunos;
    @FXML private TableColumn<AlunoRow, Long> colId;
    @FXML private TableColumn<AlunoRow, String> colNome;
    @FXML private TableColumn<AlunoRow, String> colEmail;
    @FXML private TableColumn<AlunoRow, String> colCurso;
    @FXML private TableColumn<AlunoRow, String> colMatricula;
    @FXML private TableColumn<AlunoRow, String> colTelefone;
    @FXML private TableColumn<AlunoRow, Void> colAcoes;

    private final ObservableList<AlunoRow> alunosData = FXCollections.observableArrayList();
    private PauseTransition flashTimer;
    private static final String FLASH_SUCESSO =
            "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final String FLASH_ALERTA =
            "-fx-background-color: #ffedd5; -fx-text-fill: #92400e; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";

    @FXML
    public void initialize() {
        configurarTabela();
        carregarAlunos();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        colAcoes.setCellFactory(column -> new TableCell<>() {
            private final Button btnRemover = new Button("🗑️ Remover");
            private final HBox container = new HBox(btnRemover);

            {
                btnRemover.getStyleClass().add("btn-secondary");
                container.setSpacing(8);
                container.getStyleClass().add("actions-cell");

                btnRemover.setOnAction(event -> {
                    AlunoRow row = getTableView().getItems().get(getIndex());
                    removerAluno(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tabelaAlunos.setItems(alunosData);
    }

    private void carregarAlunos() {
        atualizarTabela(alunoRepository.findAll());
    }

    private void atualizarTabela(List<Aluno> alunos) {
        alunosData.setAll(alunos.stream()
                .map(AlunoRow::new)
                .collect(Collectors.toList()));

        lblTotalAlunos.setText(String.valueOf(alunos.size()));

        long cursosDistintos = alunos.stream()
                .map(Aluno::getCurso)
                .filter(curso -> curso != null && !curso.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .count();
        lblCursos.setText(String.valueOf(cursosDistintos));

        lblInfo.setText(String.format("Exibindo %d aluno(s)", alunos.size()));
    }

    @FXML
    private void handleAtualizar() {
        carregarAlunos();
        mostrarFlash("Alunos atualizados às " + LocalDateTime.now().format(HORA_FORMATTER), true);
    }

    @FXML
    private void handleNovo() {
        if (!sessionManager.podeCriarAluno()) {
            sessionManager.mostrarAcessoNegado("Criar Aluno");
            return;
        }
        abrirModal("/fxml/pages/CriarAluno.fxml", "Cadastrar Aluno");
    }

    @FXML
    private void handleBuscar() {
        String termo = Optional.ofNullable(txtBusca.getText())
                .map(String::trim)
                .orElse("");

        if (termo.isEmpty()) {
            carregarAlunos();
            return;
        }

        String termoLower = termo.toLowerCase();
        List<Aluno> filtrados = alunoRepository.findAll().stream()
                .filter(aluno ->
                        (aluno.getNome() != null && aluno.getNome().toLowerCase().contains(termoLower)) ||
                        (aluno.getEmail() != null && aluno.getEmail().toLowerCase().contains(termoLower)) ||
                        (aluno.getMatricula() != null && aluno.getMatricula().toLowerCase().contains(termoLower)) ||
                        (aluno.getCurso() != null && aluno.getCurso().toLowerCase().contains(termoLower)))
                .collect(Collectors.toList());

        atualizarTabela(filtrados);
    }

    @FXML
    private void handleLimpar() {
        txtBusca.clear();
        carregarAlunos();
    }

    private void removerAluno(AlunoRow row) {
        if (!sessionManager.podeDeletarAluno()) {
            sessionManager.mostrarAcessoNegado("Deletar Aluno");
            return;
        }

        long inscricoesAssociadas = inscricaoRepository.countByAlunoId(row.getId());
        if (inscricoesAssociadas > 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Remoção bloqueada",
                    "Este aluno possui inscrições ativas e não pode ser removido.");
            mostrarFlash("Não é possível remover: aluno possui inscrições.", false);
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar remoção");
        confirmacao.setHeaderText("Deseja remover o aluno selecionado?");
        confirmacao.setContentText(row.getNome());

        confirmacao.showAndWait()
                .filter(resposta -> resposta == ButtonType.OK)
                .ifPresent(resposta -> {
                    alunoRepository.delete(row.getAluno());
                    carregarAlunos();
                    mostrarAlerta(Alert.AlertType.INFORMATION,
                            "Removido",
                            "Aluno removido com sucesso.");
                    mostrarFlash("Aluno removido com sucesso.", true);
                });
    }

    private void abrirModal(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(SpringContext.getSpringContext()::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            carregarAlunos();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Erro ao abrir janela",
                    "Não foi possível carregar: " + fxmlPath);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarFlash(String mensagem, boolean sucesso) {
        if (lblFlash == null) {
            return;
        }
        lblFlash.setText(mensagem);
        lblFlash.setStyle(sucesso ? FLASH_SUCESSO : FLASH_ALERTA);
        lblFlash.setVisible(true);
        lblFlash.setManaged(true);
        if (flashTimer != null) {
            flashTimer.stop();
        }
        flashTimer = new PauseTransition(Duration.seconds(4));
        flashTimer.setOnFinished(e -> {
            lblFlash.setVisible(false);
            lblFlash.setManaged(false);
        });
        flashTimer.play();
    }

    public static class AlunoRow {
        private final Aluno aluno;

        public AlunoRow(Aluno aluno) {
            this.aluno = aluno;
        }

        public Long getId() { return aluno.getId(); }
        public String getNome() { return Optional.ofNullable(aluno.getNome()).orElse("—"); }
        public String getEmail() { return Optional.ofNullable(aluno.getEmail()).orElse("—"); }
        public String getCurso() { return Optional.ofNullable(aluno.getCurso()).orElse("—"); }
        public String getMatricula() { return Optional.ofNullable(aluno.getMatricula()).orElse("—"); }
        public String getTelefone() { return Optional.ofNullable(aluno.getTelefone()).orElse("—"); }

        private Aluno getAluno() {
            return aluno;
        }
    }
}

