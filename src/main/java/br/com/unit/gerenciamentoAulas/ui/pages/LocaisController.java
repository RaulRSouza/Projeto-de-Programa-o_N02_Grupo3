package br.com.unit.gerenciamentoAulas.ui.pages;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.entidades.Local;
import br.com.unit.gerenciamentoAulas.repositories.LocalRepository;
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
public class LocaisController {

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private SessionManager sessionManager;

    @FXML private TextField txtBusca;
    @FXML private Label lblTotalLocais;
    @FXML private Label lblDisponiveis;
    @FXML private Label lblInfo;
    @FXML private TableView<LocalRow> tabelaLocais;
    @FXML private TableColumn<LocalRow, Long> colId;
    @FXML private TableColumn<LocalRow, String> colNome;
    @FXML private TableColumn<LocalRow, String> colEndereco;
    @FXML private TableColumn<LocalRow, String> colCapacidade;
    @FXML private TableColumn<LocalRow, String> colTipo;
    @FXML private TableColumn<LocalRow, String> colDisponivel;
    @FXML private TableColumn<LocalRow, Void> colAcoes;
    @FXML private Label lblFlash;

    private final ObservableList<LocalRow> locaisData = FXCollections.observableArrayList();
    private PauseTransition flashTimer;
    private static final String FLASH_SUCESSO =
            "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final String FLASH_ALERTA =
            "-fx-background-color: #ffedd5; -fx-text-fill: #92400e; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        configurarTabela();
        carregarLocais();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colCapacidade.setCellValueFactory(new PropertyValueFactory<>("capacidade"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDisponivel.setCellValueFactory(new PropertyValueFactory<>("disponivel"));

        colAcoes.setCellFactory(column -> new TableCell<>() {
            private final Button btnToggle = new Button();
            private final HBox container = new HBox(btnToggle);

            {
                btnToggle.getStyleClass().add("btn-secondary");
                container.setSpacing(8);
                container.getStyleClass().add("actions-cell");

                btnToggle.setOnAction(event -> {
                    LocalRow row = getTableView().getItems().get(getIndex());
                    alternarDisponibilidade(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    LocalRow row = getTableView().getItems().get(getIndex());
                    btnToggle.setText(row.isDisponivel() ? "⛔ Indisponibilizar" : "✅ Disponibilizar");
                    setGraphic(container);
                }
            }
        });

        tabelaLocais.setItems(locaisData);
    }

    private void carregarLocais() {
        atualizarTabela(localRepository.findAll());
    }

    private void atualizarTabela(List<Local> locais) {
        locaisData.clear();
        locaisData.addAll(locais.stream()
            .map(LocalRow::new)
            .collect(Collectors.toList()));

        atualizarEstatisticas();
        lblInfo.setText(String.format("Exibindo %d local(is)", locaisData.size()));
    }

    private void atualizarEstatisticas() {
        long total = localRepository.count();
        long disponiveis = localRepository.findByDisponivel(true).size();

        lblTotalLocais.setText(String.valueOf(total));
        lblDisponiveis.setText(String.valueOf(disponiveis));
    }

    @FXML
    private void handleAtualizar() {
        carregarLocais();
        mostrarFlash("Locais atualizados às " + LocalDateTime.now().format(HORA_FORMATTER), true);
    }

    @FXML
    private void handleNovo() {
        if (!sessionManager.podeCriarLocal()) {
            sessionManager.mostrarAcessoNegado("Criar Local");
            return;
        }
        abrirModal("/fxml/pages/CriarLocal.fxml", "Cadastrar Local");
    }

    @FXML
    private void handleBuscar() {
        String termo = Optional.ofNullable(txtBusca.getText())
                .map(String::trim)
                .orElse("");

        if (termo.isEmpty()) {
            carregarLocais();
            return;
        }

        String termoLower = termo.toLowerCase();
        List<Local> filtrados = localRepository.findAll().stream()
                .filter(local ->
                        (local.getNome() != null && local.getNome().toLowerCase().contains(termoLower)) ||
                        (local.getTipo() != null && local.getTipo().toLowerCase().contains(termoLower)) ||
                        (local.getEndereco() != null && local.getEndereco().toLowerCase().contains(termoLower)))
                .collect(Collectors.toList());

        atualizarTabela(filtrados);
    }

    @FXML
    private void handleLimpar() {
        txtBusca.clear();
        carregarLocais();
    }

    private void alternarDisponibilidade(LocalRow row) {
        if (!sessionManager.podeEditarLocal()) {
            sessionManager.mostrarAcessoNegado("Editar Local");
            return;
        }
        
        Local local = row.getLocal();
        local.setDisponivel(!local.isDisponivel());
        localRepository.save(local);
        carregarLocais();
        mostrarAlerta(Alert.AlertType.INFORMATION,
                "Status atualizado",
                String.format("Local %s agora está %s.",
                        local.getNome(),
                        local.isDisponivel() ? "disponível" : "indisponível"));
        mostrarFlash(String.format("Local %s agora está %s.",
                local.getNome(),
                local.isDisponivel() ? "disponível" : "indisponível"), true);
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

            carregarLocais();
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

    public static class LocalRow {
        private final Local local;

        public LocalRow(Local local) {
            this.local = local;
        }

        public Long getId() {
            return local.getId();
        }

        public String getNome() {
            return local.getNome();
        }

        public String getEndereco() {
            return Optional.ofNullable(local.getEndereco()).orElse("—");
        }

        public String getCapacidade() {
            return local.getCapacidade() + " pessoas";
        }

        public String getTipo() {
            return Optional.ofNullable(local.getTipo()).orElse("Não informado");
        }

        public String getDisponivel() {
            return local.isDisponivel() ? "Sim" : "Não";
        }

        public boolean isDisponivel() {
            return local.isDisponivel();
        }

        private Local getLocal() {
            return local;
        }
    }
}
