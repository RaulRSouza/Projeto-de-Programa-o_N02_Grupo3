package br.com.unit.gerenciamentoAulas.ui.pages;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.unit.gerenciamentoAulas.entidades.Inscricao;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

@Component
public class InscricoesController {
    
    @FXML
    private TableView<Inscricao> inscricoesTable;
    
    @FXML
    private TableColumn<Inscricao, Long> idColumn;
    
    @FXML
    private TableColumn<Inscricao, String> alunoColumn;
    
    @FXML
    private TableColumn<Inscricao, String> aulaColumn;
    
    @FXML
    private TableColumn<Inscricao, String> dataInscricaoColumn;
    
    @FXML
    private TableColumn<Inscricao, String> statusColumn;
    
    @FXML
    private TextField pesquisaField;
    
    @FXML
    private Button novaInscricaoBtn;
    
    @FXML
    private Button editarBtn;
    
    @FXML
    private Button excluirBtn;

    @FXML
    private Label lblFlash;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @Autowired
    private SessionManager sessionManager;
    
    private ObservableList<Inscricao> inscricoesList;
    private PauseTransition flashTimer;
    private static final String FLASH_SUCESSO =
            "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final String FLASH_ALERTA =
            "-fx-background-color: #ffedd5; -fx-text-fill: #92400e; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    @FXML
    public void initialize() {
        configurarTabela();
        carregarInscricoes();
    }
    
    private void configurarTabela() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        alunoColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getAluno() != null ? 
                cellData.getValue().getAluno().getNome() : "N/A"
            )
        );
        aulaColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getAula() != null ? 
                cellData.getValue().getAula().getTitulo() : "N/A"
            )
        );
        dataInscricaoColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDataInscricao() != null ?
                cellData.getValue().getDataInscricao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"
            )
        );
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        inscricoesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean itemSelecionado = newSelection != null;
                editarBtn.setDisable(!itemSelecionado);
                excluirBtn.setDisable(!itemSelecionado);
            }
        );
    }
    
    private void carregarInscricoes() {
        if (inscricoesList == null) {
            inscricoesList = FXCollections.observableArrayList();
        }
        inscricoesList.setAll(inscricaoRepository.findAll());
        inscricoesTable.setItems(inscricoesList);
        inscricoesTable.refresh();
    }
    
    @FXML
    private void handleNovaInscricao() {
        if (!sessionManager.podeCriarInscricao()) {
            sessionManager.mostrarAcessoNegado("Criar Inscrição");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pages/CriarInscricao.fxml"));
            loader.setControllerFactory(SpringContext.getSpringContext()::getBean);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Nova Inscrição");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            carregarInscricoes();
            mostrarFlash("Inscrições atualizadas após novo cadastro.", true);
        } catch (IOException e) {
            mostrarErro("Erro ao abrir formulário de inscrição: " + e.getMessage());
            e.printStackTrace();
            mostrarFlash("Falha ao abrir formulário de inscrição.", false);
        }
    }
    
    @FXML
    private void handleEditar() {
        if (!sessionManager.podeCancelarInscricao()) {
            sessionManager.mostrarAcessoNegado("Editar Inscrição");
            return;
        }
        
        Inscricao selecionada = inscricoesTable.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            mostrarAlerta("Nenhuma inscrição selecionada!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pages/CriarInscricao.fxml"));
            loader.setControllerFactory(SpringContext.getSpringContext()::getBean);
            Parent root = loader.load();

            CriarInscricaoController controller = loader.getController();
            controller.prepararEdicao(selecionada);

            Stage stage = new Stage();
            stage.setTitle("Editar Inscrição");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            carregarInscricoes();
            mostrarFlash("Inscrições atualizadas após edição.", true);
        } catch (IOException e) {
            mostrarErro("Erro ao abrir formulário de edição: " + e.getMessage());
            e.printStackTrace();
            mostrarFlash("Falha ao abrir formulário de edição.", false);
        }
    }
    
    @FXML
    private void handleExcluir() {
        Inscricao selecionada = inscricoesTable.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            mostrarAlerta("Nenhuma inscrição selecionada!");
            return;
        }
        
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir Inscrição");
        confirmacao.setContentText("Tem certeza que deseja excluir esta inscrição?");
        
        confirmacao.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                inscricaoRepository.delete(selecionada);
                carregarInscricoes();
                mostrarSucesso("Inscrição excluída com sucesso!");
                mostrarFlash("Inscrição excluída.", true);
            }
        });
    }
    
    @FXML
    private void handlePesquisar() {
        String termo = pesquisaField.getText().toLowerCase();
        if (termo.isEmpty()) {
            carregarInscricoes();
            return;
        }
        
        ObservableList<Inscricao> filtradas = inscricoesList.filtered(inscricao ->
            (inscricao.getAluno() != null && inscricao.getAluno().getNome().toLowerCase().contains(termo)) ||
            (inscricao.getAula() != null && inscricao.getAula().getTitulo().toLowerCase().contains(termo)) ||
            inscricao.getStatus().toLowerCase().contains(termo)
        );
        
        inscricoesTable.setItems(filtradas);
        inscricoesTable.refresh();
    }

    @FXML
    private void handleLimparPesquisa() {
        pesquisaField.clear();
        carregarInscricoes();
    }

    @FXML
    private void handleAtualizar() {
        carregarInscricoes();
        if (pesquisaField != null) {
            pesquisaField.clear();
        }
        mostrarFlash("Inscrições atualizadas às " + LocalDateTime.now().format(HORA_FORMATTER), true);
    }
    
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
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
}
