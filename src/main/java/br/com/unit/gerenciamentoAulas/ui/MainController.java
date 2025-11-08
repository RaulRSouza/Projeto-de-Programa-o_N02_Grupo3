package br.com.unit.gerenciamentoAulas.ui;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.servicos.AulaService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

@Controller
public class MainController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private AulaService aulaService;

    @Autowired
    private ApplicationContext springContext;

    @FXML private StackPane contentArea;
    @FXML private TableView<AulaTableRow> tabelaAulas;
    @FXML private TableColumn<AulaTableRow, Long> colId;
    @FXML private TableColumn<AulaTableRow, String> colCurso;
    @FXML private TableColumn<AulaTableRow, String> colInstrutor;
    @FXML private TableColumn<AulaTableRow, String> colLocal;
    @FXML private TableColumn<AulaTableRow, String> colDataHora;
    @FXML private TableColumn<AulaTableRow, String> colVagas;
    @FXML private TableColumn<AulaTableRow, String> colStatus;
    @FXML private Label lblTotal;
    @FXML private Label lblTotalAulas;
    @FXML private Label lblProximasAulas;
    @FXML private Label lblVagasDisponiveis;
    @FXML private Label lblFlash;

    private final ObservableList<AulaTableRow> aulasData = FXCollections.observableArrayList();
    private javafx.scene.Node dashboardRoot;
    private PauseTransition flashTimer;
    private static final String FLASH_SUCESSO =
            "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final String FLASH_ALERTA =
            "-fx-background-color: #ffedd5; -fx-text-fill: #92400e; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final DateTimeFormatter FLASH_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        System.out.println("====================================");
        System.out.println("Inicializando MainController...");
        System.out.println("====================================");
        
        System.out.println("Verificando componentes:");
        System.out.println("   - tabelaAulas: " + (tabelaAulas != null ? "[OK]" : "[FALHA]"));
        System.out.println("   - lblTotalAulas: " + (lblTotalAulas != null ? "[OK]" : "[FALHA]"));
        System.out.println("   - lblProximasAulas: " + (lblProximasAulas != null ? "[OK]" : "[FALHA]"));
        System.out.println("   - lblVagasDisponiveis: " + (lblVagasDisponiveis != null ? "[OK]" : "[FALHA]"));
        System.out.println("   - aulaService: " + (aulaService != null ? "[OK]" : "[FALHA]"));
        
        if (tabelaAulas != null) {
            System.out.println("⚙️ Configurando colunas da tabela...");
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
            colInstrutor.setCellValueFactory(new PropertyValueFactory<>("instrutor"));
            colLocal.setCellValueFactory(new PropertyValueFactory<>("local"));
            colDataHora.setCellValueFactory(new PropertyValueFactory<>("dataHora"));
            colVagas.setCellValueFactory(new PropertyValueFactory<>("vagas"));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colId.setStyle("-fx-alignment: CENTER;");
            colDataHora.setStyle("-fx-alignment: CENTER;");
            colVagas.setStyle("-fx-alignment: CENTER;");
            colStatus.setStyle("-fx-alignment: CENTER;");
            tabelaAulas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tabelaAulas.setItems(aulasData);
            System.out.println("Colunas configuradas!");
        }

        if (contentArea != null && !contentArea.getChildren().isEmpty()) {
            dashboardRoot = contentArea.getChildren().get(0);
        }
        
        System.out.println("Aguardando Spring carregar dados...");
        Platform.runLater(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("Carregando dados do banco...");
                carregarDashboard();
            } catch (Exception e) {
                System.err.println("Erro ao carregar dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleDashboard() {
        System.out.println("Botão Dashboard clicado - recarregando dados...");
        if (dashboardRoot != null) {
            contentArea.getChildren().setAll(dashboardRoot);
        }
        
        carregarDashboard();
    }

    @FXML
    private void handleGerenciarAulas() {
        carregarPagina("/fxml/pages/GerenciarAulas.fxml");
    }

    @FXML
    private void handleCursos() {
        carregarPagina("/fxml/pages/Cursos.fxml");
    }

    @FXML
    private void handleInstrutores() {
        carregarPagina("/fxml/pages/Instrutores.fxml");
    }

    @FXML
    private void handleLocais() {
        carregarPagina("/fxml/pages/Locais.fxml");
    }

    @FXML
    private void handleInscricoes() {
        carregarPagina("/fxml/pages/Inscricoes.fxml");
    }

    @FXML
    private void handleConfiguracoes() {
        try {
            carregarPagina("/fxml/pages/ConfiguracoesSimples.fxml");
        } catch (Exception e) {
            mostrarErro("Erro ao carregar Configurações", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSair() {
        Platform.exit();
    }

    @FXML
    private void handleAtualizar() {
        carregarDashboard();
        mostrarFlash("Dashboard atualizado às " + LocalDateTime.now().format(FLASH_FORMATTER), true);
    }

    private void carregarDashboard() {
        try {
            System.out.println("========================================");
            System.out.println("Carregando dashboard...");
            System.out.println("========================================");
            
            if (aulaService == null) {
                System.err.println("AulaService está NULL!");
                System.err.println("   Spring não injetou o serviço!");
                return;
            }
            
            System.out.println("AulaService está disponível!");
            System.out.println("Buscando aulas do banco...");
            
            List<Aula> todasAulas = aulaService.listarTodas();
            System.out.println("Total de aulas encontradas: " + todasAulas.size());
            
            if (todasAulas.isEmpty()) {
                System.err.println("BANCO ESTÁ VAZIO!");
                System.err.println("   data.sql NÃO foi executado!");
                System.err.println("   Ou o banco foi recriado sem dados!");
            }
            
            List<Aula> proximasAulas = aulaService.listarAulasFuturas();
            System.out.println("Aulas futuras: " + proximasAulas.size());
            
            List<Aula> aulasDisponiveis = aulaService.listarAulasDisponiveis();
            System.out.println("Aulas disponíveis: " + aulasDisponiveis.size());

            if (lblTotalAulas != null) {
                lblTotalAulas.setText(String.valueOf(todasAulas.size()));
            }
            
            if (lblProximasAulas != null) {
                lblProximasAulas.setText(String.valueOf(proximasAulas.size()));
            }
            
            int totalVagasDisponiveis = aulasDisponiveis.stream()
                    .mapToInt(Aula::getVagasDisponiveis)
                    .sum();
                    
            if (lblVagasDisponiveis != null) {
                lblVagasDisponiveis.setText(String.valueOf(totalVagasDisponiveis));
            }

            List<Aula> aulasParaExibir = proximasAulas.size() > 0 ? 
                proximasAulas.subList(0, Math.min(10, proximasAulas.size())) : 
                todasAulas.subList(0, Math.min(10, todasAulas.size()));
                
            System.out.println("Atualizando tabela com " + aulasParaExibir.size() + " aulas");
            atualizarTabela(aulasParaExibir);
            
            System.out.println("Dashboard carregado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao carregar dashboard: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao carregar dashboard", e.getMessage());
        }
    }

    private void carregarPagina(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            mostrarErro("Erro ao carregar página", "Não foi possível carregar: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void atualizarTabela(List<Aula> aulas) {
        aulasData.clear();
        aulas.forEach(aula -> aulasData.add(new AulaTableRow(
                aula.getId(),
                aula.getCurso().getNome(),
                aula.getInstrutor().getNome(),
                aula.getLocal().getNome(),
                aula.getDataHoraInicio().format(FORMATTER),
                aula.getVagasDisponiveis() + "/" + aula.getVagasTotais(),
                aula.getStatus()
        )));
        lblTotal.setText("Exibindo: " + aulas.size() + " aula(s)");
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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

    public static class AulaTableRow {
        private final Long id;
        private final String curso;
        private final String instrutor;
        private final String local;
        private final String dataHora;
        private final String vagas;
        private final String status;

        public AulaTableRow(Long id, String curso, String instrutor, String local,
                            String dataHora, String vagas, String status) {
            this.id = id;
            this.curso = curso;
            this.instrutor = instrutor;
            this.local = local;
            this.dataHora = dataHora;
            this.vagas = vagas;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getCurso() { return curso; }
        public String getInstrutor() { return instrutor; }
        public String getLocal() { return local; }
        public String getDataHora() { return dataHora; }
        public String getVagas() { return vagas; }
        public String getStatus() { return status; }
    }
}
