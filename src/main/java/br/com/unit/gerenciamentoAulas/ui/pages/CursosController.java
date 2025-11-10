package br.com.unit.gerenciamentoAulas.ui.pages;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.repositories.AulaRepository;
import br.com.unit.gerenciamentoAulas.repositories.CursoRepository;
import br.com.unit.gerenciamentoAulas.servicos.AulaService;
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
import javafx.scene.control.Control;
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
public class CursosController {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private AulaService aulaService;
    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private SessionManager sessionManager;

    @FXML private TextField txtBusca;
    @FXML private Label lblTotalCursos;
    @FXML private Label lblCursosAtivos;
    @FXML private Label lblTotalAulas;
    @FXML private Label lblInfo;
    @FXML private TableView<CursoRow> tabelaCursos;
    @FXML private TableColumn<CursoRow, Long> colId;
    @FXML private TableColumn<CursoRow, String> colNome;
    @FXML private TableColumn<CursoRow, String> colDescricao;
    @FXML private TableColumn<CursoRow, String> colCargaHoraria;
    @FXML private TableColumn<CursoRow, String> colStatus;
    @FXML private TableColumn<CursoRow, Void> colAcoes;

    @FXML private Label lblFlash;

    private final ObservableList<CursoRow> cursosData = FXCollections.observableArrayList();
    private PauseTransition flashTimer;
    private static final String FLASH_SUCESSO =
            "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final String FLASH_ALERTA =
            "-fx-background-color: #ffedd5; -fx-text-fill: #92400e; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;";
    private static final DateTimeFormatter HORA_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        configurarTabela();
        carregarCursos();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCargaHoraria.setCellValueFactory(new PropertyValueFactory<>("cargaHoraria"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colDescricao.setCellFactory(column -> new TableCell<>() {
            private final Label label = new Label();

            {
                label.setWrapText(true);
                setGraphic(label);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    label.setText(null);
                } else {
                    label.setText(item);
                    label.setMaxWidth(column.getWidth() - 20);
                }
            }
        });

        colAcoes.setCellFactory(column -> new TableCell<>() {
            private final Button btnToggle = new Button();
            private final Button btnRemover = new Button("🗑️ Remover");
            private final HBox container = new HBox(8, btnToggle, btnRemover);

            {
                container.getStyleClass().add("actions-cell");
                btnToggle.getStyleClass().add("btn-secondary");
                btnRemover.getStyleClass().add("btn-secondary");

                btnToggle.setOnAction(event -> {
                    CursoRow row = getTableView().getItems().get(getIndex());
                    alternarStatus(row);
                });

                btnRemover.setOnAction(event -> {
                    CursoRow row = getTableView().getItems().get(getIndex());
                    removerCurso(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CursoRow row = getTableView().getItems().get(getIndex());
                    btnToggle.setText(row.isAtivo() ? "⛔ Desativar" : "✅ Ativar");
                    setGraphic(container);
                }
            }
        });

        tabelaCursos.setItems(cursosData);
    }

    private void carregarCursos() {
        List<Curso> cursos = cursoRepository.findAll();
        atualizarTabela(cursos);
    }

    private void atualizarTabela(List<Curso> cursos) {
        cursosData.clear();
        cursosData.addAll(cursos.stream()
            .map(CursoRow::new)
            .collect(Collectors.toList()));

        atualizarEstatisticas();
        lblInfo.setText(String.format("Exibindo %d curso(s)", cursosData.size()));
    }

    private void atualizarEstatisticas() {
        long totalCursos = cursoRepository.count();
        long cursosAtivos = cursoRepository.findByAtivo(true).size();
        long totalAulas = aulaService.listarTodas().size();

        lblTotalCursos.setText(String.valueOf(totalCursos));
        lblCursosAtivos.setText(String.valueOf(cursosAtivos));
        lblTotalAulas.setText(String.valueOf(totalAulas));
    }

    @FXML
    private void handleAtualizar() {
        carregarCursos();
        mostrarFlash("Cursos atualizados às " + LocalDateTime.now().format(HORA_FORMATTER), true);
    }

    @FXML
    private void handleNovo() {
        if (!sessionManager.podeCriarCurso()) {
            sessionManager.mostrarAcessoNegado("Criar Curso");
            return;
        }
        abrirModal("/fxml/pages/CriarCurso.fxml", "Cadastrar Curso");
    }

    @FXML
    private void handleBuscar() {
        String termo = Optional.ofNullable(txtBusca.getText())
                .map(String::trim)
                .orElse("");

        if (termo.isEmpty()) {
            carregarCursos();
            return;
        }

        List<Curso> resultados = cursoRepository.findByNomeContainingIgnoreCase(termo);
        resultados.addAll(
            cursoRepository.findAll().stream()
                .filter(curso -> curso.getCategoria() != null &&
                        curso.getCategoria().toLowerCase().contains(termo.toLowerCase()))
                .collect(Collectors.toList())
        );

        atualizarTabela(resultados.stream().distinct().collect(Collectors.toList()));
    }

    @FXML
    private void handleLimparBusca() {
        txtBusca.clear();
        carregarCursos();
    }

    @FXML
    private void handleExportar() {
        mostrarAlerta(Alert.AlertType.INFORMATION,
                "Exportação em breve",
                "A exportação de cursos está em desenvolvimento.");
    }

    private void alternarStatus(CursoRow row) {
        if (!sessionManager.podeEditarCurso()) {
            sessionManager.mostrarAcessoNegado("Editar Curso");
            return;
        }
        
        Curso curso = row.getCurso();
        curso.setAtivo(!curso.isAtivo());
        cursoRepository.save(curso);
        carregarCursos();
        String msg = String.format("Curso %s agora está %s.",
                curso.getNome(),
                curso.isAtivo() ? "ativo" : "inativo");
        mostrarAlerta(Alert.AlertType.INFORMATION, "Status atualizado", msg);
        mostrarFlash(msg, true);
    }

    private void removerCurso(CursoRow row) {
        if (!sessionManager.podeDeletarCurso()) {
            sessionManager.mostrarAcessoNegado("Deletar Curso");
            return;
        }
        
        Curso curso = row.getCurso();
        long aulasAssociadas = aulaRepository.countByCursoId(curso.getId());

        if (aulasAssociadas > 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Remoção bloqueada",
                    "Este curso possui aulas associadas e não pode ser removido.");
            mostrarFlash("Curso não removido: existem aulas vinculadas.", false);
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar remoção");
        confirmacao.setHeaderText("Deseja remover o curso selecionado?");
        confirmacao.setContentText(curso.getNome());

        confirmacao.showAndWait()
            .filter(resposta -> resposta == ButtonType.OK)
            .ifPresent(resposta -> {
                cursoRepository.delete(curso);
                carregarCursos();
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Removido",
                        "Curso removido com sucesso.");
                mostrarFlash("Curso removido com sucesso.", true);
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

            carregarCursos();
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

    public static class CursoRow {
        private final Curso curso;

        public CursoRow(Curso curso) {
            this.curso = curso;
        }

        public Long getId() {
            return curso.getId();
        }

        public String getNome() {
            return curso.getNome();
        }

        public String getDescricao() {
            return curso.getDescricao() != null ? curso.getDescricao() : "Sem descrição";
        }

        public String getCargaHoraria() {
            return curso.getCargaHoraria() + " h";
        }

        public String getStatus() {
            return curso.isAtivo() ? "Ativo" : "Inativo";
        }

        public boolean isAtivo() {
            return curso.isAtivo();
        }

        private Curso getCurso() {
            return curso;
        }
    }
}
