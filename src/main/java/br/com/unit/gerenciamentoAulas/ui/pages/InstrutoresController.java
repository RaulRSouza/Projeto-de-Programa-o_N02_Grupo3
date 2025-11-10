package br.com.unit.gerenciamentoAulas.ui.pages;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.repositories.AulaRepository;
import br.com.unit.gerenciamentoAulas.repositories.InstrutorRepository;
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
public class InstrutoresController {

    @Autowired
    private InstrutorRepository instrutorRepository;
    @Autowired
    private AulaRepository aulaRepository;
    @Autowired
    private SessionManager sessionManager;

    @FXML private TextField txtBusca;
    @FXML private Label lblTotalInstrutores;
    @FXML private Label lblEspecialidades;
    @FXML private Label lblInfo;
    @FXML private TableView<InstrutorRow> tabelaInstrutores;
    @FXML private TableColumn<InstrutorRow, Long> colId;
    @FXML private TableColumn<InstrutorRow, String> colNome;
    @FXML private TableColumn<InstrutorRow, String> colEmail;
    @FXML private TableColumn<InstrutorRow, String> colEspecialidade;
    @FXML private TableColumn<InstrutorRow, String> colRegistro;
    @FXML private TableColumn<InstrutorRow, String> colTelefone;
    @FXML private TableColumn<InstrutorRow, Void> colAcoes;
    @FXML private Label lblFlash;

    private final ObservableList<InstrutorRow> instrutoresData = FXCollections.observableArrayList();
    private PauseTransition flashTimer;
    private static final String FLASH_SUCESSO =
            "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final String FLASH_ALERTA =
            "-fx-background-color: #ffedd5; -fx-text-fill: #92400e; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        configurarTabela();
        carregarInstrutores();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEspecialidade.setCellValueFactory(new PropertyValueFactory<>("especialidade"));
        colRegistro.setCellValueFactory(new PropertyValueFactory<>("registro"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        colAcoes.setCellFactory(column -> new TableCell<>() {
            private final Button btnRemover = new Button("🗑️ Remover");
            private final HBox container = new HBox(btnRemover);

            {
                btnRemover.getStyleClass().add("btn-secondary");
                container.setSpacing(8);
                container.getStyleClass().add("actions-cell");

                btnRemover.setOnAction(event -> {
                    InstrutorRow row = getTableView().getItems().get(getIndex());
                    removerInstrutor(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });

        tabelaInstrutores.setItems(instrutoresData);
    }

    private void carregarInstrutores() {
        List<Instrutor> instrutores = instrutorRepository.findAll();
        atualizarTabela(instrutores);
    }

    private void atualizarTabela(List<Instrutor> instrutores) {
        instrutoresData.clear();
        instrutoresData.addAll(instrutores.stream()
            .map(InstrutorRow::new)
            .collect(Collectors.toList()));

        atualizarEstatisticas(instrutores);
        lblInfo.setText(String.format("Exibindo %d instrutor(es)", instrutoresData.size()));
    }

    private void atualizarEstatisticas(List<Instrutor> instrutores) {
        lblTotalInstrutores.setText(String.valueOf(instrutorRepository.count()));
        Set<String> especialidades = instrutores.stream()
                .map(Instrutor::getEspecialidade)
                .filter(especialidade -> especialidade != null && !especialidade.isBlank())
                .collect(Collectors.toSet());
        lblEspecialidades.setText(String.valueOf(especialidades.size()));
    }

    @FXML
    private void handleAtualizar() {
        carregarInstrutores();
        mostrarFlash("Instrutores atualizados às " + LocalDateTime.now().format(HORA_FORMATTER), true);
    }

    @FXML
    private void handleNovo() {
        if (!sessionManager.podeCriarInstrutor()) {
            sessionManager.mostrarAcessoNegado("Criar Instrutor");
            return;
        }
        abrirModal("/fxml/pages/CriarInstrutor.fxml", "Cadastrar Instrutor");
    }

    @FXML
    private void handleBuscar() {
        String termo = Optional.ofNullable(txtBusca.getText())
                .map(String::trim)
                .orElse("");

        if (termo.isEmpty()) {
            carregarInstrutores();
            return;
        }

        String termoLower = termo.toLowerCase();

        List<Instrutor> filtrados = instrutorRepository.findAll().stream()
                .filter(instrutor ->
                        (instrutor.getNome() != null && instrutor.getNome().toLowerCase().contains(termoLower)) ||
                        (instrutor.getEspecialidade() != null && instrutor.getEspecialidade().toLowerCase().contains(termoLower)) ||
                        (instrutor.getRegistro() != null && instrutor.getRegistro().toLowerCase().contains(termoLower)))
                .collect(Collectors.toList());

        atualizarTabela(filtrados);
    }

    @FXML
    private void handleLimpar() {
        txtBusca.clear();
        carregarInstrutores();
    }

    private void removerInstrutor(InstrutorRow row) {
        if (!sessionManager.podeDeletarInstrutor()) {
            sessionManager.mostrarAcessoNegado("Deletar Instrutor");
            return;
        }
        
        Instrutor instrutor = row.getInstrutor();
        long aulasAssociadas = aulaRepository.countByInstrutorId(instrutor.getId());

        if (aulasAssociadas > 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Remoção bloqueada",
                    "Este instrutor possui aulas associadas e não pode ser removido.");
            mostrarFlash("Não é possível remover: instrutor possui aulas.", false);
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar remoção");
        confirmacao.setHeaderText("Deseja remover o instrutor selecionado?");
        confirmacao.setContentText(instrutor.getNome());

        confirmacao.showAndWait()
                .filter(resposta -> resposta == ButtonType.OK)
                .ifPresent(resposta -> {
                    instrutorRepository.delete(instrutor);
                    carregarInstrutores();
                    mostrarAlerta(Alert.AlertType.INFORMATION,
                            "Removido",
                            "Instrutor removido com sucesso.");
                    mostrarFlash("Instrutor removido com sucesso.", true);
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

            carregarInstrutores();
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

    public static class InstrutorRow {
        private final Instrutor instrutor;

        public InstrutorRow(Instrutor instrutor) {
            this.instrutor = instrutor;
        }

        public Long getId() {
            return instrutor.getId();
        }

        public String getNome() {
            return instrutor.getNome();
        }

        public String getEmail() {
            return instrutor.getEmail();
        }

        public String getEspecialidade() {
            return Optional.ofNullable(instrutor.getEspecialidade()).orElse("Não informada");
        }

        public String getRegistro() {
            return Optional.ofNullable(instrutor.getRegistro()).orElse("—");
        }

        public String getTelefone() {
            return Optional.ofNullable(instrutor.getTelefone()).orElse("—");
        }

        private Instrutor getInstrutor() {
            return instrutor;
        }
    }
}
