package br.com.unit.gerenciamentoAulas.ui.pages;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.exceptions.AulaNotFoundException;
import br.com.unit.gerenciamentoAulas.exceptions.BusinessException;
import br.com.unit.gerenciamentoAulas.servicos.AulaService;
import br.com.unit.gerenciamentoAulas.servicos.CsvExportService;
import br.com.unit.gerenciamentoAulas.ui.SessionManager;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

@Controller
public class GerenciarAulasController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private AulaService aulaService;

    @Autowired
    private CsvExportService csvExportService;

    @Autowired
    private SessionManager sessionManager;

    @FXML private TableView<AulaRow> tabelaAulas;
    @FXML private TableColumn<AulaRow, Long> colId;
    @FXML private TableColumn<AulaRow, String> colTitulo;
    @FXML private TableColumn<AulaRow, String> colCurso;
    @FXML private TableColumn<AulaRow, String> colInstrutor;
    @FXML private TableColumn<AulaRow, String> colLocal;
    @FXML private TableColumn<AulaRow, String> colDataInicio;
    @FXML private TableColumn<AulaRow, String> colDataFim;
    @FXML private TableColumn<AulaRow, String> colVagas;
    @FXML private TableColumn<AulaRow, String> colStatus;
    @FXML private TableColumn<AulaRow, String> colAlertas;
    @FXML private TableColumn<AulaRow, Void> colAcoes;
    @FXML private Label lblTotal;
    @FXML private Label lblAlertasVisuais;

    private final ObservableList<AulaRow> aulasData = FXCollections.observableArrayList();
    private PauseTransition alertaTimeout;
    private static final String ALERTA_SUCESSO_STYLE =
            "-fx-text-fill: #166534; -fx-background-color: #dcfce7; -fx-padding: 6 12; -fx-background-radius: 6;";
    private static final String ALERTA_AVISO_STYLE =
            "-fx-text-fill: #92400e; -fx-background-color: #ffedd5; -fx-padding: 6 12; -fx-background-radius: 6;";

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
        colInstrutor.setCellValueFactory(new PropertyValueFactory<>("instrutor"));
        colLocal.setCellValueFactory(new PropertyValueFactory<>("local"));
        colDataInicio.setCellValueFactory(new PropertyValueFactory<>("dataInicio"));
        colDataFim.setCellValueFactory(new PropertyValueFactory<>("dataFim"));
        colVagas.setCellValueFactory(new PropertyValueFactory<>("vagas"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAlertas.setCellValueFactory(data -> data.getValue().alertaProperty());
        colAlertas.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #b45309;");
                }
            }
        });

        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnCancelar = new Button("🚫");
            private final Button btnDeletar = new Button("🗑️");
            private final HBox hbox = new HBox(5, btnEditar, btnCancelar, btnDeletar);

            {
                hbox.setAlignment(Pos.CENTER);
                btnEditar.getStyleClass().add("btn-action-edit");
                btnCancelar.getStyleClass().add("btn-action-cancel");
                btnDeletar.getStyleClass().add("btn-action-delete");
                
                btnEditar.setOnAction(e -> handleEditar(getTableView().getItems().get(getIndex())));
                btnCancelar.setOnAction(e -> handleCancelar(getTableView().getItems().get(getIndex())));
                btnDeletar.setOnAction(e -> handleDeletar(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        tabelaAulas.setItems(aulasData);
        tabelaAulas.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(AulaRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || !item.hasConflito()) {
                    setStyle("");
                    setTooltip(null);
                } else {
                    setStyle("-fx-background-color: #FFF7ED;");
                    Tooltip tooltip = new Tooltip(item.getDescricaoConflito());
                    setTooltip(tooltip);
                }
            }
        });
        carregarTodasAulas();
    }

    @FXML
    private void handleNova() {
        if (!sessionManager.podeCriarAula()) {
            sessionManager.mostrarAcessoNegado("Criar Aula");
            return;
        }
        abrirModal("/fxml/pages/CriarAula.fxml", "Cadastrar Aula");
    }

    @FXML
    private void handleAtualizar() {
        carregarTodasAulas();
        mostrarAlertaVisual("Lista de aulas atualizada às " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), ALERTA_SUCESSO_STYLE);
    }

    @FXML
    private void handleTodas() {
        carregarAulas(aulaService::listarTodas);
    }

    @FXML
    private void handleFuturas() {
        carregarAulas(aulaService::listarAulasFuturas);
    }

    @FXML
    private void handleDisponiveis() {
        carregarAulas(aulaService::listarAulasDisponiveis);
    }

    @FXML
    private void handleCanceladas() {
        carregarAulas(() -> aulaService.listarPorStatus("CANCELADA"));
    }

    @FXML
    private void handleExportarCSV() {
        try {
            List<Aula> aulas = aulaService.listarTodas();
            String csvData = csvExportService.exportAulasToCsv(aulas);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Arquivo CSV");
            fileChooser.setInitialFileName("aulas_export.csv");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            File file = fileChooser.showSaveDialog(tabelaAulas.getScene().getWindow());
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(csvData);
                }
                mostrarSucesso("Exportado com sucesso!", "Arquivo salvo em: " + file.getAbsolutePath());
                mostrarAlertaVisual("CSV exportado para " + file.getName(), ALERTA_SUCESSO_STYLE);
            }
        } catch (Exception e) {
            mostrarErro("Erro ao exportar", e.getMessage());
            mostrarAlertaVisual("Falha ao exportar CSV", ALERTA_AVISO_STYLE);
        }
    }

    private void handleEditar(AulaRow row) {
        try {
            Aula aula = aulaService.buscarPorId(row.getId());
            Long instrutorId = aula.getInstrutor() != null ? aula.getInstrutor().getId() : null;
            
            if (!sessionManager.podeEditarAula() && 
                !sessionManager.podeEditarAulaDoInstrutor(instrutorId)) {
                sessionManager.mostrarAcessoNegadoAulaNaoPropria();
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pages/EditarAula.fxml"));
            loader.setControllerFactory(SpringContext.getSpringContext()::getBean);
            Parent root = loader.load();

            EditarAulaController controller = loader.getController();
            controller.setAulaId(row.getId());

            Stage stage = new Stage();
            stage.setTitle("Editar Aula");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            carregarTodasAulas();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir janela", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleCancelar(AulaRow row) {
        try {
            Aula aula = aulaService.buscarPorId(row.getId());
            Long instrutorId = aula.getInstrutor() != null ? aula.getInstrutor().getId() : null;
            
            if (!sessionManager.podeCancelarAula() && 
                !sessionManager.podeEditarAulaDoInstrutor(instrutorId)) {
                sessionManager.mostrarAcessoNegadoAulaNaoPropria();
                return;
            }
            
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Cancelar Aula");
            confirmacao.setHeaderText("Deseja realmente cancelar esta aula?");
            confirmacao.setContentText("Aula: " + row.getTitulo());

            Optional<ButtonType> resultado = confirmacao.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                aulaService.cancelarAula(row.getId(), "Cancelada pelo usuário");
                mostrarSucesso("Aula cancelada", "A aula foi cancelada com sucesso");
                carregarTodasAulas();
            }
        } catch (Exception e) {
            mostrarErro("Erro ao cancelar", e.getMessage());
        }
    }

    private void handleDeletar(AulaRow row) {
        if (!sessionManager.podeDeletarAula()) {
            sessionManager.mostrarAcessoNegado("Deletar Aula");
            return;
        }
        
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Deletar Aula");
        confirmacao.setHeaderText("Deseja realmente deletar esta aula?");
        confirmacao.setContentText("Esta ação não pode ser desfeita!");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                aulaService.deletarAula(row.getId());
                String titulo = row.getTitulo() != null ? row.getTitulo() : "Aula";
                mostrarSucesso("Aula deletada", titulo + " foi removida com sucesso.");
                carregarTodasAulas();
            } catch (AulaNotFoundException | BusinessException e) {
                mostrarErro("Não foi possível deletar", e.getMessage());
            } catch (Exception e) {
                mostrarErro("Erro inesperado", "Falha ao deletar a aula. Tente novamente.");
            }
        }
    }

    private void carregarTodasAulas() {
        carregarAulas(aulaService::listarTodas);
    }

    private void carregarAulas(SupplierChecked supplier) {
        try {
            Platform.runLater(() -> {
                try {
                    List<Aula> aulas = supplier.get();
                    atualizarTabela(aulas);
                } catch (Exception e) {
                    mostrarErro("Erro ao carregar aulas", e.getMessage());
                }
            });
        } catch (Exception e) {
            mostrarErro("Erro", e.getMessage());
        }
    }

    private void atualizarTabela(List<Aula> aulas) {
        Set<Long> conflitosInstrutor = identificarConflitos(aulas, aula ->
                aula.getInstrutor() != null ? aula.getInstrutor().getId() : null);
        Set<Long> conflitosLocal = identificarConflitos(aulas, aula ->
                aula.getLocal() != null ? aula.getLocal().getId() : null);

        aulasData.clear();
        aulas.forEach(aula -> aulasData.add(new AulaRow(
                aula.getId(),
                aula.getTitulo() != null ? aula.getTitulo() : "Sem título",
                aula.getCurso().getNome(),
                aula.getInstrutor().getNome(),
                aula.getLocal().getNome(),
                aula.getDataHoraInicio().format(FORMATTER),
                aula.getDataHoraFim().format(FORMATTER),
                aula.getVagasDisponiveis() + "/" + aula.getVagasTotais(),
                aula.getStatus(),
                conflitosInstrutor.contains(aula.getId()),
                conflitosLocal.contains(aula.getId())
        )));
        lblTotal.setText("Total: " + aulas.size() + " aula(s)");
    }

    private Set<Long> identificarConflitos(List<Aula> aulas, Function<Aula, Long> chaveExtractor) {
        Map<Long, List<Aula>> agrupadas = aulas.stream()
                .filter(aula -> !"CANCELADA".equalsIgnoreCase(aula.getStatus()))
                .filter(aula -> chaveExtractor.apply(aula) != null)
                .collect(Collectors.groupingBy(chaveExtractor));

        Set<Long> idsComConflito = new HashSet<>();

        for (List<Aula> lista : agrupadas.values()) {
            List<Aula> ordenadas = new ArrayList<>(lista);
            ordenadas.sort(Comparator.comparing(Aula::getDataHoraInicio));
            for (int i = 0; i < ordenadas.size(); i++) {
                Aula atual = ordenadas.get(i);
                for (int j = i + 1; j < ordenadas.size(); j++) {
                    Aula seguinte = ordenadas.get(j);
                    if (!horariosSeSobrepoem(
                            atual.getDataHoraInicio(), atual.getDataHoraFim(),
                            seguinte.getDataHoraInicio(), seguinte.getDataHoraFim())) {
                        break;
                    }
                    idsComConflito.add(atual.getId());
                    idsComConflito.add(seguinte.getId());
                }
            }
        }
        return idsComConflito;
    }

    private boolean horariosSeSobrepoem(LocalDateTime inicioA, LocalDateTime fimA,
                                        LocalDateTime inicioB, LocalDateTime fimB) {
        return inicioA.isBefore(fimB) && inicioB.isBefore(fimA);
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarSucesso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarAlertaVisual(String mensagem, String estilo) {
        if (lblAlertasVisuais == null) {
            return;
        }
        lblAlertasVisuais.setText(mensagem);
        lblAlertasVisuais.setStyle(estilo);
        lblAlertasVisuais.setVisible(true);
        lblAlertasVisuais.setManaged(true);
        if (alertaTimeout != null) {
            alertaTimeout.stop();
        }
        alertaTimeout = new PauseTransition(Duration.seconds(4));
        alertaTimeout.setOnFinished(e -> {
            lblAlertasVisuais.setText("");
            lblAlertasVisuais.setVisible(false);
            lblAlertasVisuais.setManaged(false);
        });
        alertaTimeout.play();
    }

    private void mostrarInfo(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
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

            carregarTodasAulas();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir janela", e.getMessage());
            e.printStackTrace();
        }
    }

    public static class AulaRow {
        private final Long id;
        private final String titulo;
        private final String curso;
        private final String instrutor;
        private final String local;
        private final String dataInicio;
        private final String dataFim;
        private final String vagas;
        private final String status;
        private final boolean conflitoInstrutor;
        private final boolean conflitoLocal;
        private final javafx.beans.property.SimpleStringProperty alertaProperty;

        public AulaRow(Long id, String titulo, String curso, String instrutor, String local,
                      String dataInicio, String dataFim, String vagas, String status,
                      boolean conflitoInstrutor, boolean conflitoLocal) {
            this.id = id;
            this.titulo = titulo;
            this.curso = curso;
            this.instrutor = instrutor;
            this.local = local;
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
            this.vagas = vagas;
            this.status = status;
            this.conflitoInstrutor = conflitoInstrutor;
            this.conflitoLocal = conflitoLocal;
            this.alertaProperty = new javafx.beans.property.SimpleStringProperty(gerarResumoAlertas());
        }

        public Long getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getCurso() { return curso; }
        public String getInstrutor() { return instrutor; }
        public String getLocal() { return local; }
        public String getDataInicio() { return dataInicio; }
        public String getDataFim() { return dataFim; }
        public String getVagas() { return vagas; }
        public String getStatus() { return status; }

        public boolean hasConflito() {
            return conflitoInstrutor || conflitoLocal;
        }

        public String getDescricaoConflito() {
            if (!hasConflito()) {
                return "";
            }
            if (conflitoInstrutor && conflitoLocal) {
                return "Conflito de instrutor e local com outra aula no mesmo horário.";
            }
            if (conflitoInstrutor) {
                return "Instrutor possui outra aula sobreposta.";
            }
            return "Local já possui outra aula sobreposta.";
        }

        public javafx.beans.property.SimpleStringProperty alertaProperty() {
            return alertaProperty;
        }

        private String gerarResumoAlertas() {
            if (!hasConflito()) {
                return "";
            }
            if (conflitoInstrutor && conflitoLocal) {
                return "⚠ Instrutor e Local";
            }
            if (conflitoInstrutor) {
                return "⚠ Instrutor";
            }
            return "⚠ Local";
        }
    }

    @FunctionalInterface
    private interface SupplierChecked {
        List<Aula> get() throws Exception;
    }
}
